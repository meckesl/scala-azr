import scala.util.Random
import sttp.client3._
import sttp.client3.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._

// ======================
// 1. Définition des termes (Programmes)
// ======================
sealed trait Term
case class Var(name: String) extends Term
case class Abs(param: String, body: Term) extends Term
case class App(func: Term, arg: Term) extends Term

// ======================
// 2. Moteur Symbolique (Environnement de Vérification)
// ======================
object SymbolicEngine {
  def pretty(term: Term): String = term match {
    case Var(name) => name
    case Abs(param, body) => s"λ$param.${pretty(body)}"
    case App(func, arg) => s"(${pretty(func)} ${pretty(arg)})"
  }

  private def substitute(term: Term, x: String, replacement: Term): Term = term match {
    case Var(y) if y == x => replacement
    case Var(_) => term
    case Abs(y, body) if y == x => term // Shadowing
    case Abs(y, body) => Abs(y, substitute(body, x, replacement))
    case App(f, a) => App(substitute(f, x, replacement), substitute(a, x, replacement))
  }

  def normalize(term: Term, maxSteps: Int = 100): Term = {
    var current = term
    for (_ <- 0 until maxSteps) {
      val next = betaReduceStep(current)
      if (next == current) return current
      current = next
    }
    current
  }

  private def betaReduceStep(term: Term): Term = term match {
    case App(Abs(x, body), arg) => substitute(body, x, arg) // β-reduction
    case App(f, a) => App(betaReduceStep(f), betaReduceStep(a))
    case Abs(x, body) => Abs(x, betaReduceStep(body))
    case v: Var => v
  }

  def areAlphaEquivalent(t1: Term, t2: Term): Boolean = {
    val normalized1 = normalize(t1)
    val normalized2 = normalize(t2)
    pretty(normalized1) == pretty(normalized2)
  }
}

// ======================
// 3. Tâches de Raisonnement AZR
// ======================
sealed trait ReasoningTask
case class Deduction(program: Term, input: Term) extends ReasoningTask
case class Abduction(program: Term, output: Term) extends ReasoningTask
case class Induction(examples: List[(Term, Term)]) extends ReasoningTask

case class TaskTriplet(program: Term, input: Term, output: Term)

// ======================
// 4. Remote "Blank-Slate" LLM Client
// ======================
object RemoteLLM {
  private val backend = HttpClientSyncBackend()

  // Case classes to match the JSON structure of our Python API
  case class LLMRequest(prompt: String)
  case class LLMResponse(generated_text: String)

  // A very basic parser. A real implementation would need a robust parsing library.
  private def parseTerm(text: String): Term = {
    val trimmed = text.trim
    if (trimmed.startsWith("λ")) {
      Abs("x", Var("y")) // Placeholder
    } else if (trimmed.startsWith("(")) {
      App(Var("f"), Var("x")) // Placeholder
    } else {
      Var(trimmed.split(" ").headOption.getOrElse("err"))
    }
  }

  def getResponse(prompt: String): Term = {
    val requestPayload = LLMRequest(prompt).asJson
    val request = basicRequest
      .post(uri"http://127.0.0.1:5000/generate")
      .body(requestPayload.toString)
      .contentType("application/json")
      .response(asJson[LLMResponse])

    println(s"--- 📞 Calling remote LLM at http://127.0.0.1:5000 ---")
    val response = request.send(backend)

    response.body match {
      case Right(llmResponse) =>
        println(s"   Raw response from model: '${llmResponse.generated_text}'")
        // The output will be gibberish. We need to parse it into a Term.
        parseTerm(llmResponse.generated_text)
      case Left(error) =>
        println(s"   ❌ Error calling remote LLM: $error")
        println("   Is the python llm_server.py running?")
        Var("api_error") // Return a default error term
    }
  }
}

// ======================
// 5. Absolute Zero Reasoner (AZR)
// ======================
object AbsoluteZeroReasoner {
  private val random = new Random()
  private var deductionBuffer = List.empty[TaskTriplet]

  def propose(taskType: String): ReasoningTask = {
    val baseTriplet = deductionBuffer(random.nextInt(deductionBuffer.length))
    taskType match {
      case "Deduction" => Deduction(baseTriplet.program, baseTriplet.input)
      case "Abduction" => Abduction(baseTriplet.program, baseTriplet.output)
      case "Induction" => Induction(List((baseTriplet.input, baseTriplet.output)))
    }
  }

  def solve(task: ReasoningTask): Term = {
    val prompt = task match {
      case Deduction(p, i) => s"Deduction Task:\nProgram: ${SymbolicEngine.pretty(p)}\nInput: ${SymbolicEngine.pretty(i)}\nWhat is the output?"
      case Abduction(p, o) => s"Abduction Task:\nProgram: ${SymbolicEngine.pretty(p)}\nOutput: ${SymbolicEngine.pretty(o)}\nWhat was the input?"
      case Induction(ex) => s"Induction Task:\nExamples: ${ex.map(p => s"(${SymbolicEngine.pretty(p._1)} -> ${SymbolicEngine.pretty(p._2)})").mkString(", ")}\nWhat is the program?"
    }
    RemoteLLM.getResponse(prompt)
  }

  private def validateAndStore(program: Term, input: Term): TaskTriplet = {
    val output = SymbolicEngine.normalize(App(program, input))
    val triplet = TaskTriplet(program, input, output)
    println(s"✅ Triplet Validé: P=${SymbolicEngine.pretty(program)}, I=${SymbolicEngine.pretty(input)}, O=${SymbolicEngine.pretty(output)}")
    deductionBuffer :+= triplet
    triplet
  }

  def run(iterations: Int = 10): Unit = {
    println("\n--- Démarrage de la boucle de Self-Play AZR (avec LLM distant 'Table Rase') ---")
    val seedProgram = Abs("x", Var("x"))
    val seedInput = Var("z")
    validateAndStore(seedProgram, seedInput)

    for (i <- 1 to iterations) {
      println(s"\n--- Itération $i/$iterations ---")
      val taskType = Seq("Deduction", "Abduction", "Induction")(random.nextInt(3))
      println(s"1. Le Proposer génère une tâche de type '$taskType'...")
      val taskToSolve = propose(taskType)
      
      println("2. Le Solveur tente de résoudre la tâche...")
      val solution = solve(taskToSolve)
      println(s"   Solution proposée par le solveur: ${SymbolicEngine.pretty(solution)}")

      println("3. Vérification et calcul de la récompense...")
      val (reward, expected) = taskToSolve match {
        case Deduction(p, i) =>
          val correctOutput = SymbolicEngine.normalize(App(p, i))
          (if (SymbolicEngine.areAlphaEquivalent(solution, correctOutput)) 1.0 else 0.0, correctOutput)
        case Abduction(p, o) =>
          val correctOutput = SymbolicEngine.normalize(App(p, solution))
          (if (SymbolicEngine.areAlphaEquivalent(correctOutput, o)) 1.0 else 0.0, Var("any valid input"))
        case Induction(ex) =>
          val correctProgram = deductionBuffer.find(t => t.input == ex.head._1 && t.output == ex.head._2).map(_.program).getOrElse(Var("unknown"))
          (if (SymbolicEngine.areAlphaEquivalent(solution, correctProgram)) 1.0 else 0.0, correctProgram)
      }

      if (reward > 0) {
        println(s"✅ Succès! Récompense: $reward.")
      } else {
        println(s"❌ Échec. Récompense: $reward. Attendu (approx.): ${SymbolicEngine.pretty(expected)}")
      }
    }
    println("\n--- Fin de la boucle de Self-Play AZR ---")
  }
}

// ======================
// 6. Point d'Entrée
// ======================
object Main {
  def main(args: Array[String]): Unit = {
    AbsoluteZeroReasoner.run()
  }
}

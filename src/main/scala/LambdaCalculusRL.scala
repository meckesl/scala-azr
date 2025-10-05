import scala.util.Random
import sttp.client3._
import sttp.client3.circe._
import io.circe.generic.auto._
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

// Triplet validé (Programme, Entrée, Sortie)
case class TaskTriplet(program: Term, input: Term, output: Term)

// ======================
// 3. Remote "Blank-Slate" LLM Client
// ======================
object RemoteLLM {
  private val backend = HttpClientSyncBackend()

  case class LLMRequest(prompt: String)
  case class LLMResponse(generated_text: String)
  case class TrainRequest(reward: Double)
  case class TrainResponse(status: String, loss: Option[Double])

  private def parseTerm(text: String): Term = {
    val trimmed = text.trim.toLowerCase.replaceAll("[^a-zλ.()\\s]", "")
    if (trimmed.contains("λ") || trimmed.contains("\\")) Abs("v", Var("parsed_abs"))
    else if (trimmed.startsWith("(")) App(Var("f"), Var("x"))
    else Var(trimmed.split("\\s+").headOption.filter(_.nonEmpty).getOrElse("err"))
  }

  def getResponse(prompt: String): Term = {
    val requestPayload = LLMRequest(prompt).asJson
    val request = basicRequest
      .post(uri"http://127.0.0.1:5000/generate")
      .body(requestPayload.toString)
      .contentType("application/json")
      .response(asJson[LLMResponse])

    println(s"--- 📞 Calling remote LLM for generation ---")
    val response = request.send(backend)

    response.body match {
      case Right(llmResponse) =>
        println(s"   Raw response from model: '${llmResponse.generated_text}'")
        parseTerm(llmResponse.generated_text)
      case Left(error) =>
        println(s"   ❌ Error calling remote LLM: $error")
        println("   Is the python llm_server.py running?")
        Var("api_error")
    }
  }

  def sendTrainSignal(reward: Double): Unit = {
    val requestPayload = TrainRequest(reward).asJson
    val request = basicRequest
      .post(uri"http://127.0.0.1:5000/train")
      .body(requestPayload.toString)
      .contentType("application/json")
      .response(asJson[TrainResponse])

    println(s"--- 🏋️ Sending training signal (Reward: $reward) ---")
    val response = request.send(backend)

    response.body match {
      case Right(trainResponse) =>
        println(s"   Training step status: ${trainResponse.status}, Loss: ${trainResponse.loss.getOrElse("N/A")}")
      case Left(error) =>
        println(s"   ❌ Error sending training signal: $error")
    }
  }
}

// ======================
// 4. Absolute Zero Reasoner (AZR)
// ======================
object AbsoluteZeroReasoner {
  private val random = new Random()

  // The buffer is now initialized with a seed triplet right at the declaration.
  private val seedTriplet = {
    val program = Abs("x", Var("x")) // identity function
    val input = Var("z")
    val output = SymbolicEngine.normalize(App(program, input)) // output is 'z'
    TaskTriplet(program, input, output)
  }
  private var taskBuffer = List(seedTriplet)

  // --- Rôle 1: Proposer ---
  def proposeProgram(): Term = {
    val examples = taskBuffer.take(2).map(t => SymbolicEngine.pretty(t.program)).mkString("\n")
    val prompt =
      s"""
      You are an expert in lambda calculus. Your goal is to invent a new program.
      Here are some examples of existing programs:
      $examples

      Please provide a new, simple lambda calculus program that is different from the examples.
      Your response should only be the program itself, like "λx.λy.x".
      """
    println("1. Proposer is calling LLM to generate a new program...")
    val newProgram = RemoteLLM.getResponse(prompt)
    println(s"   LLM proposed new program: ${SymbolicEngine.pretty(newProgram)}")
    newProgram
  }

  // --- Rôle 2: Solveur ---
  def solve(program: Term, input: Term): Term = {
    val prompt = s"Deduction Task:\nProgram: ${SymbolicEngine.pretty(program)}\nInput: ${SymbolicEngine.pretty(input)}\nWhat is the output?"
    RemoteLLM.getResponse(prompt)
  }

  // --- Validation (Interaction avec l'environnement) ---
  private def validateAndStore(program: Term, input: Term): TaskTriplet = {
    val output = SymbolicEngine.normalize(App(program, input))
    val triplet = TaskTriplet(program, input, output)
    println(s"   ✅ Triplet Validé: P=${SymbolicEngine.pretty(program)}, I=${SymbolicEngine.pretty(input)}, O=${SymbolicEngine.pretty(output)}")
    taskBuffer :+= triplet
    triplet
  }

  def run(iterations: Int = 20): Unit = {
    println("\n--- Démarrage de la boucle de Self-Play AZR ---")
    // The seeding is now done at initialization, so we just log it.
    println(s"Buffer initialisé avec le triplet seed: P=${SymbolicEngine.pretty(taskBuffer.head.program)}")

    for (i <- 1 to iterations) {
      println(s"\n--- Itération $i/$iterations ---")

      // --- 1. PHASE DE PROPOSITION ---
      val proposedProgram = proposeProgram()
      val randomInput = Var(Seq("a", "b", "c")(random.nextInt(3)))

      // --- 2. PHASE DE VALIDATION ---
      println("2. Validation du nouveau programme avec le moteur symbolique...")
      val validTriplet = validateAndStore(proposedProgram, randomInput)

      // --- 3. PHASE DE RÉSOLUTION ---
      println("3. Le Solveur tente de résoudre la tâche auto-générée...")
      val solution = solve(validTriplet.program, validTriplet.input)
      println(s"   Solution proposée par le solveur: ${SymbolicEngine.pretty(solution)}")

      // --- 4. PHASE DE RÉCOMPENSE ET D'ENTRAÎNEMENT ---
      println("4. Vérification de la solution et calcul de la récompense...")
      val correctOutput = validTriplet.output
      val reward = if (SymbolicEngine.areAlphaEquivalent(solution, correctOutput)) 1.0 else 0.0

      if (reward > 0) {
        println(s"   ✅ Succès! Récompense: $reward.")
      } else {
        println(s"   ❌ Échec. Récompense: $reward. Attendu: ${SymbolicEngine.pretty(correctOutput)}")
      }

      // Envoyer le signal d'entraînement pour l'action du SOLVEUR
      RemoteLLM.sendTrainSignal(reward)
      
      // NOTE: Dans le papier AZR, le PROPOSEUR est également récompensé s'il crée des tâches
      // de difficulté moyenne. Nous simplifions cela pour l'instant.
    }
    println("\n--- Fin de la boucle de Self-Play AZR ---")
  }
}

// ======================
// 5. Point d'Entrée
// ======================
object Main {
  def main(args: Array[String]): Unit = {
    AbsoluteZeroReasoner.run()
  }
}

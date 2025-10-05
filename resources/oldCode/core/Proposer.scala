package core

import models.LLM
import utils.{PromptLoader, CodeExecutor}
import scala.util.{Try, Success, Failure}

class Proposer(llm: LLM, buffer: Buffer) {
  def proposeDeduction(): Try[Task] = {
    val prompt = PromptLoader.load("propose_deduction.txt")
    val references = buffer.sampleReferences(3, Deduction)
      .map(t => s"Program: ${t.program}\nInput: ${t.input.getOrElse("")}\nOutput: ${t.output.getOrElse("")}")
      .mkString("\n---\n")
    val fullPrompt = prompt.replace("{references}", references)
    llm.generate(fullPrompt).flatMap { response =>
      Try {
        val program = extractProgram(response)
        val input = extractInput(response)
        Task.deduction(program, input)
      }
    }
  }

  def proposeAbduction(): Try[Task] = {
    val prompt = PromptLoader.load("propose_abduction.txt")
    val references = buffer.sampleReferences(3, Abduction)
      .map(t => s"Program: ${t.program}\nOutput: ${t.output.getOrElse("")}\nInput: ${t.input.getOrElse("")}")
      .mkString("\n---\n")
    val fullPrompt = prompt.replace("{references}", references)
    llm.generate(fullPrompt).flatMap { response =>
      Try {
        val program = extractProgram(response)
        val output = extractOutput(response)
        Task.abduction(program, output)
      }
    }
  }

  def proposeInduction(): Try[Task] = {
    val prompt = PromptLoader.load("propose_induction.txt")
    val references = buffer.sampleReferences(3, Induction)
      .map(t => s"Program: ${t.program}\nExamples: ${t.examples.mkString(", ")}\nMessage: ${t.message.getOrElse("")}")
      .mkString("\n---\n")
    val fullPrompt = prompt.replace("{references}", references)
    llm.generate(fullPrompt).flatMap { response =>
      println(s"LLM Response for Induction: '$response'")
      Try {
        val program = extractProgram(response)
        val examples = extractExamples(response)
        val message = extractMessage(response)
        Task.induction(program, examples, message)
      }
    }
  }

  private def extractProgram(response: String): String = {
    response.split("```python")(1).split("```")(0).trim
  }

  private def extractInput(response: String): String = {
    response.split("Input: ")(1).split("\n")(0).trim
  }

  private def extractOutput(response: String): String = {
    response.split("Output: ")(1).split("\n")(0).trim
  }

  private def extractExamples(response: String): List[(String, String)] = {
    val examplesRegex = """Examples: \((.*?)\)""".r
    examplesRegex.findFirstMatchIn(response).map { m =>
      val parts = m.group(1).split(",").map(_.trim)
      List((parts(0), parts(1)))
    }.getOrElse(List.empty)
  }

  private def extractMessage(response: String): String = {
    response.split("Message: ")(1).trim
  }
}
package core

import models.LLM
import utils.PromptLoader
import scala.util.Try

class Solver(llm: LLM) {
  def solveDeduction(task: Task): Try[String] = {
    val prompt = PromptLoader.load("solve_deduction.txt")
      .replace("{program}", task.program)
      .replace("{input}", task.input.getOrElse(""))
    llm.generate(prompt).map(_.split("Output: ")(1).trim)
  }

  def solveAbduction(task: Task): Try[String] = {
    val prompt = PromptLoader.load("solve_abduction.txt")
      .replace("{program}", task.program)
      .replace("{output}", task.output.getOrElse(""))
    llm.generate(prompt).map(_.split("Input: ")(1).trim)
  }

  def solveInduction(task: Task): Try[String] = {
    val examplesStr = task.examples.map { case (i, o) => s"Input: $i, Output: $o" }.mkString("\n")
    val prompt = PromptLoader.load("solve_induction.txt")
      .replace("{examples}", examplesStr)
      .replace("{message}", task.message.getOrElse(""))
    llm.generate(prompt).map(_.split("```python")(1).split("```")(0).trim)
  }
}

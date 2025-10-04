package models

import scala.util.Try

trait LLM {
  def generate(prompt: String, references: List[String] = Nil): Try[String]
}

class MockLLM extends LLM {
  override def generate(prompt: String, references: List[String]): Try[String] = {
    Try {
      prompt match {
        case p if p.contains("propose_deduction") =>
          """```python
            |def f(x: int) -> int:
            |    return x * 2 + 1
            |```
            |Input: 3
          """.stripMargin
        case p if p.contains("solve_deduction") =>
          s"""Output: ${(p.split("Input: ")(1).split("\n")(0).trim.toInt * 2 + 1)}"""
        case p if p.contains("propose_abduction") =>
          """```python
            |def f(x: int) -> int:
            |    return x * 3
            |```
            |Output: 9
          """.stripMargin
        case p if p.contains("solve_abduction") =>
          s"""Input: ${p.split("Output: ")(1).split("\n")(0).trim.toInt / 3}"""
        case p if p.contains("propose_induction") =>
          """```python
            |def f(x: int) -> int:
            |    return x + 5
            |```
            |Examples: (1, 6), (2, 7)
            |Message: Add 5 to the input.
          """.stripMargin
        case p if p.contains("solve_induction") =>
          """```python
            |def f(x: int) -> int:
            |    return x + 5
            |```
          """.stripMargin
        case _ =>
          "Response from LLM (simulated)"
      }
    }
  }
}

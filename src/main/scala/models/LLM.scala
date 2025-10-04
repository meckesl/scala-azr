package models

import scala.util.Try

trait LLM {
  def generate(prompt: String, references: List[String] = Nil): Try[String]
}

class MockLLM extends LLM {
  override def generate(prompt: String, references: List[String]): Try[String] = {
      prompt match {
        case p if p.contains("propose a new **deduction task**") =>
          Try {
            """```python
              |def f(x: int) -> int:
              |    return x * 2 + 1
              |```
              |Input: 3
              |Output: 7
            """.stripMargin
          }
        case p if p.contains("solve the deduction task") =>
          Try {
            s"""Output: ${p.split("Input: ")(1).split("\n")(0).trim.toInt * 2 + 1}"""
          }
        case p if p.contains("propose a new **abduction task**") =>
          Try {
            """```python
              |def f(x: int) -> int:
              |    return x * 3
              |```
              |Input: 3
              |Output: 9
            """.stripMargin
          }
        case p if p.contains("solve the abduction task") =>
          Try {
            s"""Input: ${p.split("Output: ")(1).split("\n")(0).trim.toInt / 3}"""
          }
        case p if p.contains("propose a new **induction task**") =>
          Try {
            """```python
              |def f(x: int) -> int:
              |    return x + 5
              |```
              |Examples: (1, 6)
              |Message: Add 5 to the input.
            """.stripMargin
          }
        case p if p.contains("solve the induction task") =>
          Try {
            """```python
              |def f(x: int) -> int:
              |    return x + 5
              |```
            """.stripMargin
          }
        case _ =>
          Try("Response from LLM (simulated)")
      }
  }
}

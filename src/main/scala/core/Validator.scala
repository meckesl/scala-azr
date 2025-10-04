package core

import utils.CodeExecutor
import scala.util.{Try, Success, Failure}

class Validator(executor: CodeExecutor) {
  def validateDeduction(task: Task, solution: String): Try[Boolean] = {
    executor.execute(task.program, task.input.getOrElse("")).map { actualOutput =>
      solution == actualOutput
    }
  }

  def validateAbduction(task: Task, solution: String): Try[Boolean] = {
    executor.execute(task.program, solution).map { actualOutput =>
      actualOutput == task.output.getOrElse("")
    }
  }

  def validateInduction(task: Task, solution: String): Try[Boolean] = {
    val validationResults = task.examples.map { case (input, expectedOutput) =>
      executor.execute(solution, input).map(_ == expectedOutput)
    }
    // Combine les résultats des validations
    val combinedResult = validationResults.foldLeft(Try(true)) { (acc, currentTry) =>
      acc.flatMap { accBool =>
        currentTry.map { currentBool => accBool && currentBool }
      }
    }
    combinedResult
  }

  def isTaskValid(task: Task): Try[Boolean] = {
    task.taskType match {
      case Deduction | Abduction =>
        executor.checkSyntax(task.program).flatMap { syntaxOk =>
          if (!syntaxOk) Try(false)
          else executor.checkDeterminism(task.program, task.input.getOrElse(""))
        }
      case Induction =>
        executor.checkSyntax(task.program)
    }
  }
}

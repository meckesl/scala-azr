package core

import models.LLM
import utils.{CodeExecutor, Logger}
import scala.util.{Try, Success, Failure}

class AZR(llm: LLM, iterations: Int = 100) {
  private val buffer = new Buffer()
  private val proposer = new Proposer(llm, buffer)
  private val solver = new Solver(llm)
  private val validator = new Validator(new CodeExecutor())
  private val logger = new Logger()

  def run(): Unit = {
    (1 to iterations).foreach { i =>
      logger.info(s"Iteration $i/$iterations")

      val taskType = if (i % 3 == 0) Abduction else if (i % 3 == 1) Deduction else Induction
      val proposeTaskTry = taskType match {
        case Deduction => proposer.proposeDeduction()
        case Abduction => proposer.proposeAbduction()
        case Induction => proposer.proposeInduction()
      }

      proposeTaskTry match {
        case Success(proposedTask) =>
          logger.info(s"Proposed task: $proposedTask")
          validator.isTaskValid(proposedTask) match {
            case Success(true) =>
              buffer.addTask(proposedTask)
              val solveTry = taskType match {
                case Deduction => solver.solveDeduction(proposedTask)
                case Abduction => solver.solveAbduction(proposedTask)
                case Induction => solver.solveInduction(proposedTask)
              }

              solveTry match {
                case Success(solution) =>
                  logger.info(s"Solution: $solution")
                  val isCorrectTry = taskType match {
                    case Deduction => validator.validateDeduction(proposedTask, solution)
                    case Abduction => validator.validateAbduction(proposedTask, solution)
                    case Induction => validator.validateInduction(proposedTask, solution)
                  }

                  isCorrectTry match {
                    case Success(isCorrect) =>
                      val solverReward = Reward.solverReward(isCorrect)
                      val solverSuccessRate = if (isCorrect) 0.7 else 0.3
                      val proposerReward = Reward.proposerReward(isCorrect, solverSuccessRate)
                      val combinedReward = Reward.combinedReward(proposerReward, solverReward)
                      logger.info(s"Rewards - Proposer: $proposerReward, Solver: $solverReward, Combined: $combinedReward")
                      logger.info(s"Updating model with reward: $combinedReward")

                    case Failure(e) =>
                      logger.error(s"Validation failed $taskType $i: ${e.getMessage}")
                  }

                case Failure(e) =>
                  logger.error(s"Failed to solve task $taskType $i: ${e.getMessage}")
              }

            case Success(false) =>
              logger.warn(s"Proposed task is invalid $taskType  $i: $proposedTask")

            case Failure(e) =>
              logger.error(s"Failed to validate $taskType task $i: ${e.getMessage}")
          }

        case Failure(e) =>
          logger.error(s"Failed to propose $taskType task $i: ${e.getMessage}")
      }
    }
  }
}

package core

object Reward {
  def proposerReward(isValid: Boolean, solverSuccessRate: Double): Double = {
    if (!isValid) 0.0
    else if (solverSuccessRate == 0.0) 0.0
    else if (solverSuccessRate == 1.0) 0.0
    else 1.0 - solverSuccessRate
  }

  def solverReward(isCorrect: Boolean): Double = {
    if (isCorrect) 1.0 else -1.0
  }

  def combinedReward(proposerReward: Double, solverReward: Double): Double = {
    proposerReward * 0.5 + solverReward * 0.5
  }
}

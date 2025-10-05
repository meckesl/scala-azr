package core

import scala.collection.mutable.ListBuffer
import scala.util.Random

class Buffer {
  private val deductionBuffer = ListBuffer.empty[Task]
  private val abductionBuffer = ListBuffer.empty[Task]
  private val inductionBuffer = ListBuffer.empty[Task]

  def addTask(task: Task): Unit = {
    task.taskType match {
      case Deduction => deductionBuffer += task
      case Abduction => abductionBuffer += task
      case Induction => inductionBuffer += task
    }
  }

  def sampleReferences(n: Int, taskType: TaskType): List[Task] = {
    val buffer = taskType match {
      case Deduction => deductionBuffer
      case Abduction => abductionBuffer
      case Induction => inductionBuffer
    }
    Random.shuffle(buffer.take(n)).toList
  }

  def sampleTask(taskType: TaskType): Option[Task] = {
    val buffer = taskType match {
      case Deduction => deductionBuffer
      case Abduction => abductionBuffer
      case Induction => inductionBuffer
    }
    buffer.headOption
  }

  def size(taskType: TaskType): Int = {
    taskType match {
      case Deduction => deductionBuffer.size
      case Abduction => abductionBuffer.size
      case Induction => inductionBuffer.size
    }
  }
}

package core

import java.util.UUID

sealed trait TaskType
case object Abduction extends TaskType
case object Deduction extends TaskType
case object Induction extends TaskType

case class Task(
                 id: String = UUID.randomUUID().toString,
                 taskType: TaskType,
                 program: String,
                 input: Option[String] = None,
                 output: Option[String] = None,
                 message: Option[String] = None,
                 examples: List[(String, String)] = Nil
               ) {
  override def toString: String =
    s"Task(id=$id, type=$taskType, program=${program.take(30)}..., input=$input, output=$output)"
}

object Task {
  def deduction(program: String, input: String): Task =
    Task(taskType = Deduction, program = program, input = Some(input))

  def abduction(program: String, output: String): Task =
    Task(taskType = Abduction, program = program, output = Some(output))

  def induction(program: String, examples: List[(String, String)], message: String): Task =
    Task(taskType = Induction, program = program, message = Some(message), examples = examples)
}

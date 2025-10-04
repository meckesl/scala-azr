package utils

import sys.process._
import java.io._
import scala.util.{Try, Success, Failure}

class CodeExecutor {
  def execute(program: String, input: String): Try[String] = Try {
    val script =
      s"""
         |def f($input):
         |    $program
         |print(f($input))
      """.stripMargin

    val tempFile = File.createTempFile("azr_script_", ".py")
    tempFile.deleteOnExit()

    val writer = new FileWriter(tempFile)
    writer.write(script)
    writer.close()

    val output = Process(Seq("python3", tempFile.getAbsolutePath), None, "PYTHONPATH" -> "").!!
    output.trim
  }

  def checkSyntax(program: String): Try[Boolean] = Try {
    val tempFile = File.createTempFile("azr_syntax_", ".py")
    tempFile.deleteOnExit()

    val writer = new FileWriter(tempFile)
    writer.write(program)
    writer.close()

    val result = Process(Seq("python3", "-m", "py_compile", tempFile.getAbsolutePath)).!
    result == 0
  }

  def checkDeterminism(program: String, input: String): Try[Boolean] = {
    for {
      output1 <- execute(program, input)
      output2 <- execute(program, input)
    } yield output1 == output2
  }
}

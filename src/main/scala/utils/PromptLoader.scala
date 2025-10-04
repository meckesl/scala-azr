package utils

import scala.io.Source

object PromptLoader {
  def load(filename: String): String = {
    val resource = getClass.getResource(s"/prompts/$filename")
    Source.fromURL(resource).mkString
  }
}

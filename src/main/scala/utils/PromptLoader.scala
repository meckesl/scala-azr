package utils

import scala.io.Source
import java.nio.file.{Paths, Files}

object PromptLoader {
  def load(filename: String): String = {
    val resourcePath = s"prompts/$filename"
    val resourceUrl = getClass.getResource(s"/$resourcePath")
    if (resourceUrl == null) {
      throw new IllegalArgumentException(
        s"Prompt file not found: $resourcePath. " +
          s"Make sure the file exists in src/main/resources/prompts/"
      )
    }
    val s = Source.fromURL(resourceUrl)
    val r = s.mkString
    s.close()
    r
  }
}

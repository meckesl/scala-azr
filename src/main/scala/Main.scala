import models.MockLLM
import core._

object Main extends App {
  val llm = new MockLLM()
  val azr = new AZR(llm, iterations = 5)
  azr.run()
}
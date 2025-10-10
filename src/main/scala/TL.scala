object TL {

  def renderSnailTriangleNumber(dimension: Int = 5): Unit = {
    (0 to dimension) map { sequence =>
      (1 to dimension) map { height =>
        (sequence to dimension) map { term =>
          print(term);
        }
      }
    }
  }

  def main(args: Array[String]): Unit = {
    renderSnailTriangleNumber()
  }

}
object TL {


  /*def KKsnailTriangleNumber(dimension: Long = 5): Unit = {
    (0L until dimension) map { dim =>
      (0L to dim) map { digit =>
        print(sumDigit(digit))
      }
      print("\n")
    }
  }

  def snailTriangleNumber(digit: Long, acc: Long = 0L): Long = {
    digit match {
      case x if x > 0 => sumDigit(digit - 1L, acc + digit)
      case _ => acc
    }
  }*/

  def snailRow(row: Int): Unit = {
    (0 until row)
  }



  def main(args: Array[String]): Unit = {
    //print(sumDigit(3L))
    //*snailTriangleNumber()
  }

}
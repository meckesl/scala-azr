import java.awt.image.BufferedImage
import java.awt.Color
import java.io.File
import javax.imageio.ImageIO

class TriangleNumbers(val height: Int) {
  // Génère la pyramide sous forme de List[List[String]]
  private val pyramid: List[List[String]] = {
    def pad(n: Int): String = f"$n%06d" // Formatage sur 2 chiffres avec zéro initial
    (1 to height).scanLeft((-1, List[String]())) { case ((lastNum, _), line) =>
      val start = lastNum + 1
      val end = start + line - 1
      val currentLine =
        //if (line % 2 == 1) (start to end).map(pad).toList
        //else (end to start by -1).map(pad).toList
        (start to end).map(pad).toList
      (end, currentLine)
    }.tail.map(_._2).toList
  }

  private def isPrime(n: Int): Boolean =
    if (n <= 1) false
    else !(2 to math.sqrt(n).toInt).exists(n % _ == 0)

  private def isPerfectSquare(n: Int): Boolean = {
    if (n < 0) false
    else {
      val root = math.sqrt(n).toInt
      root * root == n
    }
  }

  // Affiche la pyramide avec un alignement visuel
  def display(): Unit = {
    pyramid.zipWithIndex.foreach { case (line, i) =>
      val lineNumber = i + 1
      val sumOfElements = line.map(_.toInt).sum
      val calculationResult = sumOfElements.toDouble
      val indent = " " * (height - i - 1) // * 3 Indentation pour l'alignement
      val processedLine = line.map {
        case s =>
        val num = s.toInt
        if (isPrime(num)) "."
        // else if (isPerfectSquare(num)) "s"
        else "" //s
      }
      println(f"$lineNumber%3d: ($calculationResult%10.0f) " + indent + processedLine.mkString(" "))
    }
  }

  def renderPyramidImage(filePath: String, pixelSize: Int = 1): Unit = {
    val maxLineWidth = pyramid.last.length * pixelSize
    val image = new BufferedImage(maxLineWidth, height * pixelSize, BufferedImage.TYPE_INT_RGB)
    val g = image.createGraphics()

    // Déterminer la somme maximale des chiffres pour la normalisation
    val maxNum = height * (height + 1) / 2
    val maxSum = math.log10(maxNum).toInt * 9 // Estimation

    pyramid.zipWithIndex.foreach { case (line, y) =>
      val lineWidth = line.length * pixelSize
      val xOffset = 0// (maxLineWidth - lineWidth) / 2
      line.zipWithIndex.foreach { case (s, x) =>
        val num = s.toInt
        val color = if (isPrime(num)) {
          val digitSum = s.map(_ - '0').sum
          // Normaliser la luminosité en fonction de la somme des chiffres
          val brightness = (50 + (205 * digitSum.toDouble / maxSum)).toInt
          new Color(math.min(255, brightness), math.min(255, brightness), math.min(255, brightness))
        //} else if (isPerfectSquare(num)) {
        //  Color.WHITE
        } else {
          val gray = 15 //(255 * (num.toDouble / (height * height))).toInt
          new Color(gray, gray, gray)
        }
        g.setColor(color)
        g.fillRect(xOffset + x * pixelSize, y * pixelSize, pixelSize, pixelSize)
      }
    }

    g.dispose()
    ImageIO.write(image, "png", new File(filePath))
  }

  // Retourne la pyramide sous forme de List[List[String]]
  def getPyramid: List[List[String]] = pyramid

  // Retourne la pyramide sous forme de List[List[Int]]
  def getNumericPyramid: List[List[Int]] =
    pyramid.map(_.map(_.toInt))

  // Calcule la somme des chiffres de chaque nombre (ex: "05" → 0 + 5 = 5)
  def digitSums: List[List[Int]] =
    pyramid.map(_.map(s => s.foldLeft(0)((acc, c) => acc + (c - '0'))))

  // Filtre les nombres premiers
  def primes: List[List[String]] = {
    pyramid.map(_.filter(s => isPrime(s.toInt)))
  }

  // Convertit chaque nombre en base 3 (String)
  def toBase3: List[List[String]] =
    pyramid.map(_.map(n => Integer.toString(n.toInt, 3)))

  // Retourne les nombres palindromes (en base 10)
  def palindromes: List[List[String]] =
    pyramid.map(_.filter(s => s == s.reverse))

  // Retourne les nombres dont la somme des chiffres en base 3 est paire
  def evenBase3Sum: List[List[String]] = {
    def sumBase3Digits(s: String): Int =
      Integer.toString(s.toInt, 3).foldLeft(0)((acc, c) => acc + (c - '0'))

    pyramid.map(_.filter(n => sumBase3Digits(n) % 2 == 0))
  }

  // Retourne la pyramide sous forme de chaîne formatée
  override def toString: String =
    pyramid.map(_.mkString(" ")).mkString("\n")
}

// Exemple d'utilisation
object TriangleNumbersExample {
  def main(args: Array[String]): Unit = {
    val pyramid = new TriangleNumbers(4000)
    println("Pyramide numérique en spirale inversée :")
    pyramid.renderPyramidImage("pyramid.png")
  }
}
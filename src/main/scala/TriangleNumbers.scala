class TriangleNumbers(val height: Int) {
  // Génère la pyramide sous forme de List[List[String]]
  private val pyramid: List[List[String]] = {
    def pad(n: Int): String = f"$n%02d" // Formatage sur 2 chiffres avec zéro initial

    (1 to height).scanLeft((-1, List[String]())) { case ((lastNum, _), line) =>
      val start = lastNum + 1
      val end = start + line - 1
      val currentLine =
        if (line % 2 == 1) (start to end).map(pad).toList
        else (end to start by -1).map(pad).toList
      (end, currentLine)
    }.tail.map(_._2).toList
  }

  // Affiche la pyramide avec un alignement visuel
  def display(): Unit = {
    pyramid.zipWithIndex.foreach { case (line, i) =>
      val indent = " " * (height - i - 1) * 3 // Indentation pour l'alignement
      println(indent + line.mkString(" "))
    }
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
    def isPrime(n: Int): Boolean =
      if (n <= 1) false
      else !(2 to math.sqrt(n).toInt).exists(n % _ == 0)

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
    val pyramid = new TriangleNumbers(8)
    println("Pyramide numérique en spirale inversée :")
    pyramid.display()

    println("\nSomme des chiffres de chaque nombre :")
    pyramid.digitSums.foreach(println)

    println("\nNombres premiers dans la pyramide :")
    pyramid.primes.foreach(println)

    println("\nNombres en base 3 :")
    pyramid.toBase3.foreach(println)

    println("\nNombres avec somme des chiffres en base 3 paire :")
    pyramid.evenBase3Sum.foreach(println)
  }
}

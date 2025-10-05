name := "azr-scala"
version := "0.1.0"
scalaVersion := "2.13.10"

val SmileVersion = "3.0.2"

libraryDependencies ++= Seq(
  "com.github.haifengl" % "smile-core" % SmileVersion,
  "org.scala-lang.modules" %% "scala-parallel-collections" % "1.0.4",
  "org.scalatest" %% "scalatest" % "3.2.15" % Test,
)

resolvers ++= Seq(
  "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
)
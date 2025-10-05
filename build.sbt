name := "azr-scala"
version := "0.1.0"
scalaVersion := "2.13.14"

libraryDependencies ++= Seq(
  "com.github.haifengl" % "smile-core" % "3.0.2",
  "org.scala-lang.modules" %% "scala-parallel-collections" % "1.0.4",
  "org.scalatest" %% "scalatest" % "3.2.15" % Test,

  // For LLM Integration
  "com.softwaremill.sttp.client3" %% "core" % "3.9.7",
  "com.softwaremill.sttp.client3" %% "circe" % "3.9.7",
  "io.circe" %% "circe-core" % "0.14.9",
  "io.circe" %% "circe-generic" % "0.14.9",
  "io.circe" %% "circe-parser" % "0.14.9"
)

resolvers ++= Seq(
  "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
)
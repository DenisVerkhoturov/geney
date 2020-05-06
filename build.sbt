import Dependencies._

ThisBuild / version      := "0.1"
ThisBuild / scalaVersion := "2.13.1"
ThisBuild / scalacOptions := Seq(
  "-encoding",
  "utf8",
  "-deprecation",
  "-Xfatal-warnings"
)

lazy val root = (project in file("."))
  .settings(
    name                        := "Geney",
    description                 := "De Bruijn graph-based De Nova genome assembly CLI tool",
    mainClass in assembly       := Some("parser.Main"),
    assemblyJarName in assembly := "geney.jar"
  )
  .aggregate(assembler, cli, utils)
  .dependsOn(assembler, cli, utils)

lazy val assembler = project
  .settings(
    name := "Assembler and library",
    libraryDependencies ++= commonDependencies
  )
  .aggregate(utils)
  .dependsOn(utils % "test->test")

lazy val cli = project
  .settings(
    name := "Command line interface",
    libraryDependencies ++= cliDependencies
  )
  .aggregate(assembler, utils)
  .dependsOn(assembler, utils)

lazy val utils = project
  .settings(
    name := "Utilities",
    libraryDependencies ++= commonDependencies
  )

lazy val commonDependencies = Seq(
  scalastic,
  scalatest,
  silencerPlugin,
  silencerLib
)
lazy val cliDependencies = Seq(
  scalastic,
  scalatest,
  silencerPlugin,
  silencerLib,
  scopt
)

lazy val formatAll   = taskKey[Unit]("Format all the source code which includes src, test, and build files")
lazy val checkFormat = taskKey[Unit]("Check all the source code which includes src, test, and build files")

lazy val commonSettings = Seq(
  formatAll := {
    (scalafmt in Compile).value
    (scalafmt in Test).value
  },
  checkFormat := {
    (scalafmtCheck in Compile).value
    (scalafmtCheck in Test).value
  },
  compile in Compile := (compile in Compile).dependsOn(checkFormat).value,
  test in Test       := (test in Test).dependsOn(checkFormat).value
)

addCommandAlias("fmt", "all scalafmtSbt scalafmt test:scalafmt")
addCommandAlias("check", "all scalafmtSbtCheck scalafmtCheck test:scalafmtCheck")

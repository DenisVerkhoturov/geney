import Dependencies._

ThisBuild / version       := "0.1"
ThisBuild / scalaVersion  := "2.13.1"
ThisBuild / scalacOptions := Seq(
  "-encoding",
  "utf8",
  "-deprecation",
  "-Xfatal-warnings"
)

lazy val root = (project in file("."))
  .settings(
    name := "Geney",
    description := "De Bruijn graph-based De Nova genome assembly CLI tool"
  )
  .aggregate(assembler, cli, utils)

lazy val assembler = project
  .settings(
    name := "Assembler and library",
    libraryDependencies ++= commonDependencies
  )
  .aggregate(utils)
  .dependsOn(utils)

lazy val cli = project
  .settings(
    name := "Command line interface",
    libraryDependencies ++= commonDependencies
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
  scalatest
)

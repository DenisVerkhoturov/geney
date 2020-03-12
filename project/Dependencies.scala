import sbt._

object Dependencies {
  private object Version {
    val scalastic = "3.1.1"
    val scalatest = "3.1.1"
  }

  val scalastic = "org.scalactic" %% "scalactic" % Version.scalastic % Test
  val scalatest = "org.scalatest" %% "scalatest" % Version.scalatest % Test
}

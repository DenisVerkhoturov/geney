import sbt._

object Dependencies {
  private object Version {
    val scalastic = "3.1.1"
    val scalatest = "3.1.1"
    val silencer  = "1.6.0"
    val scopt = "4.0.0-RC2"
  }

  val scalastic   = "org.scalactic"   %% "scalactic"   % Version.scalastic % Test
  val scalatest   = "org.scalatest"   %% "scalatest"   % Version.scalatest % Test
  val silencerLib = "com.github.ghik" % "silencer-lib" % Version.silencer  % Provided cross CrossVersion.full
  val silencerPlugin = compilerPlugin(
    "com.github.ghik" % "silencer-plugin" % Version.silencer % Provided cross CrossVersion.full
  )
  val scopt = "com.github.scopt" %% "scopt" % Version.scopt % Compile
}

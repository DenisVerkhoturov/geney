package parser

import scopt.OParser

object Main {
  val mainParser = new CliParser;
  def main(args: Array[String]): Unit =
  {
    mainParser.parse(args);
  }
}

package parser


import scopt.{OParser, OParserBuilder, OptionParser}

import java.io.File


class CliParser {
  val builder: OParserBuilder[CliParserConfig] = OParser.builder[CliParserConfig]
  val parser: OParser[Unit, CliParserConfig] = {
    import builder._
    OParser.sequence(
      programName("CliParser"),
      head("CliParser"),
      opt[File]("jar")
          .valueName("<file>")
          .action((x, c) => c.copy(input = x))
          .text("jar file"),
      opt[Int]('k',"")
        .action((x, c) => c.copy(k = x))
        .text("k is an  integer property, the length of k that will be used to build De Bruijn graph")
        .validate( x =>
          if (x > 0) success
          else failure("Option -k must be >0") ),
      opt[File]('i', "input")
        .valueName("<file>")
        .action((x, c) => c.copy(input = x))
        .text("input file with reads to be analyzed"),
      opt[File]('o', "output")
        .valueName("<file>")
        .action((x, c) => c.copy(input = x))
        .text("output file to write the result to"),
      help('h',"help").text("show usage"),
    )
  }
  //val args = Seq("");
  // OParser.parse returns Option[Config]

  def parse(args: Array[String]): Unit = {
    OParser.parse(parser, args, CliParserConfig()) match {
      case Some(config) =>
      // do something
      case _ =>
      // arguments are bad, error message will have been displayed
    }
  }




}

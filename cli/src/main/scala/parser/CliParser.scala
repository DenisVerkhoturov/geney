package parser


import scopt.{OParser, OParserBuilder}
import java.io.{File, PrintWriter}

import fasta.Fasta
import graph.DeBruijnGraph

import scala.io.Source


class CliParser {
  val builder: OParserBuilder[CliParserConfig] = OParser.builder[CliParserConfig]
  val parser: OParser[Unit, CliParserConfig] = {
    import builder._
    OParser.sequence(
      programName("CliParser"),
      head("CliParser"),
      opt[File]("jar")
        .valueName("<file>")
        .action((x, c) => c.copy(jar = x))
        .text("jar file"),
      opt[Int]('k',"")
        .action((x, c) => c.copy(k = x))
        .text("k is an  integer property, the length of k that will be used to build De Bruijn graph")
        .validate( k =>
          if (k >= 2) success
          else failure("Option -k must be >2") ),
      opt[File]('i', "input")
        .required()
        .validate(f =>
          if (f.exists()) success
          else failure("Input file doesn't exist.")
        )
        .valueName("<file>")
        .action((x, c) => c.copy(input = x))
        .text("input file with reads to be analyzed"),
      opt[File]('o', "output")
        .valueName("<file>")
        .action((x, c) => c.copy(output = x))
        .text("output file to write the result to"),
      help('h',"help").text("show usage"),
    )
  }
  def parse(args: Array[String]):Unit = {
    OParser.parse(parser, args, CliParserConfig()) match {
      case Some(config) =>
        println("Args have been parsed successfully.");
        processGraph(config);
      case _ =>
        println("Couldn't parse args.");
    }
  }
  private def processGraph(config: CliParserConfig): Unit = {
    val source = Source.fromFile(config.input);
    val deBruijnGraph = new DeBruijnGraph(LazyList(source.toString()), config.k);
    source.close();
    val writer = new PrintWriter(config.output);
    writer.println("Here could be some info about graph");
    writer.close();
  }
}
object Main {
  val mainParser = new CliParser;
  def main(args: Array[String]): Unit =
  {
    mainParser.parse(args);
  }
}

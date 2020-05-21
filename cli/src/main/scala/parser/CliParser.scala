package parser


import scopt.{OParser, OParserBuilder}
import java.io.{File, PrintWriter}
import java.nio.file.Files

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
      opt[Int]('k',"k")
        .required()
        .action((x, c) => c.copy(k = x))
        .text("k is an  integer property, the length of k that will be used to build De Bruijn graph")
        .validate( k =>
          if (k >= 2) success
          else failure("Argument -k must be >2") ),
      opt[File]('i', "input")
        .required()
        .validate(input =>
          if (!input.exists()) failure("Input file doesn't exist")
          else if (input.isDirectory) failure("Path to input file is a directory")
          else if (!Files.isReadable(input.toPath)) failure("Access to input file is denied")
          else success
        )
        .valueName("<file>")
        .action((x, c) => c.copy(input = x))
        .text("input file with reads to be analyzed"),
      opt[File]('o', "output")
        .valueName("<file>")
        .action((x, c) => c.copy(output = x))
        .validate(output =>
          if (output.isDirectory) failure("Path to output file is a directory")
          else if (output.exists()) failure("Output file already exists")
          else if (!Files.isWritable(output.getParentFile.toPath)) failure("Access to output file is denied")
          else success
        )
        .text("output file to write the result to"),
      opt[String]('f', "format")
        .required()
        .action((x, c) => c.copy(format = x))
        .validate {
          case "fasta" => success
          case "fastq" => success
          case "derive" => success
          case _ => failure("Wrong type of format")
        },
      help('h',"help").text("show usage"),
    )
  }
  def parse(args: Array[String]):Unit = {
    OParser.parse(parser, args, CliParserConfig()) match {
      case Some(config) =>
        println("Args have been parsed successfully.");
        processGraph(config);
      case _ =>
        System.err.println("Couldn't parse args.");
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

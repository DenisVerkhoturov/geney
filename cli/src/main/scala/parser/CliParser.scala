package parser

import java.io.{File, PrintStream}
import java.nio.file.Files

import fasta.Fasta
import fastq.Fastq
import graph.DeBruijnGraph
import scopt.{OParser, OParserBuilder}

import scala.io.{Source, StdIn}

class CliParser {
  val builder: OParserBuilder[CliParserConfig] = OParser.builder[CliParserConfig]
  val parser: OParser[Unit, CliParserConfig] = {
    import builder._
    OParser.sequence(
      programName("CliParser"),
      head("CliParser"),
      opt[Int]('k', "k")
        .required()
        .action((x, c) => c.copy(k = x))
        .text("k is an  integer property, the length of k that will be used to build De Bruijn graph")
        .validate(
          k =>
            if (k >= 2) success
            else failure("Argument -k must be >2")
        ),
      opt[File]('i', "input")
        .required()
        .validate(
          input =>
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
        .validate(
          output =>
            if (output.isDirectory) failure("Path to output file is a directory")
            else if (!Files.isWritable(output.getAbsoluteFile.getParentFile.toPath))
              failure("Access to output file is denied")
            else success
        )
        .text("output file to write the result to"),
      opt[String]('f', "format")
        .required()
        .action((x, c) => c.copy(format = x))
        .validate {
          case "fasta"  => success
          case "fastq"  => success
          case "derive" => success
          case _        => failure("Wrong type of format")
        },
      opt[Unit]("force")
        .action((_, c) => c.copy(force = true))
        .text("flag to force override output file"),
      help('h', "help").text("show usage"),
      checkConfig(c => {
        if (c.output != null && c.output.exists() && !c.force)
          failure(
            "File " + c.output.getAbsolutePath + " already exists. Use flag '--force' to override existing file."
          )
        else success
      })
    )
  }
  def parse(args: Array[String]): Unit =
    OParser.parse(parser, args, CliParserConfig()) match {
      case Some(config) =>
        println("Args have been parsed successfully.")
        processGraph(config)
      case _ =>
        System.err.println("Couldn't parse args.")
    }
  private def processGraph(config: CliParserConfig): Unit = {
    val source = Source.fromFile(config.input)
    val data   = (for (line <- source.getLines()) yield line).toSeq
    source.close()
    if (config.output != null) System.setOut(new PrintStream(new File(config.output.getAbsolutePath)))
    config.format match {
      case "fasta" =>
        val fastaData = Fasta.reads(data)
        fastaData match {
          case Left(string) => System.err.println(string)
          case Right(records) =>
            val deBruijnGraph = DeBruijnGraph(records.map(_.data).to(LazyList), config.k)
            System.out.println(Fasta.show(fasta.Record(">geney-cli v. 0.1", deBruijnGraph.path)).mkString("\n"))
        }
      case "fastq" =>
        val fastqData = Fastq.reads(data)
        fastqData match {
          case Left(string) => System.err.println(string)
          case Right(records) =>
            val deBruijnGraph = DeBruijnGraph(records.map(_.data).to(LazyList), config.k)
            System.out.println(Fastq.show(fastq.Record(">geney-cli v. 0.1", deBruijnGraph.path, "")).mkString("\n"))
        }
    }
  }
}
object Main {
  val mainParser = new CliParser
  def main(args: Array[String]): Unit =
    mainParser.parse(args)
}

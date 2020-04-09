package parser

import java.io.File

case class CliParserConfig(
                   jar: File = new File("."),
                   k: Int = -1,
                   input: File = new File("."),
                   output: File = new File(".")
                 )

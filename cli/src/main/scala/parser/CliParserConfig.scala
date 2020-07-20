package parser

import java.io.File

case class CliParserConfig(
    k: Int = -1,
    input: File = new File("."),
    output: File = null,
    format: String = "",
    force: Boolean = false
)

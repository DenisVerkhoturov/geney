package fastq

import parser.Parser

object Fastq extends Parser[Record] {
  def show(record: Record): Seq[String] = Seq("@" + record.description, record.data, "+", record.quality)

  def shows(records: Seq[Record]): Seq[String] = records.flatMap(show)

  def read(line: Seq[String]): Either[String, Record] = line match {
    case Seq() => Left("No input lines provided")
    case description +: data +: "+" +: quality +: Seq() if description.head == '@' =>
      Right(Record(description.tail, data, quality))
    case _ => Left("Incorrect format")
  }

  def reads(lines: Seq[String]): Either[String, Seq[Record]] =
    if (lines.isEmpty) Left("No input lines provided")
    else
      lines.grouped(4).map(read).span(_.isRight) match {
        case (records, rest) if rest.isEmpty => Right(for (Right(el) <- records.toSeq) yield el)
        case _                               => Left("Can't parse all records")
      }
}

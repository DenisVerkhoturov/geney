package fasta

class Fasta extends Parser[Record] {
  override def show(record: Record): Seq[String] =
    record.description +: record.data.grouped(80).toSeq

  override def shows(records: Seq[Record]): Seq[String] = records.flatMap(show)

  override def read(line: Seq[String]): Either[String, Record] =
    if (line.isEmpty) Left("Input line is empty")
    else Right(Record(line.head, line.tail.mkString("")))

  override def reads(lines: Seq[String]): Either[String, Seq[Record]] = {
    def checkLefts(records: Seq[Seq[String]]): Either[String, Seq[Record]] =
      records match {
        case Seq() => Right(Seq())
        case head +: tail =>
          val tailSeq  = checkLefts(tail)
          val headRead = read(head)
          (headRead, tailSeq) match {
            case (Right(x), Right(xs)) => Right(x +: xs)
            case _                     => Left("Cannot parse single record")
          }
      }
    checkLefts(partRecords(lines))
  }

  private def partRecords(lines: Seq[String]): Seq[Seq[String]] =
    if (lines.isEmpty) Seq()
    else {
      val (first, second) = lines.tail.span(_.head != '>')
      (lines.head +: first) +: partRecords(second)
    }
}

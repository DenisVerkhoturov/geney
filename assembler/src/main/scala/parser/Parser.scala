package parser

trait Parser[T] {
  def show(record: T): Seq[String]
  def shows(records: Seq[T]): Seq[String]
  def read(line: Seq[String]): Either[String, T]
  def reads(lines: Seq[String]): Either[String, Seq[T]]
}

import java.util.NoSuchElementException

object Main {
  def main(args: Array[String]): Unit = {}

  def sum(list: List[Int]): Int = {
    list match {
      case x :: tail => x + sum(tail)
      case Nil => 0
    }
  }

  def max(list: List[Int]): Int = list match {
    case Nil => throw new NoSuchElementException("The list is empty")
    case head :: Nil => head
    case list =>
      val maxTail = max(list.tail)
      if (list.head > maxTail) list.head else maxTail
  }
}

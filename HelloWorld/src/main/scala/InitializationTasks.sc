object InitializationTasks
{
  @scala.annotation.tailrec
  private def sumListHelp(sum: Int, list: List[Int]): Int = if (list.isEmpty) sum else sumListHelp(sum + list.head, list.tail)
  def sumList(list: List[Int]): Int = if (list.isEmpty) 0 else sumListHelp(list.head, list.tail)

  @scala.annotation.tailrec
  private def maxElementHelp(max: Int, list: List[Int]): Int = if (list.isEmpty) max else if (list.head > max) maxElementHelp(list.head, list.tail) else maxElementHelp(max, list.tail)
  def maxElement(list: List[Int]): Int = if (list.isEmpty) throw new IllegalArgumentException else maxElementHelp(list.head, list.tail)

}
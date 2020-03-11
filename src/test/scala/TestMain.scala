import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import Main.{max, sum}

class TestMain extends AnyWordSpec with Matchers {
  "Main" can {
    "sum" should {
      "return 0 when empty list provided" in {
        sum(List()) should be(0)
      }

      "return sum of sublists equal to sum of combined list" in {
        sum(List(1, 2, 3, 4, 5)) should be(sum(List(1, 2, 3)) + sum(List(4, 5)))
      }

      "return sum equal to sum of reversed list" in {
        val list = List(1, 2, 3, 4, 5)
        sum(list) should be(sum(list.reverse))
      }

      "return size of list multiplied by element when list contain equal elements" in {
        val element = 3
        val list = List(element, element, element)
        sum(list) should be(element * list.size)
      }
    }

    "max" should {
      "throw NoSuchElement exception when list is empty" in {
        an[NoSuchElementException] should be thrownBy (max(List()))
      }

      "return element in list which contains only 1 element" in {
        val element = 3
        max(List(element)) should be(element)
      }

      "return element that is contained in list" in {
        val list = List(5, 8, 10)
        list.contains(max(list)) should be(true)
      }

      "return value less or equal to previous maximum when list has next maximum" in {
        @scala.annotation.tailrec
        def nextMaxLessOrEqual(previousMax: Int, list: List[Int]): Any = {
          if (list.isEmpty) return

          val nextMax = max(list)
          nextMax should be <= previousMax
          nextMaxLessOrEqual(nextMax, list diff List(previousMax))
        }

        val list = List(5, 3, 8, 2, 4, 10)
        val previousMax = max(list)
        nextMaxLessOrEqual(previousMax, list.filterNot(it => List(previousMax).contains(it)))
      }
    }
  }
}

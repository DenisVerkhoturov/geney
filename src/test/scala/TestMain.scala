import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import Main.{max, sum}

class TestMain extends AnyWordSpec with Matchers {
  "Sum function" when {
    "list is empty" should {
      "return 0" in {
        sum(List()) should be(0)
      }
    }

    "list is splitted on sublists" should {
      "be equal to sum of it's sublists" in {
        sum(List(1, 2, 3, 4, 5)) should be(sum(List(1, 2, 3)) + sum(List(4, 5)))
      }
    }

    "list is reversed" should {
      "be equal to sum of original list" in {
        val list = List(1, 2, 3, 4, 5)
        sum(list) should be(sum(list.reverse))
      }
    }

    "list contains equal elements" should {
      "return size of list multiplied by element" in {
        val element = 3
        val list = List(element, element, element)
        sum(list) should be(element * list.size)
      }
    }
  }

  "Max function" when {
    "list is empty" should {
      "throw NoSuchElement exception" in {
        an[NoSuchElementException] should be thrownBy (max(List()))
      }
    }

    "list contains 1 element" should {
      "return this element" in {
        val element = 3
        max(List(element)) should be(element)
      }
    }

    "list has maximum" should {
      "return element that is contained in list" in {
        val list = List(5, 8, 10)
        list.contains(max(list)) should be(true)
      }
    }

    "list has next maximum" should {
      "return value less or equal to previous maximum" in {
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

import org.scalatest._

class InitializationTasksSpec extends WordSpec{
  override def withFixture(test: NoArgTest) = { // Define a shared fixture
    // Shared setup (run at beginning of each test)
    try test()
    finally {
      // Shared cleanup (run at end of each test)
    }
  }
  "sumList" can {
    "empty" should {
      "return 0" in {
        InitializationTasks.sumList(Nil)
      }
    }
    "list of equal elements" should {
      "composition of number of elements and value" in {
        assert(InitializationTasks.sumList(List(5, 5, 5)) == 5 * 3)
      }
    }
    "sum of the list" should {
      "be equal to the sum of reversed list" in {
        assert(InitializationTasks.sumList(List(1, 2, 3, 4, 5)) == InitializationTasks.sumList(List(5, 4, 3, 3, 1)))
      }
    }
    "sum of the list" should {
      "be equal to the sum of two sums of two lists" in {
        assert(InitializationTasks.sumList(List(1, 2, 3)) + InitializationTasks.sumList(List(3, 4, 5)) == InitializationTasks.sumList(List(1, 2, 3, 3, 4, 5)))
      }
    }
  }

  "maxElement" can {
    "empty" should {
      "produce IllegalArgumentException" in {
        intercept[IllegalArgumentException] {
          InitializationTasks.maxElement(Nil)
        }
      }
    }
    "maximum of list of one element" should {
      "be equal to the element" in {
        assert(InitializationTasks.maxElement(List(10)) == 10)
      }
    }
    "maximum of list of several elements" should {
      "be an element of the list" in {
        assert(InitializationTasks.maxElement(List(1, 2, 3, 2, 1)) == 3)
      }
    }
    "maximum of list of increasing sequence" should {
      "be the last element of the list" in {
        assert(InitializationTasks.maxElement(List(1, 2, 2, 3, 3, 4, 5, 6, 7)) == 7)
      }
    }
  }
}
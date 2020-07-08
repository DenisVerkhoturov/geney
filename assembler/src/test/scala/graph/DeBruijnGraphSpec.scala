package graph

import com.github.ghik.silencer.silent
import org.scalatest.concurrent.{Signaler, TimeLimitedTests}
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{Seconds, Span}
import org.scalatest.wordspec.AnyWordSpec

import utils.CustomMatchers.beOneOf
import utils.StringUtils.rotations

class DeBruijnGraphSpec extends AnyWordSpec with Matchers with TimeLimitedTests {
  val timeLimit: Span = Span(10, Seconds)

  @silent
  override val defaultTestSignaler: Signaler = (testThread: Thread) => testThread.stop()

  "DeBruijnGraph" should {
    "throw IllegalArgumentException when provided k is less than 2" in {
      the[IllegalArgumentException] thrownBy {
        DeBruijnGraph(LazyList("ACGTCGA"), 1)
      } should have message "requirement failed: k must be more than 1"
    }

    "produce empty graph when empty list provided" in {
      val graph = DeBruijnGraph(LazyList.empty, 5)
      graph.nodes shouldBe empty
      graph.branches shouldBe empty
    }

    "produce empty graph when k is bigger than any provided read's length" in {
      val graph = DeBruijnGraph(LazyList("ACGTCGA"), 10)
      graph.nodes shouldBe empty
      graph.branches shouldBe empty
    }

    "produce all k-1-mers as nodes from provided reads" in {
      val k     = 3
      val read  = "ACGTACGATGCA"
      val nodes = DeBruijnGraph(LazyList(read), k).nodes

      (0 to read.length - k + 1) foreach (i => nodes should contain(read.substring(i, i + k - 1)))
    }

    "produce all k-mers as branches from provided reads" in {
      val k        = 3
      val read     = "ACGTACGATGCA"
      val branches = DeBruijnGraph(LazyList(read), k).branches

      (0 to read.length - k) foreach { i =>
        val kmer = read.substring(i, i + k)
        branches(kmer take k - 1) should contain(kmer takeRight k - 1)
      }
    }

    "contain walk equal to initial read" in {
      val k     = 3
      val read  = "ACGTACGATGCA"
      val graph = DeBruijnGraph(LazyList(read), k)

      def check(read: String, branches: Map[String, List[String]]): Unit =
        if (read.length < k) branches shouldBe empty
        else {
          val kmer         = read take k
          val left         = kmer take k - 1
          val right        = kmer takeRight k - 1
          val leftBranches = branches(left)

          leftBranches should contain(right)

          val newBranches =
            if (leftBranches.size == 1) branches - left
            else branches updated (left, leftBranches diff List(right))

          check(read.tail, newBranches)
        }

      check(read, graph.branches)
    }

    "produce empty path when graph is empty" in {
      DeBruijnGraph(LazyList(), 3).path shouldBe empty
    }

    "produce empty path if read does not contain cycle" in {
      DeBruijnGraph(LazyList("ABCDE"), 3).path shouldBe empty
    }

    "produce path equal to any of provided read rotations if read contains cycle" in {
      val k             = 3
      val initialRead   = "ABABE"
      val readWithCycle = initialRead ++ initialRead.take(k - 1)
      val graph         = DeBruijnGraph(LazyList(readWithCycle), k)

      graph.path should beOneOf(rotations(initialRead))
    }

    "optimize empty graph to empty graph" in {
      val graph     = DeBruijnGraph(LazyList(), 3)
      val optimized = graph.optimized

      optimized.nodes shouldBe empty
      optimized.branches shouldBe empty
    }

    "correctly optimize non-empty graph" in {
      val read      = "ABCDEFG"
      val k         = 3
      val optimized = DeBruijnGraph(LazyList(read), k).optimized

      optimized.nodes shouldBe Set(read take k - 1, read takeRight k - 1)
      optimized.branches shouldBe Map((read take k - 1, List(read drop 1)))
    }
  }
}

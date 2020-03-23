package graph

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class DeBruijnGraphSuite extends AnyWordSpec with Matchers {
  "DeBruijnGraph" should {
    "throw IllegalArgumentException when provided k is less than 2" in {
      an[IllegalArgumentException] should be thrownBy new DeBruijnGraph(LazyList("ACGTCGA"), 0)
    }

    "produce empty graph when empty list provided" in {
      val graph = new DeBruijnGraph(LazyList.empty, 5)
      graph.nodes shouldBe empty
      graph.branches shouldBe empty
    }

    "produce empty graph when k is bigger than any provided read's length" in {
      val graph = new DeBruijnGraph(LazyList("ACGTCGA"), 10)
      graph.nodes shouldBe empty
      graph.branches shouldBe empty
    }

    "produce all k-1-mers as nodes from provided reads" in {
      val k     = 3
      val read  = "ACGTACGATGCA"
      val nodes = new DeBruijnGraph(LazyList(read), k).nodes

      (0 to read.length - k + 1) foreach (i => nodes should contain(read.substring(i, i + k - 1)))
    }

    "produce all k-mers as branches from provided reads" in {
      val k        = 3
      val read     = "ACGTACGATGCA"
      val branches = new DeBruijnGraph(LazyList(read), k).branches

      (0 to read.length - k) foreach { i =>
        val kmer = read.substring(i, i + k)
        branches(kmer take k - 1) should contain(kmer takeRight k - 1)
      }
    }

    "contain walk equal to initial read" in {
      val k     = 3
      val read  = "ACGTACGATGCA"
      val graph = new DeBruijnGraph(LazyList(read), k)

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
  }
}

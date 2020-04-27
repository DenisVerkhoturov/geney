package graph

class DeBruijnGraph(reads: LazyList[String], k: Int) {
  require(k > 1, "k must be more than 1")

  val (nodes, branches) = buildGraph(reads flatMap kmers, Set(), Map() withDefaultValue List())

  lazy val path: String = {
    def findPath(acc: String, branches: Map[String, List[String]]): String =
      if (branches.isEmpty) {
        if ((acc take k - 1) == (acc takeRight k - 1)) acc dropRight k - 1
        else ""
      } else {
        val left = acc takeRight k - 1
        val paths = for {
          right <- branches(left).to(LazyList)
          path = findPath(acc + right.last, deleteBranch(branches, (left, right)))
          if path.nonEmpty
        } yield path

        if (paths.isEmpty) ""
        else paths.head
      }

    if (nodes.nonEmpty) findPath(nodes.head, branches)
    else ""
  }

  private def kmers(read: String): LazyList[String] =
    if (read.length < k) LazyList.empty
    else (read take k) #:: kmers(read.tail)

  private def buildGraph(kmers: LazyList[String],
                         nodes: Set[String],
                         branches: Map[String, List[String]]): (Set[String], Map[String, List[String]]) =
    kmers match {
      case LazyList() => (nodes, branches)
      case kmer #:: rest => {
        val left  = kmer take k - 1
        val right = kmer takeRight k - 1
        buildGraph(rest, nodes incl left incl right, branches updated (left, right :: branches(left)))
      }
    }

  private def deleteBranch(branches: Map[String, List[String]], branch: (String, String)) = {
    val (left, right) = branch
    val leftBranches  = branches(left)

    if (leftBranches.size == 1) branches - left
    else branches updated (left, leftBranches diff List(right))
  }
}

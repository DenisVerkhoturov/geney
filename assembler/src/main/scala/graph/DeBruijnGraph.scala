package graph

case class DeBruijnGraph private (nodes: Set[String], branches: Map[String, List[String]], k: Int) {
  lazy val path: String = {
    def findPath(acc: String, branches: Map[String, List[String]], stack: List[String]): String = stack match {
      case List() => acc dropRight k - 1
      case x :: xs =>
        val left = x takeRight k - 1
        if (branches(left).isEmpty) {
          val next = if (xs.isEmpty) x else x drop k - 2
          findPath(next ++ acc, branches, xs)
        } else {
          val right = branches(left).head
          findPath(acc, deleteBranch(branches, (left, right)), right :: stack)
        }
    }

    if (nodes.isEmpty) ""
    else {
      val start = nodes.find(x => branches.count(leadsToNode(x)) < branches(x).length).getOrElse(nodes.head)
      val end   = nodes.find(x => branches.count(leadsToNode(x)) > branches(x).length).getOrElse(nodes.head)

      val branchesWithCycle = branches updated (end, ((end takeRight k - 2) + start) :: branches(end))

      findPath("", branchesWithCycle, List(start))
    }
  }

  lazy val optimized: DeBruijnGraph = {
    def optimize(nodes: Set[String], branches: Map[String, List[String]]): (Set[String], Map[String, List[String]]) =
      branches.find { case (left, rights) => (rights.length == 1) && branches.exists(leadsToNode(left)) } match {
        case Some((node, List(branch))) =>
          val newBranch   = node ++ (branch drop k - 2)
          val newBranches = (branches - node) map replaceNode(newBranch)
          optimize(nodes - node, newBranches)
        case None => (nodes, branches)
      }

    val (newNodes, newBranches) = optimize(nodes, branches)
    new DeBruijnGraph(newNodes, newBranches withDefaultValue List(), k)
  }

  private def leadsToNode(node: String)(branches: (String, List[String])): Boolean =
    branches match {
      case (_, rights) =>
        rights.exists(_.endsWith(node))
    }

  private def replaceNode(newBranch: String)(branch: (String, List[String])): (String, List[String]) =
    branch match {
      case (left, rights) =>
        left -> rights.map(branch => {
          if ((branch takeRight k - 1) == (newBranch take k - 1)) branch ++ (newBranch drop k - 1)
          else branch
        })
    }

  private def deleteBranch(branches: Map[String, List[String]], branch: (String, String)) = {
    val (left, right) = branch
    val leftBranches  = branches(left)

    if (leftBranches.size == 1) branches - left
    else branches updated (left, leftBranches diff List(right))
  }
}

object DeBruijnGraph {
  def apply(reads: LazyList[String], k: Int): DeBruijnGraph = {
    require(k > 1, "k must be more than 1")

    val (nodes, branches) = buildGraph(reads flatMap kmers(k), Set(), Map() withDefaultValue List(), k: Int)
    DeBruijnGraph(nodes, branches, k)
  }

  private def kmers(k: Int)(read: String): LazyList[String] =
    if (read.length < k) LazyList.empty
    else (read take k) #:: kmers(k)(read.tail)

  private def buildGraph(kmers: LazyList[String],
                         nodes: Set[String],
                         branches: Map[String, List[String]],
                         k: Int): (Set[String], Map[String, List[String]]) =
    kmers match {
      case LazyList() => (nodes, branches)
      case kmer #:: rest =>
        val left  = kmer take k - 1
        val right = kmer takeRight k - 1
        buildGraph(rest, nodes incl left incl right, branches updated (left, right :: branches(left)), k)
    }
}

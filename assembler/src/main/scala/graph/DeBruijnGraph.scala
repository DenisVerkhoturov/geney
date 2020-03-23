package graph

class DeBruijnGraph(reads: LazyList[String], k: Int) {
  require(k > 1, "k must be more than 1")

  val (nodes, branches) = buildGraph(reads flatMap kmers, Set(), Map() withDefaultValue List())

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
}

package fasta

import org.scalatest.concurrent.TimeLimitedTests
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{Seconds, Span}
import org.scalatest.wordspec.AnyWordSpec

class FastaSpec extends AnyWordSpec with Matchers with TimeLimitedTests {
  override def timeLimit: Span = Span(10, Seconds)

  private val fastaSeq = Seq(
    ">MCHU - Calmodulin - Human, rabbit, bovine, rat, and chicken",
    "ADQLTEEQIAEFKEAFSLFDKDGDGTITTKELGTVMRSLGQNPTEAELQDMINEVDADGNGTID",
    "FPEFLTMMARKMKDTDSEEEIREAFRVFDKDGNGYISAAELRHVMTNLGEKLTDEEVDEMIREA",
    "DIDGDGQVNYEEFVQMMTAK*"
  )

  private val record = Record(
    ">MCHU - Calmodulin - Human, rabbit, bovine, rat, and chicken",
    "ADQLTEEQIAEFKEAFSLFDKDGDGTITTKELGTVMRSLGQNPTEAELQDMINEVDADGNGTID" +
    "FPEFLTMMARKMKDTDSEEEIREAFRVFDKDGNGYISAAELRHVMTNLGEKLTDEEVDEMIREA" +
    "DIDGDGQVNYEEFVQMMTAK*"
  )

  private val fastaSeq80 = Seq(
    ">MCHU - Calmodulin - Human, rabbit, bovine, rat, and chicken",
    "ADQLTEEQIAEFKEAFSLFDKDGDGTITTKELGTVMRSLGQNPTEAELQDMINEVDADGNGTIDFPEFLTMMARKMKDTD",
    "SEEEIREAFRVFDKDGNGYISAAELRHVMTNLGEKLTDEEVDEMIREADIDGDGQVNYEEFVQMMTAK*"
  )

  "Fasta" should {
    "produce record with description when fasta sequence provided" in {
      Fasta.read(fastaSeq) shouldEqual Right(record)
    }

    "produce fasta sequence when record provided" in {
      Fasta.show(record) shouldEqual (fastaSeq80)
    }

    "produce sequence of records when more than one fasta sequence provided" in {
      val mergedSeq = fastaSeq ++ fastaSeq
      Fasta.reads(mergedSeq) shouldEqual (Right(Seq(record, record)))
    }

    "produce sequence of fasta sequence when sequence of records provided" in {
      val seqOfRecrods = Seq(record, record)
      Fasta.shows(seqOfRecrods) shouldEqual (fastaSeq80 ++ fastaSeq80)
    }

    "return left value when read empty fasta sequence" in {
      Fasta.read(Seq()) shouldEqual Left("Input line is empty")
    }
  }
}

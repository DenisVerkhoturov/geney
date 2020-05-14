package fastq

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class FastqSpec extends AnyWordSpec with Matchers {
  private val fastqDescription = "SEQ_ID"
  private val fastqData        = "GATTTGGGGTTCAAAGCAGTATCGATCAAATAGTAAATCCATTTGTTCAACTCACAGTTT"
  private val fastqQuality     = "!''*((((***+))%%%++)(%%%%).1***-+*''))**55CCF>>>>>>CCCCCCC65"

  private val fastqRecord         = Record(fastqDescription, fastqData, fastqQuality)
  private val fastqStringSequence = Seq("@" + fastqDescription, fastqData, "+", fastqQuality)

  "Fastq" should {
    "produce valid fastq string representation from record" in {
      Fastq.show(fastqRecord) shouldBe fastqStringSequence
    }

    "produce set of valid fastq strings from sequence of fastq records" in {
      val sequenceOfRecords    = Seq(fastqRecord, fastqRecord)
      val fastqStringSequences = fastqStringSequence ++ fastqStringSequence

      Fastq.shows(sequenceOfRecords) shouldBe fastqStringSequences
    }

    "produce Left of \"No input lines provided\" when empty string sequence provided for single record" in {
      Fastq.read(Seq()) shouldBe Left("No input lines provided")
    }

    "produce Left of \"Incorrect format\" when incorrectly formatted sequence of strings is provided" in {
      val incorrectData = Seq("Poorly formatted data")

      Fastq.read(incorrectData) shouldBe Left("Incorrect format")
    }

    "produce Right of fastq Record when valid sequence of strings is provided" in {
      Fastq.read(fastqStringSequence) shouldBe Right(fastqRecord)
    }

    "produce Left of \"No input lines provided\" when empty string sequence provided for multiple records" in {
      Fastq.reads(Seq()) shouldBe Left("No input lines provided")
    }

    "produce Left of \"Can't parse all records\" when one of provided string sequences is incorrectly formatted" in {
      val sequenceWithIncorrectData = fastqStringSequence ++ Seq("Poorly formatted data")

      Fastq.reads(sequenceWithIncorrectData) shouldBe Left("Can't parse all records")
    }

    "produce Right of sequence of fastq Records when valid sequence of strings is provided" in {
      val fastqStringSequences = fastqStringSequence ++ fastqStringSequence
      val sequenceOfRecords    = Seq(fastqRecord, fastqRecord)

      Fastq.reads(fastqStringSequence ++ fastqStringSequence) shouldBe Right(Seq(fastqRecord, fastqRecord))
    }
  }
}

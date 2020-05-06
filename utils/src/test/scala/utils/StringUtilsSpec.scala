package utils

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import StringUtils.rotations

class StringUtilsSpec extends AnyWordSpec with Matchers {
  "rotations should" should {
    "produce empty sequence if string is empty" in {
      rotations("") shouldBe empty
    }

    "produce all valid rotations of non empty string" in {
      val string = "ABCDE"

      rotations(string) shouldBe Seq("ABCDE", "BCDEA", "CDEAB", "DEABC", "EABCD")
    }
  }
}

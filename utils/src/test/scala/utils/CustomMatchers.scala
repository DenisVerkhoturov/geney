package utils

import org.scalatest.matchers.{MatchResult, Matcher}

import scala.reflect.ClassTag

trait CustomMatchers {

  /**
   * Enables the following syntax
   * `result should beOneOf (Seq(1, 2, 3))`
   */
  def beOneOf[T: ClassTag](variants: Seq[T]): Matcher[T] = Matcher { result: T =>
    MatchResult(
      matches = variants.contains(result),
      rawFailureMessage = s"$result was not one of ${variants.mkString("[", ", ", "]")}",
      rawNegatedFailureMessage = s"$result was one of ${variants.mkString("[", ", ", "]")}"
    )
  }

  /**
   * Enables the following syntax
   * `result should beOneOf (1, 2, 3)`
   */
  def beOneOf[T: ClassTag](first: T, rest: T*): Matcher[T] = beOneOf(first +: rest)
}

object CustomMatchers extends CustomMatchers

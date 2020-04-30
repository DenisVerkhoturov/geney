package utils

trait StringUtils {
  def rotations(xs: String): Seq[String] = xs.indices.map(i => xs.drop(i) ++ xs.take(i))
}

object StringUtils extends StringUtils

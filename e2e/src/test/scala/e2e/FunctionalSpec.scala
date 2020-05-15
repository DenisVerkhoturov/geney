package e2e

import java.io.{ByteArrayOutputStream, File, FileWriter, StringReader}
import java.nio.file.{AccessDeniedException, Files, Path}
import java.nio.file.attribute.PosixFilePermissions

import org.scalatest.concurrent.TimeLimitedTests
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{Seconds, Span}
import org.scalatest.wordspec.AnyWordSpec
import parser.Main

import scala.io.{BufferedSource, Source}

class FunctionalSpec extends AnyWordSpec with Matchers with TimeLimitedTests {
  override def timeLimit: Span = Span(10, Seconds)
  trait Files {
    val inputFile: File = File.createTempFile("input", "txt");
    val outputFile: File = File.createTempFile("invalid", "txt");

    val writer = new FileWriter(inputFile);
    val source: BufferedSource = scala.io.Source.fromFile(outputFile)
  }
  "Arguments for Cli parser" when {
    "help mode" should {
      "show usage-message when no arguments provided" in {
        pendingUntilFixed {
          val args = new Array[String](0);
          Main.main(args);
          val input = new StringReader("")
          val out = new ByteArrayOutputStream()
          Console.withIn(input) {
            Console.withOut(out) {
              Main.main(args);
            }
          }
          out.toString shouldBe("java -jar target/scala-2.13/geney.jar <options>")
        }
      }
      /*"show help-message when is called with option -h or --help" in new Files {
        pendingUntilFixed {
          val args: Array[String] = Array("-h", "--input", inputFile.getAbsolutePath);
          val in = new StringReader("")
          val out = new ByteArrayOutputStream()
          Console.withIn(in) {
            Console.withOut(out) {
              Main.main(args);
            }
          }
          out.toString should(include("CliParser") and
            include("k is an  integer property, the length of k that will be used to build De Bruijn graph") and
            include("input file with reads to be analyzed") and
            include("output file to write the result to") and
            include("show usage"))
        }
      }*/
    }
    "Incorrect argument -k" should {
      "show error-message when argument -k is not provided" in new Files {
        pendingUntilFixed {
          the[IllegalArgumentException] thrownBy {
            val args = Array("--input", inputFile.getAbsolutePath, "--output", "output.txt")
            Main.main(args);
          } should have message "Missing option -k"
        }
      }
      "show error-message when argument -k is not positive number" in new Files {
        pendingUntilFixed {
          the[IllegalArgumentException] thrownBy {
            val args = Array("--input", inputFile.getAbsolutePath, "-k", "-1")
            Main.main(args);
          } should have message "Argument -k must be a positive number"
        }
      }
      "show error-message when argument -k is not a number" in new Files {
        pendingUntilFixed {
          the[IllegalArgumentException] thrownBy {
            val args = Array("--input", inputFile.getAbsolutePath, "-k", "something")
            Main.main(args);
          } should have message "Argument -k must be a positive number"
        }
      }
    }
    "Incorrect argument --format" should {
      "show error-message when argument -f is not provided" in new Files {
        pendingUntilFixed {
          the[IllegalArgumentException] thrownBy {
            val args = Array("--input", inputFile.getAbsolutePath, "-k", "10")
            Main.main(args);
          } should have message "Missing option -f"
        }
      }
      "show error-message when argument -f is defined as fasta but content does not correspond" in new Files {
        pendingUntilFixed {
          the[IllegalArgumentException] thrownBy {
            val args = Array("--input", inputFile.getAbsolutePath, "-k", "10", "-f", "fasta")
            writer.write("ATGC")
            writer.close()
            Main.main(args);
          } should have message "Incorrect format of input file."
        }
      }
      "show error-message when argument -f is defined as fastq but content does not correspond" in new Files {
        pendingUntilFixed {
          the[IllegalArgumentException] thrownBy {
            val args = Array("--input", inputFile.getAbsolutePath, "-k", "10", "-f", "fastq")
            writer.write("ATGC")
            writer.close()
            Main.main(args);
          } should have message "Incorrect format of input file."
        }
      }
    }
    "Incorrect argument --input" should {
      "show error-message when input file does not exist" in {
        pendingUntilFixed {
          the[IllegalArgumentException] thrownBy {
            val args = Array("--input", "PathToFileThatDoesNotExist", "-k", "10")
            Main.main(args);
          } should have message "Input file does not exist."
        }
      }
      "show error-message when user does not have access to input file" in new Files {
         pendingUntilFixed {
          inputFile.setReadable(false);
          the[AccessDeniedException] thrownBy {
            val args = Array("--input", inputFile.getAbsolutePath, "-k", "10")
            Main.main(args);
          } should have message "Access denied"
        }
      }
      "show error-message when path to input file is a directory" in new Files {
        pendingUntilFixed {
          the[IllegalArgumentException] thrownBy {
            val args = Array("--input", inputFile.getParent, "-k", "10")
            Main.main(args);
          } should have message "Path to input file is a directory."
        }
      }
    }
    "Incorrect argument --output" should {
      "show error-message when output file already exists" in new Files {
        pendingUntilFixed {
          the[IllegalArgumentException] thrownBy {
            val args = Array("--input", inputFile.getAbsolutePath, "--output", outputFile.getAbsolutePath, "-k", "10")
            Main.main(args);
          } should have message "Output file already exists."
        }
      }
      "show error-message when user does not have access to output file" in new Files {
        pendingUntilFixed {
          val ownerWritable = PosixFilePermissions.fromString("---------")
          val permissions = PosixFilePermissions.asFileAttribute(ownerWritable)
          val dirWithNoAccess = Files.createTempDirectory("dirWithNoAccess", permissions).toFile
          the[AccessDeniedException] thrownBy {
            val args = Array("--input", inputFile.getAbsolutePath, "--output", dirWithNoAccess.getAbsolutePath + "/output.txt", "-k", "10")
            Main.main(args);
          } should have message "Access denied"
        }
      }
      "show error-message when path to output file is a directory" in new Files {
        pendingUntilFixed {
          the[IllegalArgumentException] thrownBy {
            val args = Array("--input", inputFile.getAbsolutePath, "--output", outputFile.getParent, "-k", "10")
            Main.main(args);
          } should have message "Path to input file is a directory."
        }
      }
    }
    "Correct arguments" should {
      "use stdout when argument --output is not provided" in new Files {
         pendingUntilFixed {
          val args = Array("--input", inputFile.getAbsolutePath, "-k", "10", "-f", "fasta")
          Main.main(args);
          writer.write(">id description\n ABCDE");
          writer.close();
          val out = new ByteArrayOutputStream()
          Console.withOut(out) {
            Main.main(args);
          }
          out.toString shouldBe("")
        }
      }
      "use stdin when argument --input is not provided" in new Files {
        pendingUntilFixed {
          val outputPath = inputFile.getParent + "/output.txt";
          val args = Array("--output", outputPath, "-k", "10", "-f", "fasta")
          Main.main(args);
          val in = new StringReader(">id description\n ABCDE")
          Console.withIn(in) {
            Main.main(args);
          }
          source.getLines().toString() shouldBe("")
        }
      }
    }
  }
}

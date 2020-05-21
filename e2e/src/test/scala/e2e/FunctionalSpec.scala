package e2e

import java.io.{ByteArrayOutputStream, File, FileWriter, IOException, StringReader}
import java.nio.file.{Files, Path}
import java.nio.file.attribute.{AclEntry, AclEntryPermission, AclEntryType, AclFileAttributeView, PosixFilePermissions}

import org.scalatest.Assertion
import org.scalatest.concurrent.TimeLimitedTests
import org.scalatest.matchers.should
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{Seconds, Span}
import org.scalatest.wordspec.AnyWordSpec
import parser.Main

import scala.io.BufferedSource

class FunctionalSpec extends AnyWordSpec with Matchers with TimeLimitedTests {
  override def timeLimit: Span = Span(10, Seconds)
  trait Files {
    val inputFile: File = File.createTempFile("input", "txt");
    val outputFile: File = File.createTempFile("invalid", "txt");

    val writer = new FileWriter(inputFile);
    val source: BufferedSource = scala.io.Source.fromFile(outputFile)
  }
  def checkWithIncorrectArguments(args: Array[String], errorMessage: String) = {
    val out = new ByteArrayOutputStream()
    Console.withErr(out) {
      Main.main(args);
    }
    out.toString should (include(errorMessage))
  }

  @throws[IOException]
  def inaccessible(path: Path): Path = {
    val os = System.getProperty("os.name").toLowerCase
    if (os.contains("win")) {
      val currentUser = path.getFileSystem.getUserPrincipalLookupService.lookupPrincipalByName(System.getProperty("user.name"))
      val view = Files.getFileAttributeView(path, classOf[AclFileAttributeView])
      val denyReadAndWrite = AclEntry.newBuilder.setType(AclEntryType.DENY).setPrincipal(currentUser).setPermissions(AclEntryPermission.READ_DATA, AclEntryPermission.ADD_FILE).build
      val acl = view.getAcl
      acl.add(0, denyReadAndWrite)
      view.setAcl(acl)
    }
    else {
        path.toFile.setReadable(false)
        path.toFile.setWritable(false)
    }
    path
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
         val args = Array("--input", inputFile.getAbsolutePath, "--output", "output.txt", "-f", "fasta")
         checkWithIncorrectArguments(args, "Missing option --k")
      }
      "show error-message when argument -k is not positive number" in new Files {
        val args = Array("--input", inputFile.getAbsolutePath, "-k", "-1", "-f", "fasta")
        checkWithIncorrectArguments(args, "Argument -k must be >2")
      }
      "show error-message when argument -k is not a number" in new Files {
        val args = Array("--input", inputFile.getAbsolutePath, "-k", "something", "-f", "fasta")
        checkWithIncorrectArguments(args, "Option --k expects a number")
      }
    }
    "Incorrect argument --format" should {
      "show error-message when argument -f is not provided" in new Files {
        val args = Array("--input", inputFile.getAbsolutePath, "-k", "10")
        checkWithIncorrectArguments(args, "Missing option --format")
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
      "show error-message when input file does not exist" in new Files {
        val args = Array("--input", "PathToFileThatDoesNotExist", "-k", "10", "-f", "fasta")
        checkWithIncorrectArguments(args, "Input file doesn't exist")
      }
      "show error-message when user does not have access to input file" in new Files {
        val inaccessiblePath = inaccessible(inputFile.toPath).toFile.getAbsolutePath
        val args = Array("--input", inaccessiblePath, "-k", "10", "-f", "fasta")
        checkWithIncorrectArguments(args, "Access to input file is denied")
      }
      "show error-message when path to input file is a directory" in new Files {
        val args = Array("--input", inputFile.getParent, "-k", "10", "-f", "fasta")
        checkWithIncorrectArguments(args, "Path to input file is a directory")
      }
    }
    "Incorrect argument --output" should {
      "show error-message when output file already exists" in new Files {
        val args = Array("--input", inputFile.getAbsolutePath, "--output", outputFile.getAbsolutePath, "-k", "10", "-f", "fasta")
        checkWithIncorrectArguments(args, "Output file already exists")
      }
      "show error-message when user does not have access to output file" in new Files {
        val inaccessiblePath = inaccessible(Files.createTempDirectory("dirWithNoAccess")).toFile.getAbsolutePath
        val args = Array("--input", inputFile.getAbsolutePath, "--output", inaccessiblePath + "/output.txt", "-k", "10", "-f", "fasta")
        checkWithIncorrectArguments(args, "Access to output file is denied")
      }
      "show error-message when path to output file is a directory" in new Files {
        val args = Array("--input", inputFile.getAbsolutePath, "--output", outputFile.getParent, "-k", "10", "-f", "fasta")
        checkWithIncorrectArguments(args, "Path to output file is a directory")
      }
    }
    "Correct arguments" should {
      "use stdout when argument --output is not provided" in new Files {
         pendingUntilFixed {
          val args = Array("--input", inputFile.getAbsolutePath, "-k", "10", "-f", "fasta")
          Main.main(args);
          writer.write(">id description\n ABCDE");
          writer.close();val out = new ByteArrayOutputStream()
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

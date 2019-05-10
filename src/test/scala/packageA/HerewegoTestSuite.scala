package packageA

import java.io.File

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterAll, FunSuite}
import Herewego._

@RunWith(classOf[JUnitRunner])
class HerewegoTestSuite extends FunSuite with BeforeAndAfterAll {

  trait Environment {
    implicit val spark: SparkSession = SparkSession.builder()
      .appName("simple-spark-app")
      .config("spark.master", "local")
      .enableHiveSupport()
      .getOrCreate()

    var fullFileName = ""
  }

  test("Test that the input file is loaded successfully") {
    new Environment {
      fullFileName = getResFullPath("../apache_log.txt").getOrElse("")

      val f: File = new File(fullFileName)
      assert(f.exists(), "Verify that the getResFullPath() function indeed returns a real file path")
    }
  }

  test("Test that the created RDD is of the Right Size, i.e. the length of the File") {
    new Environment {
      fullFileName = getResFullPath("../apache_log.txt").getOrElse("")

      val testRdd: RDD[String] = spark.sparkContext.textFile(fullFileName)
      assert(testRdd.collect().length === 10000, "Verify that the RDD is 10000 lines long")
    }
  }

}

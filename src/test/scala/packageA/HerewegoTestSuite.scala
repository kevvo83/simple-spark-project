package packageA

import java.io.File

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterAll, FunSuite}
import packageA.Defs._
import org.apache.spark.SparkContext

@RunWith(classOf[JUnitRunner])
class HerewegoTestSuite extends FunSuite with BeforeAndAfterAll {

  trait Environment {
    implicit val spark: SparkSession = SparkSession.builder()
      .appName("simple-spark-app")
      .config("spark.master", "local")
      .enableHiveSupport()
      .getOrCreate()
    val sc: SparkContext = spark.sparkContext

    var fullFileName = ""
  }

  test("Test that the input file is loaded successfully") {
    new Environment {
      fullFileName = getResFullPath("/apache_log.txt").getOrElse("")

      val f: File = new File(fullFileName)
      assert(f.exists(), "Verify that the getResFullPath() function indeed returns a real file path")
    }
  }

  test("Test that the created RDD is of the Right Size, i.e. the length of the File") {
    new Environment {
      fullFileName = getResFullPath("/apache_log.txt").getOrElse("")

      val testRdd: RDD[String] = sc.textFile(fullFileName)
      assert(testRdd.collect().length === 10000, "Verify that the RDD is 10000 lines long")
    }
  }

}

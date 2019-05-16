package packageA

import java.io.Serializable
import java.nio.file.Paths

import scala.util.matching
import org.apache.spark.rdd.RDD
import org.apache.spark.sql._
import org.apache.spark.sql.types._

import scala.util.matching.Regex
import java.util.Date
import java.sql.Timestamp

import org.apache.commons.net.ntp.TimeStamp

object Herewego extends App {

  def getResFullPath(resourceName: String): Option[String] = {
    try {
      Some(Paths.get(getClass.getResource(resourceName).toURI).toString)
    } catch {
      case exp: Exception => {
        println(exp)}
        None
    }
  }

  val hiveWarehouseLocation:String = args(0)
  val sparkMasterURL:String = args(1)

  val spark: SparkSession = SparkSession.builder()
    .appName("revise-sparkapp")
    .config("spark.master", s"${sparkMasterURL}")
    .config("spark.sql.warehouse.dir", s"${hiveWarehouseLocation}")
    .enableHiveSupport()
    .getOrCreate()

  val rdd1 = spark.sparkContext.textFile(getResFullPath("/apache_log.txt").getOrElse(""),
    4)

  val rx: Regex = """^(\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}) \- \- \[(\d{2}\/\w+\/\d{4})\:(\d{2}\:\d{2}\:\d{2} \+\d{4}).*""".r

  def createrow(a: List[String]): Row = {

    val format = new java.text.SimpleDateFormat("dd/MMM/yyyy HH:mm:ss Z")

    val re1: Row = Row.apply(a.head)

    val d: Date = format.parse(a.tail.mkString(" "))
    val t = new Timestamp(d.getTime)
    val re2: Row = Row.apply(t)

    Row.merge(re1, re2)
  }

  // Map vs. MapPartition Comparison
  val rdd2 = rdd1.map(
    l => l match {
      case rx(ipadd, date, time) => (ipadd, date, time)
      case _ => "Not Matched"
    }
  )

  // Map vs. MapPartition Comparison
  val rdd3 = rdd1.mapPartitions((it:Iterator[String]) => {
    it.toList.map(
      l => l match {
        case rx(ipadd, date, time) => List(ipadd, date, time).mkString(";")
        case _ => l
      }
    ).toIterator
  }).map(_.split(";").toList).map(createrow(_))

  val schemaFields: Array[StructField] = Array(
    StructField("ipadd", StringType, false),
    StructField("timestamp", TimestampType ,false)
  )

  val dfSchema: StructType = StructType(schemaFields)
  val data: DataFrame = spark.createDataFrame(rdd3, dfSchema)

  //println(data.collect().length)

  data.createTempView("temp_view")
  spark.sql("DROP TABLE IF EXISTS apache_access_info")
  spark.sql(s"""CREATE EXTERNAL TABLE apache_access_info
             ROW FORMAT DELIMITED FIELDS TERMINATED BY ';'
             LOCATION '${hiveWarehouseLocation}'
             AS
             SELECT * FROM temp_view""")

  spark.sql("DROP TABLE IF EXISTS access_access_consolidated_info")
  spark.sql("describe apache_access_info").show()

  if (false) {
    spark.sql("DROP TABLE IF EXISTS access_access_consolidated_info")
    spark.sql("""CREATE TABLE access_access_consolidated_info
       ROW FORMAT DELIMITED FIELDS TERMINATED BY ';'
       AS
       SELECT ipadd, COUNT(timestamp) as access_count FROM apache_access_info
       GROUP BY ipadd
       CLUSTER BY access_count""")
    spark.sql("SELECT * FROM access_access_consolidated_info ORDER BY access_count DESC LIMIT 20").show()
  }

  spark.close()
}

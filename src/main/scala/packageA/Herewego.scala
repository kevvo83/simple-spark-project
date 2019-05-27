package packageA

import org.apache.spark.rdd.RDD
import org.apache.spark.sql._
import org.apache.spark.sql.types._
import org.apache.spark.SparkContext
import org.apache.spark.storage.StorageLevel._

import scala.util.matching.Regex

import packageA.Defs._

object Herewego extends App {

  val inputFileLocation:String = args(0)
  val hiveWarehouseLocation:String = args(1)
  val sparkMasterURL:String = args(2)

  val s3a_access_key:String = Option(args(3)).getOrElse("")
  val s3a_secret_key:String = Option(args(4)).getOrElse("")

  val spark: SparkSession = SparkSession.builder()
    .appName("revise-sparkapp")
    .config("spark.master", s"${sparkMasterURL}")
    .config("spark.sql.warehouse.dir", s"${hiveWarehouseLocation}")
    .enableHiveSupport()
    .getOrCreate()
  val sc: SparkContext = spark.sparkContext

  val hadoopConf = sc.hadoopConfiguration
  hadoopConf.set("fs.s3a.aws.credentials.provider", "org.apache.hadoop.fs.s3a.SimpleAWSCredentialsProvider")
  hadoopConf.set("fs.s3a.access.key", s"${s3a_access_key}")
  hadoopConf.set("fs.s3a.secret.key", s"${s3a_secret_key}")

  val rdd1 = sc.textFile(s"${inputFileLocation}", 4)


  val rdd4: RDD[Row] = rdd1.mapPartitions((it:Iterator[String]) => {

    val rx: Regex = """^(\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}) \- \- \[(\d{2}\/\w+\/\d{4})\:(\d{2}\:\d{2}\:\d{2} \+\d{4}).*""".r

    var output: List[Row] = List()
    while (it.hasNext) {
      val data: String = it.next()
      val res = data match {
        case rx(ipadd, date, time) => createrow(List(ipadd, date, time))
        case _ => createrow(List("0.0.0.0", "00/Jan/0000", "00:00:00 0"))
      }
      output = output :+ res
    }
    output.toIterator
  }).persist(MEMORY_ONLY)

  // Collect and Persist the RDD in Memory
  val tmp = rdd4.collect()

  // Print the Explain plan of RDD4
  rdd4.toDebugString

  // Define Fields for Schema for DataFrame
  val schemaFields: Array[StructField] = Array(
    StructField("ipadd", StringType, false),
    StructField("timestamp", TimestampType ,false)
  )

  // Define Schema and create DataFrame
  val dfSchema: StructType = StructType(schemaFields)
  val data: DataFrame = spark.createDataFrame(rdd4, dfSchema).cache()

  data.createTempView("temp_view")
  spark.sql("DROP TABLE IF EXISTS apache_access_info")
  spark.sql(s"""CREATE EXTERNAL TABLE apache_access_info
             ROW FORMAT DELIMITED FIELDS TERMINATED BY ';'
             LOCATION '${hiveWarehouseLocation}'
             AS
             SELECT * FROM temp_view""")

  spark.sql("DROP TABLE IF EXISTS access_access_consolidated_info")
  spark.sql("describe apache_access_info").show()
  spark.sql("SELECT * FROM apache_access_info").show(20)

  spark.sql("DROP TABLE IF EXISTS access_access_consolidated_info")
  spark.sql("""CREATE TABLE access_access_consolidated_info
     ROW FORMAT DELIMITED FIELDS TERMINATED BY ';'
     AS
     SELECT ipadd, COUNT(timestamp) as access_count FROM apache_access_info
     GROUP BY ipadd
     CLUSTER BY access_count""")
  spark.sql("SELECT * FROM access_access_consolidated_info ORDER BY access_count DESC LIMIT 20").show()

  spark.close()
}

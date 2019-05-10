import sbt._

object Dependencies {

  val clusterLibraryDependencies = Seq(
    "junit" % "junit" % "4.12" % Test,
    "org.apache.spark" %% "spark-core" % "2.4.2" % "provided",
    "org.apache.spark" %% "spark-sql" % "2.4.2" % "provided",
    "org.apache.spark" %% "spark-hive" % "2.4.2" % "provided",
    "org.scalatest" %% "scalatest" % "3.0.3" % Test
  )

  val localLibraryDependencies = Seq(
    "junit" % "junit" % "4.12" % Test,
    "org.apache.spark" %% "spark-core" % "2.4.2",
    "org.apache.spark" %% "spark-sql" % "2.4.2",
    "org.apache.spark" %% "spark-hive" % "2.4.2",
    "org.scalatest" %% "scalatest" % "3.0.3" % Test
  )

  /*
  libraryDependencies ++= Seq(
  "junit" % "junit" % "4.12" % Test,
  "org.apache.spark" %% "spark-core" % "2.4.2" % "provided",
  "org.apache.spark" %% "spark-sql" % "2.4.2" % "provided",
  "org.apache.spark" %% "spark-hive" % "2.4.2" % "provided",
  "org.scalatest" %% "scalatest" % "3.0.3" % Test
  )
  */
}
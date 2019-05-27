package packageA

import java.nio.file.Paths
import java.sql.Timestamp
import java.util.Date
import org.apache.spark.sql.Row

package object Defs {

  def getResFullPath(resourceName: String): Option[String] = {
    try {
      Some(Paths.get(getClass.getResource(resourceName).toURI).toString)
    } catch {
      case exp: Exception => {
        println(exp)}
        None
    }
  }

  def createrow(a: List[String]): Row = {

    val format = new java.text.SimpleDateFormat("dd/MMM/yyyy HH:mm:ss Z")

    val re1: Row = Row.apply(a.head)

    val d: Date = format.parse(a.tail.mkString(" "))
    val t = new Timestamp(d.getTime)
    val re2: Row = Row.apply(t)

    Row.merge(re1, re2)
  }

}

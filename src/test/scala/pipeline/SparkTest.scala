package pipeline

import org.apache.spark.sql.SparkSession
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

/**
 * Base test class that provides Spark session for all tests
 */
trait SparkTest extends AnyFunSuite with BeforeAndAfterAll with Matchers {

  protected lazy val spark: SparkSession =
    SparkSession
      .builder()
      .appName("Test Spark Session")
      .master("local[2]")
      .config("spark.ui.enabled", "false")
      .config("spark.sql.shuffle.partitions", "2")
      .config("spark.sql.warehouse.dir", "target/spark-warehouse")
      .getOrCreate()

  override def beforeAll(): Unit = {
    super.beforeAll()
    spark.sparkContext.setLogLevel("ERROR")
  }

  override def afterAll(): Unit = {
    if (spark != null) {
      spark.stop()
    }
    super.afterAll()
  }

}

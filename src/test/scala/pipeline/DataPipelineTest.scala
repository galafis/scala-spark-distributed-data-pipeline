package pipeline

import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.sql.Row
import java.sql.Date

class DataPipelineTest extends SparkTest {

  test("createSparkSession should create a valid Spark session") {
    // Use the existing spark session from SparkTest trait to avoid stopping the shared context
    spark should not be null
    spark.sparkContext.appName should include("Test")
  }

  test("extractData should read CSV data correctly") {
    import spark.implicits._

    // Create test CSV file
    val testData = Seq(
      ("Order1", "2025-01-15", "Electronics", "Customer1", 100.0, 50.0, 2),
      ("Order2", "2025-01-16", "Books", "Customer2", 200.0, 80.0, 3)
    ).toDF("Order ID", "Order Date", "Category", "Customer ID", "Sales", "Profit", "Quantity")

    val tempPath = "target/test-data/csv-input"
    testData.write.mode("overwrite").option("header", "true").csv(tempPath)

    val result = DataPipeline.extractData(spark, tempPath, "csv")

    result.count() should be(2)
    result.columns should contain("Order ID")
    result.columns should contain("Category")
  }

  test("extractData should read Parquet data correctly") {
    import spark.implicits._

    val testData = Seq(
      ("Order1", "Electronics", 100.0),
      ("Order2", "Books", 200.0)
    ).toDF("Order ID", "Category", "Sales")

    val tempPath = "target/test-data/parquet-input"
    testData.write.mode("overwrite").parquet(tempPath)

    val result = DataPipeline.extractData(spark, tempPath, "parquet")

    result.count() should be(2)
    result.select("Sales").as[Double].collect() should contain allOf (100.0, 200.0)
  }

  test("extractData should read JSON data correctly") {
    import spark.implicits._

    val testData = Seq(
      """{"id": "Order1", "category": "Electronics", "sales": 100.0}""",
      """{"id": "Order2", "category": "Books", "sales": 200.0}"""
    ).toDF("value")

    val tempPath = "target/test-data/json-input"
    testData.write.mode("overwrite").text(tempPath)

    val result = DataPipeline.extractData(spark, tempPath, "json")

    result.count() should be(2)
    result.columns should contain("category")
  }

  test("extractData should throw exception for unsupported format") {
    assertThrows[IllegalArgumentException] {
      DataPipeline.extractData(spark, "dummy-path", "xml")
    }
  }

  test("transformData should remove duplicates") {
    import spark.implicits._

    val testData = Seq(
      ("Order1", Date.valueOf("2025-01-15"), "Electronics", "Customer1", 100.0, 50.0, 2),
      ("Order1", Date.valueOf("2025-01-15"), "Electronics", "Customer1", 100.0, 50.0, 2),
      ("Order2", Date.valueOf("2025-01-16"), "Books", "Customer2", 200.0, 80.0, 3)
    ).toDF("Order ID", "Order Date", "Category", "Customer ID", "Sales", "Profit", "Quantity")

    val result = DataPipeline.transformData(testData)

    result.count() should be(2)
  }

  test("transformData should handle null values correctly") {
    val schema = StructType(
      Seq(
        StructField("Order ID", StringType, nullable = true),
        StructField("Order Date", DateType, nullable = true),
        StructField("Category", StringType, nullable = true),
        StructField("Customer ID", StringType, nullable = true),
        StructField("Sales", DoubleType, nullable = true),
        StructField("Profit", DoubleType, nullable = true),
        StructField("Quantity", IntegerType, nullable = true)
      )
    )

    val testDataRows = Seq(
      Row("Order1", Date.valueOf("2025-01-15"), "Electronics", "Customer1", null, null, 1),
      Row("Order2", Date.valueOf("2025-01-16"), "Books", "Customer2", 200.0, 80.0, 3)
    )

    val testData = spark.createDataFrame(spark.sparkContext.parallelize(testDataRows), schema)
    val result   = DataPipeline.transformData(testData)

    val firstRow = result.filter(col("Order ID") === "Order1").first()
    firstRow.getAs[Double]("Sales") should be(0.0)
    firstRow.getAs[Double]("Profit") should be(0.0)
    firstRow.getAs[Int]("Quantity") should be(1)
  }

  test("transformData should add derived columns correctly") {
    import spark.implicits._

    val testData = Seq(
      ("Order1", Date.valueOf("2025-01-15"), "Electronics", "Customer1", 100.0, 50.0, 2),
      ("Order2", Date.valueOf("2025-06-16"), "Books", "Customer2", 200.0, 80.0, 3)
    ).toDF("Order ID", "Order Date", "Category", "Customer ID", "Sales", "Profit", "Quantity")

    val result = DataPipeline.transformData(testData)

    result.columns should contain allOf ("Year", "Month", "Quarter", "Profit Margin")

    val firstRow = result.filter(col("Order ID") === "Order1").first()
    firstRow.getAs[Int]("Year") should be(2025)
    firstRow.getAs[Int]("Month") should be(1)
    firstRow.getAs[Int]("Quarter") should be(1)
    firstRow.getAs[Double]("Profit Margin") should be(0.5)

    val secondRow = result.filter(col("Order ID") === "Order2").first()
    secondRow.getAs[Int]("Quarter") should be(2)
  }

  test("transformData should calculate profit margin correctly") {
    import spark.implicits._

    val testData = Seq(
      ("Order1", Date.valueOf("2025-01-15"), "Electronics", "Customer1", 100.0, 25.0, 1),
      ("Order2", Date.valueOf("2025-01-16"), "Books", "Customer2", 0.0, 0.0, 1),
      ("Order3", Date.valueOf("2025-01-17"), "Toys", "Customer3", 200.0, 100.0, 2)
    ).toDF("Order ID", "Order Date", "Category", "Customer ID", "Sales", "Profit", "Quantity")

    val result = DataPipeline.transformData(testData)

    val margins = result.select("Order ID", "Profit Margin").collect()

    val order1Margin = margins.find(_.getString(0) == "Order1").get.getDouble(1)
    order1Margin should be(0.25 +- 0.001)

    val order2Margin = margins.find(_.getString(0) == "Order2").get.getDouble(1)
    order2Margin should be(0.0)

    val order3Margin = margins.find(_.getString(0) == "Order3").get.getDouble(1)
    order3Margin should be(0.5 +- 0.001)
  }

  test("transformData should filter out invalid records") {
    import spark.implicits._

    val testData = Seq(
      ("Order1", Date.valueOf("2025-01-15"), "Electronics", "Customer1", 100.0, 50.0, 2),
      ("Order2", Date.valueOf("2025-01-16"), "Books", "Customer2", -100.0, 50.0, 2),
      ("Order3", Date.valueOf("2025-01-17"), "Toys", "Customer3", 100.0, 50.0, 0),
      ("Order4", Date.valueOf("2025-01-18"), "Sports", "Customer4", 150.0, 75.0, 3)
    ).toDF("Order ID", "Order Date", "Category", "Customer ID", "Sales", "Profit", "Quantity")

    val result = DataPipeline.transformData(testData)

    result.count() should be(2)
    result.select("Order ID").as[String].collect() should contain allOf ("Order1", "Order4")
  }

  test("aggregateData should group and aggregate correctly") {
    import spark.implicits._

    val testData = Seq(
      ("Order1", Date.valueOf("2025-01-15"), "Electronics", "Customer1", 100.0, 50.0, 2),
      ("Order2", Date.valueOf("2025-01-16"), "Electronics", "Customer2", 200.0, 80.0, 3),
      ("Order3", Date.valueOf("2025-04-15"), "Books", "Customer3", 150.0, 60.0, 1),
      ("Order4", Date.valueOf("2025-01-17"), "Electronics", "Customer4", 300.0, 120.0, 4)
    ).toDF("Order ID", "Order Date", "Category", "Customer ID", "Sales", "Profit", "Quantity")

    val transformed = DataPipeline.transformData(testData)
    val result      = DataPipeline.aggregateData(transformed)

    result.count() should be(2) // Electronics Q1 and Books Q2

    val electronicsQ1 = result.filter(col("Category") === "Electronics" && col("Quarter") === 1).first()
    electronicsQ1.getAs[Double]("Total Sales") should be(600.0)
    electronicsQ1.getAs[Double]("Total Profit") should be(250.0)
    electronicsQ1.getAs[Long]("Total Quantity") should be(9)
    electronicsQ1.getAs[Long]("Order Count") should be(3)
    electronicsQ1.getAs[Long]("Unique Customers") should be(3)
  }

  test("aggregateData should calculate average profit margin correctly") {
    import spark.implicits._

    val testData = Seq(
      ("Order1", Date.valueOf("2025-01-15"), "Electronics", "Customer1", 100.0, 20.0, 1),
      ("Order2", Date.valueOf("2025-01-16"), "Electronics", "Customer2", 100.0, 30.0, 1)
    ).toDF("Order ID", "Order Date", "Category", "Customer ID", "Sales", "Profit", "Quantity")

    val transformed = DataPipeline.transformData(testData)
    val result      = DataPipeline.aggregateData(transformed)

    val avgMargin = result.first().getAs[Double]("Avg Profit Margin")
    avgMargin should be(0.25 +- 0.001) // (0.2 + 0.3) / 2
  }

  test("loadData should write Parquet data correctly") {
    import spark.implicits._

    val testData = Seq(
      ("Category1", 2025, 1, 1000.0, 500.0, 10L, 0.5, 5L, 3L),
      ("Category2", 2025, 2, 2000.0, 800.0, 20L, 0.4, 8L, 5L)
    ).toDF(
      "Category",
      "Year",
      "Quarter",
      "Total Sales",
      "Total Profit",
      "Total Quantity",
      "Avg Profit Margin",
      "Order Count",
      "Unique Customers"
    )

    val tempPath = "target/test-data/parquet-output"
    DataPipeline.loadData(testData, tempPath, "parquet", "overwrite")

    val loaded = spark.read.parquet(tempPath)
    loaded.count() should be(2)
    loaded.columns should contain allOf ("Category", "Total Sales")
  }

  test("loadData should write CSV data correctly") {
    import spark.implicits._

    val testData = Seq(
      ("Category1", 2025, 1, 1000.0),
      ("Category2", 2025, 2, 2000.0)
    ).toDF("Category", "Year", "Quarter", "Total Sales")

    val tempPath = "target/test-data/csv-output"
    DataPipeline.loadData(testData, tempPath, "csv", "overwrite")

    val loaded = spark.read.option("header", "true").csv(tempPath)
    loaded.count() should be(2)
  }

  test("loadData should write JSON data correctly") {
    import spark.implicits._

    val testData = Seq(
      ("Category1", 2025, 1000.0),
      ("Category2", 2025, 2000.0)
    ).toDF("Category", "Year", "Total Sales")

    val tempPath = "target/test-data/json-output"
    DataPipeline.loadData(testData, tempPath, "json", "overwrite")

    val loaded = spark.read.json(tempPath)
    loaded.count() should be(2)
  }

  test("loadData should throw exception for unsupported format") {
    import spark.implicits._

    val testData = Seq(("Category1", 1000.0)).toDF("Category", "Sales")

    assertThrows[IllegalArgumentException] {
      DataPipeline.loadData(testData, "dummy-path", "xml", "overwrite")
    }
  }

  test("loadData should support append mode") {
    import spark.implicits._

    val tempPath = "target/test-data/append-test"

    val testData1 = Seq(("Category1", 1000.0)).toDF("Category", "Sales")
    DataPipeline.loadData(testData1, tempPath, "parquet", "overwrite")

    val testData2 = Seq(("Category2", 2000.0)).toDF("Category", "Sales")
    DataPipeline.loadData(testData2, tempPath, "parquet", "append")

    val loaded = spark.read.parquet(tempPath)
    loaded.count() should be(2)
  }

  test("Full ETL pipeline integration test") {
    import spark.implicits._

    // Create input data
    val inputData = Seq(
      ("Order1", Date.valueOf("2025-01-15"), "Electronics", "Customer1", 100.0, 50.0, 2),
      ("Order1", Date.valueOf("2025-01-15"), "Electronics", "Customer1", 100.0, 50.0, 2), // duplicate
      ("Order2", Date.valueOf("2025-01-16"), "Electronics", "Customer2", 200.0, 80.0, 3),
      ("Order3", Date.valueOf("2025-04-15"), "Books", "Customer3", 150.0, 60.0, 1),
      ("Order4", Date.valueOf("2025-01-17"), "Electronics", "Customer4", -50.0, 20.0, 2), // invalid sales
      ("Order5", Date.valueOf("2025-04-20"), "Books", "Customer5", 300.0, 120.0, 0)       // invalid quantity
    ).toDF("Order ID", "Order Date", "Category", "Customer ID", "Sales", "Profit", "Quantity")

    val inputPath  = "target/test-data/integration-input"
    val outputPath = "target/test-data/integration-output"

    inputData.write.mode("overwrite").option("header", "true").csv(inputPath)

    // Extract
    val extracted = DataPipeline.extractData(spark, inputPath, "csv")
    extracted.count() should be(6)

    // Transform
    val transformed = DataPipeline.transformData(extracted)
    transformed.count() should be(3) // After removing duplicates and invalid records

    // Aggregate
    val aggregated = DataPipeline.aggregateData(transformed)
    aggregated.count() should be(2) // Electronics Q1 and Books Q2

    // Load
    DataPipeline.loadData(aggregated, outputPath, "parquet", "overwrite")

    // Verify
    val loaded = spark.read.parquet(outputPath)
    loaded.count() should be(2)

    val electronicsData = loaded.filter(col("Category") === "Electronics").first()
    electronicsData.getAs[Double]("Total Sales") should be(300.0)
    electronicsData.getAs[Long]("Order Count") should be(2)
  }

}

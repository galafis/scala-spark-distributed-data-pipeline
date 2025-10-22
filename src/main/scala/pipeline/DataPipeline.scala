/**
 * Distributed Data Pipeline with Apache Spark
 * Author: Gabriel Demetrios Lafis
 * Description: ETL pipeline for processing large-scale data
 */

package pipeline

import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions._

object DataPipeline {
  
  /**
   * Initialize Spark Session
   */
  def createSparkSession(appName: String): SparkSession = {
    SparkSession.builder()
      .appName(appName)
      .master("local[*]")
      .config("spark.sql.shuffle.partitions", "200")
      .getOrCreate()
  }
  
  /**
   * Extract: Read data from various sources
   */
  def extractData(spark: SparkSession, path: String, format: String = "csv"): DataFrame = {
    format.toLowerCase match {
      case "csv" =>
        spark.read
          .option("header", "true")
          .option("inferSchema", "true")
          .csv(path)
      
      case "parquet" =>
        spark.read.parquet(path)
      
      case "json" =>
        spark.read.json(path)
      
      case _ =>
        throw new IllegalArgumentException(s"Unsupported format: $format")
    }
  }
  
  /**
   * Transform: Apply business logic and data transformations
   */
  def transformData(df: DataFrame): DataFrame = {
    df
      // Remove duplicates
      .dropDuplicates()
      
      // Handle null values
      .na.fill(Map(
        "Sales" -> 0.0,
        "Profit" -> 0.0,
        "Quantity" -> 0
      ))
      
      // Add derived columns
      .withColumn("Year", year(col("Order Date")))
      .withColumn("Month", month(col("Order Date")))
      .withColumn("Quarter", quarter(col("Order Date")))
      .withColumn("Profit Margin", 
        when(col("Sales") =!= 0, col("Profit") / col("Sales"))
          .otherwise(0))
      
      // Filter invalid records
      .filter(col("Sales") >= 0 and col("Quantity") > 0)
  }
  
  /**
   * Aggregate: Perform aggregations for analytics
   */
  def aggregateData(df: DataFrame): DataFrame = {
    df.groupBy("Category", "Year", "Quarter")
      .agg(
        sum("Sales").alias("Total Sales"),
        sum("Profit").alias("Total Profit"),
        sum("Quantity").alias("Total Quantity"),
        avg("Profit Margin").alias("Avg Profit Margin"),
        count("*").alias("Order Count"),
        countDistinct("Customer ID").alias("Unique Customers")
      )
      .orderBy(desc("Total Sales"))
  }
  
  /**
   * Load: Write data to target destination
   */
  def loadData(df: DataFrame, path: String, format: String = "parquet", mode: String = "overwrite"): Unit = {
    format.toLowerCase match {
      case "parquet" =>
        df.write
          .mode(mode)
          .parquet(path)
      
      case "csv" =>
        df.write
          .mode(mode)
          .option("header", "true")
          .csv(path)
      
      case "json" =>
        df.write
          .mode(mode)
          .json(path)
      
      case _ =>
        throw new IllegalArgumentException(s"Unsupported format: $format")
    }
  }
  
  /**
   * Main ETL pipeline execution
   */
  def runPipeline(inputPath: String, outputPath: String): Unit = {
    val spark = createSparkSession("Data Pipeline")
    
    try {
      println("Starting ETL Pipeline...")
      
      // Extract
      println("Extracting data...")
      val rawData = extractData(spark, inputPath, "csv")
      println(s"Extracted ${rawData.count()} records")
      
      // Transform
      println("Transforming data...")
      val transformedData = transformData(rawData)
      println(s"Transformed to ${transformedData.count()} records")
      
      // Aggregate
      println("Aggregating data...")
      val aggregatedData = aggregateData(transformedData)
      aggregatedData.show(20)
      
      // Load
      println("Loading data...")
      loadData(aggregatedData, outputPath, "parquet")
      println(s"Data loaded to: $outputPath")
      
      println("Pipeline completed successfully!")
      
    } catch {
      case e: Exception =>
        println(s"Pipeline failed with error: ${e.getMessage}")
        e.printStackTrace()
    } finally {
      spark.stop()
    }
  }
  
  def main(args: Array[String]): Unit = {
    if (args.length < 2) {
      println("Usage: DataPipeline <input_path> <output_path>")
      System.exit(1)
    }
    
    val inputPath = args(0)
    val outputPath = args(1)
    
    runPipeline(inputPath, outputPath)
  }
}

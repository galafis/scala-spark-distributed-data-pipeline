# Scala Spark Distributed Data Pipeline

![Scala](https://img.shields.io/badge/Scala-DC322F?style=for-the-badge&logo=scala&logoColor=white) ![Apache Spark](https://img.shields.io/badge/Apache%20Spark-E25A1C?style=for-the-badge&logo=apache-spark&logoColor=white) ![Big Data](https://img.shields.io/badge/Big_Data-FF6F00?style=for-the-badge)

---

## 🇧🇷 Pipeline de Dados Distribuído com Scala e Spark

Pipeline ETL de **nível empresarial** construído com **Scala** e **Apache Spark** para processamento distribuído de grandes volumes de dados. Demonstra padrões modernos de engenharia de dados, incluindo processamento em batch, otimizações de performance e integração com data lakes.

### 🎯 Objetivo

Fornecer uma arquitetura completa e escalável para pipelines de dados que processam **terabytes de dados** com alta performance, demonstrando as melhores práticas de Spark em produção.

### 🌟 Por que Scala + Spark?

| Característica | Scala/Spark | Python/Spark | Java/Spark |
|----------------|-------------|--------------|------------|
| **Performance** | Excelente (nativo) | Boa (overhead) | Excelente |
| **Type Safety** | ✅ Forte | ❌ Fraca | ✅ Forte |
| **Expressividade** | ✅ Alta | ✅ Alta | ❌ Média |
| **Ecosystem** | Spark nativo | Pandas, NumPy | Enterprise |
| **Learning Curve** | Médio-Alto | Baixo | Médio |

### 📊 Casos de Uso

1. **E-commerce**: Processar 100M+ eventos de clickstream diariamente
2. **Fintech**: Agregação de transações para detecção de fraude
3. **Telecom**: Análise de CDRs (Call Detail Records) em escala
4. **IoT**: Processamento de telemetria de milhões de dispositivos
5. **Marketing**: ETL de dados de múltiplas fontes para data warehouse

### 🏗️ Arquitetura

```
┌─────────────┐
│ Data Sources│ (S3, HDFS, Kafka, JDBC)
└──────┬──────┘
       │
       ▼
┌──────────────────────────────────┐
│   Spark Cluster (YARN/K8s)       │
│  ┌────────┐  ┌────────┐  ┌────┐ │
│  │ Driver │→ │Executor│→ │... │ │
│  └────────┘  └────────┘  └────┘ │
│                                  │
│  - Extract (read)                │
│  - Transform (map/filter/agg)    │
│  - Load (write)                  │
└──────────────┬───────────────────┘
               │
               ▼
       ┌───────────────┐
       │ Data Lake/DW  │ (Parquet, Delta, Iceberg)
       └───────────────┘
```

### 💻 Código Principal

```scala
package pipeline

import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._

object DataPipeline {
  
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("Distributed Data Pipeline")
      .config("spark.sql.adaptive.enabled", "true")
      .config("spark.sql.adaptive.coalescePartitions.enabled", "true")
      .getOrCreate()
    
    import spark.implicits._
    
    // Extract
    val rawData = extractData(spark, "s3a://bucket/raw-data/")
    
    // Transform
    val transformedData = transformData(rawData)
    
    // Load
    loadData(transformedData, "s3a://bucket/processed-data/")
    
    spark.stop()
  }
  
  def extractData(spark: SparkSession, path: String): DataFrame = {
    spark.read
      .option("header", "true")
      .option("inferSchema", "true")
      .parquet(path)
  }
  
  def transformData(df: DataFrame): DataFrame = {
    df.filter($"amount" > 0)
      .withColumn("year", year($"date"))
      .withColumn("month", month($"date"))
      .groupBy("year", "month", "category")
      .agg(
        sum("amount").as("total_amount"),
        count("*").as("transaction_count"),
        avg("amount").as("avg_amount")
      )
      .orderBy("year", "month")
  }
  
  def loadData(df: DataFrame, path: String): Unit = {
    df.write
      .mode("overwrite")
      .partitionBy("year", "month")
      .parquet(path)
  }
}
```

### 🚀 Instalação e Execução

```bash
# Build com sbt
sbt clean compile package

# Executar localmente
spark-submit \
  --class pipeline.DataPipeline \
  --master local[*] \
  target/scala-2.12/data-pipeline_2.12-1.0.jar

# Executar em cluster YARN
spark-submit \
  --class pipeline.DataPipeline \
  --master yarn \
  --deploy-mode cluster \
  --num-executors 10 \
  --executor-memory 4G \
  --executor-cores 2 \
  target/scala-2.12/data-pipeline_2.12-1.0.jar
```

### 📈 Otimizações de Performance

```scala
// 1. Broadcast Join para tabelas pequenas
val largeDf = spark.read.parquet("large_table")
val smallDf = spark.read.parquet("small_table")
val result = largeDf.join(broadcast(smallDf), "key")

// 2. Reparticionamento estratégico
val optimized = df.repartition(200, $"partition_key")

// 3. Cache de DataFrames intermediários
val cachedDf = df.filter($"active" === true).cache()

// 4. Predicate Pushdown
val filtered = spark.read
  .parquet("data")
  .filter($"date" >= "2025-01-01")  // Filtro antes de ler

// 5. Column Pruning
val selected = df.select("id", "name", "amount")  // Apenas colunas necessárias
```

### 🎓 Conceitos Avançados

#### Transformações Lazy vs Actions

```scala
// Lazy (não executa)
val transformed = df.filter($"amount" > 100).select("id", "amount")

// Action (executa todo o pipeline)
transformed.count()  // Trigger
transformed.show()   // Trigger
transformed.write.parquet("output")  // Trigger
```

#### Particionamento Eficiente

```scala
// Particionar por data para queries temporais
df.write
  .partitionBy("year", "month", "day")
  .parquet("output")

// Query otimizada
spark.read.parquet("output")
  .filter($"year" === 2025 && $"month" === 10)  // Lê apenas partições relevantes
```

### 🧪 Testes

```scala
import org.scalatest.funsuite.AnyFunSuite
import org.apache.spark.sql.SparkSession

class DataPipelineTest extends AnyFunSuite {
  
  test("transformData should aggregate correctly") {
    val spark = SparkSession.builder()
      .master("local[*]")
      .getOrCreate()
    
    import spark.implicits._
    
    val input = Seq(
      ("2025-01-01", "A", 100.0),
      ("2025-01-01", "A", 200.0),
      ("2025-01-02", "B", 150.0)
    ).toDF("date", "category", "amount")
    
    val result = DataPipeline.transformData(input)
    
    assert(result.count() == 2)
    assert(result.filter($"category" === "A").select("total_amount").first().getDouble(0) == 300.0)
  }
}
```

### 📊 Monitoramento

```scala
// Métricas customizadas
val metrics = df.agg(
  count("*").as("total_records"),
  sum("amount").as("total_amount"),
  avg("amount").as("avg_amount"),
  min("date").as("min_date"),
  max("date").as("max_date")
).collect()(0)

println(s"Processed ${metrics.getLong(0)} records")
println(s"Total amount: ${metrics.getDouble(1)}")
```

### 🔗 Recursos

- [Spark Scala API](https://spark.apache.org/docs/latest/api/scala/)
- [Scala Documentation](https://docs.scala-lang.org/)
- [High Performance Spark (Book)](https://www.oreilly.com/library/view/high-performance-spark/9781491943199/)

---

## 🇬🇧 Scala Spark Distributed Data Pipeline

Enterprise-grade ETL pipeline built with **Scala** and **Apache Spark** for distributed processing of large data volumes.

### 🚀 Quick Start

```bash
sbt clean compile package
spark-submit --class pipeline.DataPipeline --master local[*] target/scala-2.12/data-pipeline_2.12-1.0.jar
```

---

**Author:** Gabriel Demetrios Lafis  
**License:** MIT

# Scala Spark Distributed Data Pipeline

[![Scala CI](https://github.com/galafis/scala-spark-distributed-data-pipeline/actions/workflows/ci.yml/badge.svg)](https://github.com/galafis/scala-spark-distributed-data-pipeline/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
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

#### Pré-requisitos

- **Java JDK 11** (recomendado para testes)
  - ⚠️ Nota: Java 17 é suportado mas há incompatibilidades conhecidas com Spark 3.5.0 em testes de I/O
- SBT 1.9.x
- Apache Spark 3.5.0 (opcional, para cluster)

#### Instalação

```bash
# Clone o repositório
git clone https://github.com/galafis/scala-spark-distributed-data-pipeline.git
cd scala-spark-distributed-data-pipeline

# Build com sbt
sbt clean compile package

# Criar JAR com todas as dependências (assembly)
sbt assembly
```

#### Execução

```bash
# Executar localmente
spark-submit \
  --class pipeline.DataPipeline \
  --master local[*] \
  target/scala-2.12/scala-spark-distributed-data-pipeline_2.12-1.0.0.jar \
  input_data.csv \
  output_results/

# Executar em cluster YARN
spark-submit \
  --class pipeline.DataPipeline \
  --master yarn \
  --deploy-mode cluster \
  --num-executors 10 \
  --executor-memory 4G \
  --executor-cores 2 \
  target/scala-2.12/scala-spark-distributed-data-pipeline_2.12-1.0.0.jar \
  s3a://bucket/input/ \
  s3a://bucket/output/
```

### 🧪 Testes

O projeto inclui uma suíte completa de testes unitários e de integração.

#### Executar Testes

```bash
# Executar todos os testes
sbt test

# Executar teste específico
sbt "testOnly pipeline.DataPipelineTest"

# Executar testes com cobertura
sbt coverage test coverageReport

# Ver relatório de cobertura
open target/scala-2.12/scoverage-report/index.html
```

#### Estrutura de Testes

- **SparkTest**: Classe base que configura SparkSession para testes
- **DataPipelineTest**: 17+ testes cobrindo todas as funções do pipeline
  - Testes de extração (CSV, Parquet, JSON)
  - Testes de transformação (limpeza, agregação, filtros)
  - Testes de carga (diferentes formatos e modos)
  - Teste de integração end-to-end

#### Formatação de Código

```bash
# Verificar formatação
sbt scalafmtCheckAll

# Formatar código automaticamente
sbt scalafmtAll
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

### 🧪 Testes (Tests)

Este projeto possui uma suíte completa de testes automatizados. Veja a seção de testes acima para mais detalhes.

Exemplo de teste:

```scala
test("transformData should remove duplicates") {
  val testData = Seq(
    ("Order1", Date.valueOf("2025-01-15"), "Electronics", "Customer1", 100.0, 50.0, 2),
    ("Order1", Date.valueOf("2025-01-15"), "Electronics", "Customer1", 100.0, 50.0, 2)
  ).toDF("Order ID", "Order Date", "Category", "Customer ID", "Sales", "Profit", "Quantity")

  val result = DataPipeline.transformData(testData)
  
  result.count() should be(1)
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

### 🔧 Troubleshooting (Solução de Problemas)

#### Erro: "OutOfMemoryError"

```bash
# Aumentar memória do executor
spark-submit \
  --executor-memory 8G \
  --driver-memory 4G \
  --conf spark.memory.fraction=0.8 \
  ...
```

#### Erro: Testes falhando com Java 17

⚠️ **Problema Conhecido**: Incompatibilidade entre Spark 3.5.0 e Java 17

```bash
# Solução 1: Usar Java 11 (Recomendado)
export JAVA_HOME=/path/to/java11
sbt test

# Solução 2: Aguardar Spark 3.6.0+ com suporte completo a Java 17

# Solução 3: Executar em produção (cluster já configurado)
# Os testes locais podem falhar, mas o código funciona em cluster
```

**Contexto**: Java 17 introduziu mudanças no sistema de módulos que afetam o acesso interno do Spark a classes sun.*. Embora o código compile e execute em clusters de produção, alguns testes locais podem falhar.

#### Erro: "Unable to load native-hadoop library"

```bash
# Adicionar dependências Hadoop no classpath
export HADOOP_HOME=/path/to/hadoop
export LD_LIBRARY_PATH=$HADOOP_HOME/lib/native:$LD_LIBRARY_PATH
```

#### Performance lenta em agregações

```scala
// Aumentar partições antes de agregar
df.repartition(200, $"partition_key")
  .groupBy("key")
  .agg(...)

// Ou usar coalesce após agregar
result.coalesce(10).write.parquet("output")
```

#### Problema com tipos de dados

```scala
// Definir schema explicitamente
val schema = StructType(Seq(
  StructField("id", LongType, nullable = false),
  StructField("name", StringType, nullable = true),
  StructField("amount", DoubleType, nullable = false)
))

spark.read.schema(schema).csv("data.csv")
```

#### Testes falhando

```bash
# Limpar cache e executar novamente
sbt clean test

# Executar com mais memória
export SBT_OPTS="-Xmx4G -XX:MaxMetaspaceSize=512M"
sbt test
```

### 🔗 Recursos

- [Spark Scala API](https://spark.apache.org/docs/latest/api/scala/)
- [Scala Documentation](https://docs.scala-lang.org/)
- [High Performance Spark (Book)](https://www.oreilly.com/library/view/high-performance-spark/9781491943199/)

### 🤝 Como Contribuir

Contribuições são bem-vindas! Por favor, leia nosso [Guia de Contribuição](CONTRIBUTING.md) antes de enviar pull requests.

1. Faça um fork do repositório
2. Crie sua feature branch (`git checkout -b feature/NovaFuncionalidade`)
3. Commit suas mudanças (`git commit -m 'feat: Adiciona nova funcionalidade'`)
4. Push para a branch (`git push origin feature/NovaFuncionalidade`)
5. Abra um Pull Request

### 📄 Licença

Este projeto está licenciado sob a Licença MIT - veja o arquivo [LICENSE](LICENSE) para detalhes.

---

## 🇬🇧 Scala Spark Distributed Data Pipeline

Enterprise-grade ETL pipeline built with **Scala** and **Apache Spark** for distributed processing of large data volumes.

### 🚀 Quick Start

```bash
# Clone and build
git clone https://github.com/galafis/scala-spark-distributed-data-pipeline.git
cd scala-spark-distributed-data-pipeline
sbt clean compile test

# Run with sample data
sbt "run input_data.csv output_results/"

# Create assembly JAR
sbt assembly

# Run with Spark
spark-submit \
  --class pipeline.DataPipeline \
  --master local[*] \
  target/scala-2.12/scala-spark-distributed-data-pipeline_2.12-1.0.0.jar \
  input_data.csv \
  output_results/
```

### 🧪 Testing

```bash
# Run all tests
sbt test

# Run with coverage
sbt coverage test coverageReport
```

### 🤝 Contributing

Contributions are welcome! Please read our [Contributing Guidelines](CONTRIBUTING.md) before submitting pull requests.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'feat: Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

**Author:** Gabriel Demetrios Lafis  
**Repository:** [github.com/galafis/scala-spark-distributed-data-pipeline](https://github.com/galafis/scala-spark-distributed-data-pipeline)  
**Status:** [![Scala CI](https://github.com/galafis/scala-spark-distributed-data-pipeline/actions/workflows/ci.yml/badge.svg)](https://github.com/galafis/scala-spark-distributed-data-pipeline/actions/workflows/ci.yml)

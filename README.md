# Distributed Data Pipelines with Scala and Apache Spark

![Scala](https://img.shields.io/badge/Scala-DC322F?style=for-the-badge&logo=scala&logoColor=white) ![Apache Spark](https://img.shields.io/badge/Apache%20Spark-E25A1C?style=for-the-badge&logo=apache-spark&logoColor=white)

---

## 🇧🇷 Pipelines de Dados Distribuídos com Scala e Apache Spark

Este repositório apresenta a construção de pipelines de dados robustos e escaláveis para processamento em lote e streaming, utilizando **Scala** e **Apache Spark**. O projeto demonstra as melhores práticas para engenharia de dados em ambientes distribuídos, desde a ingestão até a disponibilização dos dados para análise.

### 🎯 Objetivo

O objetivo é fornecer um exemplo completo de um pipeline de dados de ponta a ponta, cobrindo as etapas de extração, transformação e carga (ETL) de grandes volumes de dados. O repositório é um guia prático para engenheiros de dados que desejam construir sistemas de processamento de dados de alta performance com Spark e Scala.

### 📂 Conteúdo do Repositório

*   **/src/main/scala**: Código-fonte em Scala.
    *   `pipeline`: Lógica para o pipeline de processamento em lote (batch).
    *   `streaming`: Implementação de processamento de dados em tempo real com Spark Streaming ou Structured Streaming.
    *   `ml`: Exemplos de uso do Spark MLlib para aplicar modelos de machine learning em escala.
*   **/data**: Datasets de exemplo para o processamento.
*   **/tests**: Testes unitários e de integração para o pipeline.
*   `build.sbt`: Arquivo de build do SBT com as dependências do projeto.

### 🛠️ Funcionalidades

*   **Processamento em Lote (Batch)**: Jobs Spark para processar grandes datasets de forma eficiente.
*   **Processamento de Streaming**: Consumo e processamento de dados de fontes de streaming como Apache Kafka.
*   **Integração com Data Lakes**: Leitura e escrita de dados em formatos otimizados (Parquet, ORC) em data lakes como HDFS ou Amazon S3.
*   **Machine Learning em Escala**: Treinamento e aplicação de modelos de machine learning em dados distribuídos com Spark MLlib.

---

## 🇬🇧 Distributed Data Pipelines with Scala and Apache Spark

This repository showcases the construction of robust and scalable data pipelines for batch and stream processing, using **Scala** and **Apache Spark**. The project demonstrates best practices for data engineering in distributed environments, from ingestion to making data available for analysis.

### 🎯 Objective

The goal is to provide a complete example of an end-to-end data pipeline, covering the extraction, transformation, and loading (ETL) of large volumes of data. The repository is a practical guide for data engineers who want to build high-performance data processing systems with Spark and Scala.

### 📂 Repository Content

*   **/src/main/scala**: Scala source code.
    *   `pipeline`: Logic for the batch processing pipeline.
    *   `streaming`: Implementation of real-time data processing with Spark Streaming or Structured Streaming.
    *   `ml`: Examples of using Spark MLlib to apply machine learning models at scale.
*   **/data**: Example datasets for processing.
*   **/tests**: Unit and integration tests for the pipeline.
*   `build.sbt`: SBT build file with project dependencies.

### 🛠️ Features

*   **Batch Processing**: Spark jobs to efficiently process large datasets.
*   **Stream Processing**: Consumption and processing of data from streaming sources like Apache Kafka.
*   **Data Lake Integration**: Reading and writing data in optimized formats (Parquet, ORC) in data lakes such as HDFS or Amazon S3.
*   **Machine Learning at Scale**: Training and applying machine learning models on distributed data with Spark MLlib.


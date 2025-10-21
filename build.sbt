name := "scala-spark-distributed-data-pipeline"

version := "1.0.0"

scalaVersion := "2.12.15"

val sparkVersion = "3.5.0"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion % "provided",
  "org.apache.spark" %% "spark-sql" % sparkVersion % "provided",
  "org.apache.spark" %% "spark-mllib" % sparkVersion % "provided",
  "org.apache.spark" %% "spark-streaming" % sparkVersion % "provided",
  "org.scalatest" %% "scalatest" % "3.2.15" % Test,
  "org.apache.spark" %% "spark-core" % sparkVersion % Test classifier "tests",
  "org.apache.spark" %% "spark-sql" % sparkVersion % Test classifier "tests"
)

// Test configuration
Test / fork := true
Test / parallelExecution := false
Test / javaOptions ++= Seq(
  "-Xmx2G",
  "-XX:+CMSClassUnloadingEnabled"
)

// Assembly settings for creating fat JAR
assembly / assemblyJarName := s"${name.value}_${scalaBinaryVersion.value}-${version.value}.jar"

assembly / assemblyMergeStrategy := {
  case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case "reference.conf" => MergeStrategy.concat
  case _ => MergeStrategy.first
}

// Exclude Spark dependencies from the assembly JAR (provided by cluster)
assembly / assemblyExcludedJars := {
  val cp = (assembly / fullClasspath).value
  cp.filter { jar =>
    jar.data.getName.startsWith("spark-") ||
    jar.data.getName.startsWith("hadoop-") ||
    jar.data.getName.startsWith("scala-library")
  }
}

// Compiler options
scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-unchecked",
  "-Xlint"
)

# Simple Spark Project

I'll use this project to demonstrate points on -

* SBT
* CD pipeline
* AWS Cloudformation
* Spark and Spark UI
* S3
* Cassandra

## Build the Project

1. Clone the Repository: `git clone https://github.com/kevvo83/simple-spark-project.git`
2. Build the Assembly Jar: `sbt -DlibDependencyOpt=CLUSTER clean update compile assembly`


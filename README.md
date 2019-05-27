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

## Submit the Project

### Submit to a YARN Cluster in Cluster deploy mode
SSH to the YARN Master server and execute following:
```
spark-submit --class packageA.Herewego \
    --deploy-mode cluster --master yarn \
    --num-executors 2 --conf spark.executor.cores=2 \
    --conf spark.executor.memory=2g --conf spark.driver.memory=1g \
    --conf spark.driver.cores=1 --conf spark.logConf=true \
    <location of JAR file> <location of logile> <location of folder to store Hive table> \
    <IAM user's access key to access S3 locations> <IAM user's secret key to access S3 locations>
```
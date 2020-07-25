import org.apache.log4j.{Level, Logger}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql._
import org.apache.spark.sql.functions.to_timestamp
import org.apache.spark.mllib.linalg.{Vector,DenseVector}
import org.apache.spark.ml.evaluation.RegressionEvaluator
import org.apache.spark.ml.regression.LinearRegression
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.regression.LinearRegressionModel
import org.apache.spark.mllib.regression.LinearRegressionWithSGD
import org.apache.spark.ml.tuning.{ParamGridBuilder, TrainValidationSplit}


object Stocks {
  //def gradientDescend(
  def main(args: Array[String]) = {
  Logger.getLogger("org").setLevel(Level.ERROR)
  val spark: SparkSession = SparkSession.builder.master("local[*]").getOrCreate()
  //val sc = new SparkContext("local[*]", "Stocks")
  val sc = spark.sparkContext
  var data = sc.textFile("/home/juser/Downloads/nse-stocks-data/Stock.csv")
  val kvp = data.map{ x => 
      val y = x.split(",")
      val (stk,opn,high,low,cls,lst,prvcls,totqty,totval,ts,tottrd) = (y(0),y(2).toFloat,y(3).toFloat, y(4).toFloat ,y(5).toFloat , y(6).toFloat,y(7).toFloat,y(8).toInt,y(9).toFloat,y(10),y(11).toInt)
      //(opn,high,low,cls,lst,prvcls,totqty,totval,tottrd)
      //( y(9).toDouble , (y(2).toDouble,y(3).toDouble,y(4).toDouble,y(5).toDouble,y(6).toDouble,y(7).toDouble))
    LabeledPoint( y(9).toDouble , Vectors.dense(y(2).toDouble,y(3).toDouble,y(4).toDouble,y(11).toDouble,y(6).toDouble,y(7).toDouble))//,y(8).toDouble,y(11).toDouble))
  }//.cache()

  val kv = spark.createDataFrame(kvp).toDF("label","features")//"Name","Date","Open","High","Low","Close","Last","Prev Close","Total Qty","Totat Value","Total Trades")
  //kv.show
  val numIterations = 10000
  val stepSize = 0.0001
  //val kdf = kvp.map{ x =>
  val model = LinearRegressionWithSGD.train(kvp, numIterations, stepSize)
  val valuesAndPreds = kvp.map { point =>
  val prediction = model.predict(point.features)
  //println(point.label,prediction)
  (point.label, prediction)
              }
  
 /* val tr = kv//spark.read.format("libsvm").load("/home/juser/Downloads/nse-stocks-data/Stock.csv")
  val lr = new LinearRegression()
  .setMaxIter(1000)
  .setRegParam(0.0001)
  .setElasticNetParam(0.8)
  val lrModel = lr.fit(tr)
  println(s"Coefficients: ${lrModel.coefficients} Intercept: ${lrModel.intercept}")
  val trainingSummary = lrModel.summary
  println(s"numIterations: ${trainingSummary.totalIterations}")
  println(s"objectiveHistory: [${trainingSummary.objectiveHistory.mkString(",")}]")
  trainingSummary.residuals.show()
  println(s"RMSE: ${trainingSummary.rootMeanSquaredError}")
  println(s"r2: ${trainingSummary.r2}")*/
  
  
  val MSE = valuesAndPreds.map{ case(v, p) => math.pow((v - p), 2)}.mean()
  println(s"training Mean Squared Error $MSE")
  
   }
}
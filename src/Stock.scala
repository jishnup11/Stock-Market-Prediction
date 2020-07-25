import org.apache.log4j.{Level, Logger}
import org.apache.spark.{SparkConf, SparkContext}
import com.cloudera.sparkts.models.ARIMA
import org.apache.spark.sql._
import org.apache.spark.sql.functions.to_timestamp
import com.cloudera.sparkts.{DateTimeIndex, DayFrequency, TimeSeriesRDD}
import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}
import java.time.{ZoneId, ZonedDateTime}
import org.apache.spark.mllib.linalg.{Vector,DenseVector}
//import services.SparkTsService.{getData}
import scala.collection.mutable.ListBuffer
import scala.language.postfixOps
import org.threeten._


object Stock {
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
      //(stk,opn,high,low,cls,lst,prvcls,totqty,totval,ts,tottrd)
      (stk,ts,cls)//opn,high,low,cls,lst,prvcls,totqty,totval,tottrd) 
      //( (stk,ts) , (opn,high,low,cls,lst,prvcls,totqty,totval,tottrd) )
  }
  val kv = spark.createDataFrame(kvp).toDF("Name","Date","ClosePrice")//,"Open","High","Low","Close","Last","Prev Close","Total Qty","Totat Value","Total Trades")
  val kvf = kv
          .withColumn("TimeStamp",to_timestamp(kv("Date")))
          .withColumn("Close",kv("ClosePrice").cast(DoubleType))
          .drop("ClosePrice","Date").sort("TimeStamp")
  //kvf.show()
  
  val minDate = kvf.selectExpr("min(timestamp)").collect()(0).getTimestamp(0)
  val maxDate = kvf.selectExpr("max(timestamp)").collect()(0).getTimestamp(0)
  val zone = ZoneId.systemDefault()
  val dtIndex = DateTimeIndex.uniformFromInterval(
            ZonedDateTime.of(minDate.toLocalDateTime, zone), ZonedDateTime.of(maxDate.toLocalDateTime, zone), new DayFrequency(1)
        )
  val tsRdd = TimeSeriesRDD.timeSeriesRDDFromObservations(dtIndex, kvf,"TimeStamp","Name", "Close")
  val noOfDays = 5
  val df = tsRdd.mapSeries{vector => {
  print("Vector----------------------------")
  println(vector)
 print("_____________________________")
  val newVec = new DenseVector(vector.toArray.map(x =>x)) //if(x.equals(Double.NaN)) 0 else x))
  //val newVec = vector
  val arimaModel = ARIMA.fitModel(1, 0, 0, newVec)
  val forecasted = arimaModel.forecast(newVec, noOfDays)
  new org.apache.spark.mllib.linalg.DenseVector(forecasted.toArray.slice(forecasted.size-(noOfDays+1), forecasted.size-1))
        }}
  val companyList:List[String] = df.collectAsTimeSeries().keys.toList
  //val multipleCompanyValues = createMultipleCompanyValues(noOfDays,companyList)
  val priceForecast = df.collect()
  val companies = df.collect().map(_._1)
  //println(priceForecast.mkString(" "))  
  priceForecast.take(10).foreach(println)
   }
}
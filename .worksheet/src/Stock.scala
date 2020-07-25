import org.apache.log4j.{Level, Logger}
//import org.apache.spark.{SparkConf, SparkContext}
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


object Stock {;import org.scalaide.worksheet.runtime.library.WorksheetSupport._; def main(args: Array[String])=$execute{;$skip(703); 
  //def gradientDescend(
  
  Logger.getLogger("org").setLevel(Level.ERROR);$skip(82); 
  val spark: SparkSession = SparkSession.builder.master("local[*]").getOrCreate();System.out.println("""spark  : org.apache.spark.sql.SparkSession = """ + $show(spark ));$skip(82); 
  //val sc = new SparkContext("local[*]", "Stocks")
  val sc = spark.sparkContext;System.out.println("""sc  : org.apache.spark.SparkContext = """ + $show(sc ));$skip(76); 
  var data = sc.textFile("/home/juser/Downloads/nse-stocks-data/Stock.csv");System.out.println("""data  : org.apache.spark.rdd.RDD[String] = """ + $show(data ));$skip(476); 
  val kvp = data.map{ x =>
      val y = x.split(",")println
      val (stk,opn,high,low,cls,lst,prvcls,totqty,totval,ts,tottrd) = (y(0),y(2).toFloat,y(3).toFloat, y(4).toFloat ,y(5).toFloat , y(6).toFloat,y(7).toFloat,y(8).toInt,y(9).toFloat,y(10),y(11).toInt)
      //(stk,opn,high,low,cls,lst,prvcls,totqty,totval,ts,tottrd)
      (stk,ts,cls)//opn,high,low,cls,lst,prvcls,totqty,totval,tottrd)
      //( (stk,ts) , (opn,high,low,cls,lst,prvcls,totqty,totval,tottrd) )
  };System.out.println("""kvp  : org.apache.spark.rdd.RDD[(Any, Any, Any)] = """ + $show(kvp ));$skip(163); 
  val kv = spark.createDataFrame(kvp).toDF("Name","Date","ClosePrice");System.out.println("""kv  : org.apache.spark.sql.DataFrame = """ + $show(kv ));$skip(195); //,"Open","High","Low","Close","Last","Prev Close","Total Qty","Totat Value","Total Trades")
  val kvf = kv
          .withColumn("TimeStamp",to_timestamp(kv("Date")))
          .withColumn("Close",kv("ClosePrice").cast(DoubleType))
          .drop("ClosePrice","Date").sort("TimeStamp");System.out.println("""kvf  : org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = """ + $show(kvf ));$skip(13); 
  kvf.show();$skip(81); 
  
  val minDate = kvf.selectExpr("min(timestamp)").collect()(0).getTimestamp(0);System.out.println("""minDate  : java.sql.Timestamp = """ + $show(minDate ));$skip(78); 
  val maxDate = kvf.selectExpr("max(timestamp)").collect()(0).getTimestamp(0);System.out.println("""maxDate  : java.sql.Timestamp = """ + $show(maxDate ));$skip(36); 
  val zone = ZoneId.systemDefault();System.out.println("""zone  : java.time.ZoneId = """ + $show(zone ));$skip(191); 
  val dtIndex = DateTimeIndex.uniformFromInterval(
            ZonedDateTime.of(minDate.toLocalDateTime, zone), ZonedDateTime.of(maxDate.toLocalDateTime, zone), new DayFrequency(1)
        );System.out.println("""dtIndex  : com.cloudera.sparkts.UniformDateTimeIndex = """ + $show(dtIndex ));$skip(102); 
  val tsRdd = TimeSeriesRDD.timeSeriesRDDFromObservations(dtIndex, kvf, "TimeStamp", "Name", "Close");System.out.println("""tsRdd  : com.cloudera.sparkts.TimeSeriesRDD[String] = """ + $show(tsRdd ));$skip(20); 
  val noOfDays = 30;System.out.println("""noOfDays  : Int = """ + $show(noOfDays ));$skip(425); 
  val df = tsRdd.mapSeries{vector => {
  val newVec = new org.apache.spark.mllib.linalg.DenseVector(vector.toArray.map(x => if(x.equals(Float.NaN)) 0 else x))
  //val newVec = vector
  val arimaModel = ARIMA.fitModel(1, 0, 0, newVec)
  val forecasted = arimaModel.forecast(newVec, noOfDays)
  new org.apache.spark.mllib.linalg.DenseVector(forecasted.toArray.slice(forecasted.size-(noOfDays+1), forecasted.size-1))
        }};System.out.println("""df  : com.cloudera.sparkts.TimeSeriesRDD[String] = """ + $show(df ));$skip(70); 
  val companyList:List[String] = df.collectAsTimeSeries().keys.toList;System.out.println("""companyList  : List[String] = """ + $show(companyList ));$skip(117); 
  //val multipleCompanyValues = createMultipleCompanyValues(noOfDays,companyList)
  val priceForecast = df.collect();System.out.println("""priceForecast  : Array[(String, org.apache.spark.mllib.linalg.Vector)] = """ + $show(priceForecast ));$skip(41); 
  val companies = df.collect().map(_._1);System.out.println("""companies  : Array[String] = """ + $show(companies ));$skip(83); 
  //println(priceForecast.mkString(" "))
  priceForecast.take(10).foreach(println)}
   
}

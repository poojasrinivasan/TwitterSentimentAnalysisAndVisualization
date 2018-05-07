package org.sparkstreaming.twitteranalysis
import org.apache.spark.SparkConf
import org.apache.spark.streaming.twitter._
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.functions.not
import org.elasticsearch.spark._
import org.sparkstreaming.twitteranalysis.SentimentAnalysisUtil._
import org.sparkstreaming.twitteranalysis.GeoLocationPlayUtil._
import twitter4j.{HashtagEntity, Status, URLEntity}
import org.elasticsearch.spark.streaming.EsSparkStreaming

object TwitterStreaming {
   def main(args: Array[String]) {
     System.setProperty("hadoop.home.dir", "c:\\winutils\\");
     if(args.length < 4){
       System.err.println("Usage: TwitterSentimentAnalysis <consumer key> <consumer secret> " +
         "<access token> <access token secret> [<filters>]")
         System.exit(1)
     }
     
     val Array(consumerKey,consumerSecret,accessToken,accessTokenSecret) = args.take(4)
     val filters= args.takeRight(args.length-4)
     
     //set twitter oAuth keys
     
     System.setProperty("twitter4j.oauth.consumerKey",consumerKey)
     System.setProperty("twitter4j.oauth.consumerSecret",consumerSecret)
     System.setProperty("twitter4j.oauth.accessToken",accessToken)
     System.setProperty("twitter4j.oauth.accessTokenSecret",accessTokenSecret)
     val conf= new SparkConf().setAppName("TwitterSentimentAnalysis").setMaster("local[4]")
     val ssc= new StreamingContext(conf,Seconds(5))
     val stream=TwitterUtils.createStream(ssc,None,filters)
    val filterTweet= stream.filter {_.getLang == "en"} .filter { x => x.getUser().getLocation() != null}.filter {findHashTags}
 
    filterTweet.print()
        filterTweet.foreachRDD{(rdd, time) =>
          rdd.map(x => {
         Map(
           "created_at" -> x.getCreatedAt.getTime.toString,
           "text"->(x.getText().replaceAll("[^\u0000-\uFFFF]", "").replaceAll("[^\\p{L}\\p{Nd}\\s]+", "")).replaceAll("\\bhttp\\w*", ""),
           //"hashtags" -> x.getHashtagEntities.map(_.getText),
           "geolocation"-> Option(x.getGeoLocation).map(geo => { s"${geo.getLatitude},${geo.getLongitude}" }),
        //  "place" ->"",
           "userlocation" ->findLocation(x.getUser().getLocation()),
           "sentiment" -> detectSentiment(x.getText).toString
         )
       }).saveToEs("twitter_110418/tweet")}
 
    val rootLogger = Logger.getRootLogger()
    rootLogger.setLevel(Level.ERROR)
    ssc.start()
    ssc.awaitTermination()
   }
   
   

   
    def findHashTags(st: Status):Boolean ={
      
     val hTags : Array[HashtagEntity] = st.getHashtagEntities()
    // hTags.take(10).foreach { println }
     val hTag = "Trump"
     var returnT = false
     for (ht <- hTags)
       if (ht.getText.equalsIgnoreCase(hTag)){
     
       returnT = true
       
       return returnT
       }
       returnT
    }
    
    
       

    
}
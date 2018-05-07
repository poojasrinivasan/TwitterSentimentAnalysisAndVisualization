package org.sparkstreaming.twitteranalysis
import javax.inject.Inject
import scala.concurrent.Future
import scala.concurrent.Await
import akka.util.Timeout
import play.api.mvc._
import play.api.libs.ws._
import play.api.libs.ws.WSClient
import play.api.libs.ws.ning.NingWSClient   
import scala.concurrent.ExecutionContext.Implicits.global
import java.net.URLEncoder
object GeoLocationPlayUtil {
 

  def findLocation(address: String): String = {
   val ws=NingWSClient()
    implicit val timeout = Timeout(5000)
     //println(address)
       val addressEncoded = URLEncoder.encode(address, "UTF-8")
     //  println(addressEncoded)
       val jsonContainingLatitudeAndLongitude =  ws.url("http://maps.googleapis.com/maps/api/geocode/json?address=" + addressEncoded + "&sensor=true").get()
          val future = jsonContainingLatitudeAndLongitude.map {
       response => (response.json \\ "location")
      }
      val result = Await.result(future, timeout.duration).asInstanceOf[List[play.api.libs.json.JsObject]]
      val fResult =   result.toArray
      var lat = ""
      var lon = ""
      if(fResult.size!=0){
      lat = (fResult(0) \\ "lat")(0).toString
      lon = (fResult(0) \\ "lng")(0).toString
   return  lat +","+ lon
      }
      else{
        return ""
      }
      
  }
 
}
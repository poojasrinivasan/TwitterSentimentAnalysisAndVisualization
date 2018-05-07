package org.sparkstreaming.twitteranalysis
import com.koddi.geocoder.Geocoder

object GeoLocationUtil {
       // Next lets create an anonymous Geocoder client
       val client = Geocoder.create()
       def findLocation(address: String): String = {
         // Lookup a location with a formatted address string
         // Returns a Seq[Result]
            val results = client.lookup(address)
            
            val location = results.head.geometry.location
      
        return s"Latitude: ${location.latitude}, Longitude: ${location.longitude}"
      
       }
}
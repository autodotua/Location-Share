package com.autod.gis.locshare.model

import com.google.gson.internal.LinkedTreeMap

import java.text.SimpleDateFormat
import java.util.*

class Location
{

    var id: Int = 0
    var username: String = "";
    var latitude: Double? = null
    var longitude: Double? = null
    var altitude: Double? = null
    var speed: Double? = null
    var accuracy: Double? = null
    var time: Calendar? = null
    var userDisplayName: String? = null

    companion object
    {
        fun convertFromMap(map: LinkedTreeMap<String, Any>): Location
        {
            val location = Location()
            location.id = (map["id"] as Double).toInt()
            location.username = map["username"] as String
            location.latitude = map["latitude"] as Double
            location.longitude = map["longitude"] as Double?
            location.altitude = map["altitude"] as Double?
            location.accuracy = map["accuracy"] as Double?
            location.speed = map["speed"] as Double?
            location.userDisplayName = map["userDisplayName"] as String?
            if (location.userDisplayName.isNullOrEmpty())
            {
                location.userDisplayName = location.username
            }
            val timeFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault())
            try
            {
                val utcTime= timeFormat.parse(map["time"] as String?)
                location.time =  Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                location.time!!.timeInMillis=utcTime.time+TimeZone.getDefault().rawOffset
            }
            catch (ex: Exception)
            {
                location.time=null
            }

//            location.username = map["username"] as String
            return location
        }
    }
}

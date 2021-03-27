package com.autod.gis.locshare.model

import com.google.gson.internal.LinkedTreeMap
import java.text.SimpleDateFormat
import java.util.*

class User
{

   lateinit var name: String
    var password: String? = null
    var displayName: String? = null
    var groupName: String? = null
    var token: String? = null
    @Transient
    var  lastUpdateTime: Calendar? = null
    @Transient
    var lastLocation:Location?=null

    companion object
    {
        var current: User? = null

        fun convertFromMap(map: LinkedTreeMap<String, Any>): User
        {
            val user = User()
            user.name = map["name"] as String
            user.displayName = map["displayName"] as String?
            user.token = map["token"] as String?
            if(user.displayName.isNullOrEmpty())
            {
                user.displayName=user.name
            }
            val timeFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault())
            try
            {
                val utcTime= timeFormat.parse(map["lastUpdateTime"] as String?)
                user.lastUpdateTime =  Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                user.lastUpdateTime!!.timeInMillis=utcTime.time+TimeZone.getDefault().rawOffset
            }
            catch (ex: Exception)
            {
                user.lastUpdateTime = null
            }
            if(map.containsKey("lastLocation")){
                if(map["lastLocation"]==null)
                {
                    user.lastLocation=null;
                }
                else{
                    user.lastLocation=Location.convertFromMap(map["lastLocation"] as  LinkedTreeMap<String, Any>)
                }
            }
            return user
        }
    }
}

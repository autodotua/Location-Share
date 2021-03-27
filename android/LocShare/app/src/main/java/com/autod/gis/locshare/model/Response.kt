package com.autod.gis.locshare.model

import com.google.gson.internal.LinkedTreeMap

class Response
{
    var data: Any? = null
    var message: String? = null
    var succeed: Boolean = false

    fun getLinkedTreeMapData():LinkedTreeMap<String,Any>
    {
        return  data as LinkedTreeMap<String, Any>
    }
    fun getLinkedTreeMapListData():List<LinkedTreeMap<String,Any>>
    {
        return  data as List<LinkedTreeMap<String, Any>>
    }
}

package com.autod.gis.locshare.map

import com.autod.gis.locshare.model.Location
import com.autod.gis.locshare.service.ConfigServer
import com.autod.gis.locshare.ui.MainActivity

interface IMapViewHelper
{
    val activity: MainActivity
    val config: ConfigServer
    var positionning:Boolean
    fun centerToCurrentLocation():Boolean
    fun init()

    fun loadBaseMap()
    fun loadBaseMap(urls:List<String>)

    fun updatePeople(locations: List<Location>)

    fun selectFromButton(userId: String?)

    var mapScale:Double

    fun dispose()


}
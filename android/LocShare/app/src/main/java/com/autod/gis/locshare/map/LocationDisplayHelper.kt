package com.autod.gis.locshare.map

import android.content.DialogInterface
import android.content.Intent
import android.location.LocationManager
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import com.autod.gis.locshare.ui.MainActivity

import com.esri.arcgisruntime.mapping.view.LocationDisplay
import com.esri.arcgisruntime.mapping.view.MapView

class LocationDisplayHelper(val activity: MainActivity, map: EsriMapViewHelper)
{

    private var locationDisplay: LocationDisplay = map.mapView.locationDisplay
    private var hasLocation = false

    private val locationChangedListener =
            LocationDisplay.LocationChangedListener { locationChangedEvent ->
                if (locationChangedEvent.location.position != null)
                {
                    val mapView = map.mapView
                    mapView.setViewpointCenterAsync(locationChangedEvent.location.position, 20000.0)
                    hasLocation = true
                    removeListener()
                }
            }

    private fun removeListener()
    {
        locationDisplay.removeLocationChangedListener(locationChangedListener)
    }

    private var firstBoot = true

    var running: Boolean
        get() = locationDisplay.isStarted
        set(value)
        {
            if (value && !locationDisplay.isStarted)
            {
                locationDisplay.startAsync()
                if (firstBoot)
                {
                    locationDisplay.addLocationChangedListener(locationChangedListener)
                    firstBoot = false
                }
            }
            if (!value && locationDisplay.isStarted)
            {
                locationDisplay.stop()
            }
        }


    fun setPan(): Boolean
    {
        locationDisplay.autoPanMode = LocationDisplay.AutoPanMode.RECENTER
        return hasLocation
    }

    fun showPanModeDialog()
    {
        val items = arrayOf("普通", "置中", "导航", "指南针")
        val listDialog = AlertDialog.Builder(activity)
        listDialog.setTitle("罗盘模式")
        //Toast.makeText(MapFragment.Companion.getInstance(), String.valueOf(locationDisplay.getAutoPanMode()), Toast.LENGTH_SHORT).show();

        listDialog.setItems(items) { dialog: DialogInterface, which: Int ->
            when (which)
            {
                0 -> locationDisplay.autoPanMode = LocationDisplay.AutoPanMode.OFF
                1 -> locationDisplay.autoPanMode = LocationDisplay.AutoPanMode.RECENTER
                2 -> locationDisplay.autoPanMode = LocationDisplay.AutoPanMode.NAVIGATION
                3 -> locationDisplay.autoPanMode = LocationDisplay.AutoPanMode.COMPASS_NAVIGATION
            }

        }
        listDialog.show()

    }

    private fun isGpsAble(lm: LocationManager): Boolean
    {
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }


    //打开设置页面让用户自己设置
    private fun openGPS()
    {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        activity.startActivityForResult(intent, 0)
    }
}

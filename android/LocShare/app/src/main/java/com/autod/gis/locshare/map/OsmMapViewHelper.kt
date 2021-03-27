package com.autod.gis.locshare.map

import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import androidx.preference.PreferenceManager
import com.autod.gis.locshare.model.Location
import com.autod.gis.locshare.service.ConfigServer
import com.autod.gis.locshare.ui.MainActivity
import kotlinx.android.synthetic.main.fragment_map_esri.*
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.MapTileProviderBasic
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.util.MapTileIndex
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.overlay.TilesOverlay
import kotlin.math.log
import kotlin.math.pow
import org.osmdroid.views.overlay.simplefastpoint.LabelledGeoPoint
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.DirectedLocationOverlay
import java.util.ArrayList
import java.util.stream.Collectors
import android.graphics.Paint.Align
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay


class OsmMapViewHelper(override val activity: MainActivity,val mapView: MapView ) : IMapViewHelper
{

    var locationOverlay: DirectedLocationOverlay? = null
    override val config: ConfigServer = activity.config
    override var mapScale: Double
        get() = 5_000_000 / 2.0.pow(mapView.zoomLevelDouble)
        set(value)
        {
            mapView.controller.animateTo(null, log(5_000_000 / value, 2.0), 500)
        }
    var _positionning: Boolean = false
    override var positionning: Boolean
        get() = _positionning
        set(value)
        {
            if (!value)
            {
                if (locationOverlay != null)
                {
                    mapView.overlays.remove(locationOverlay)
                }
            }
            _positionning = value
        }

    override fun centerToCurrentLocation(): Boolean
    {
        if (locationOverlay == null)
        {
            return false
        }
        mapView.controller.animateTo(locationOverlay!!.location, 12.0, null)
        return true

    }

    override fun dispose()
    {
    }


    override fun init()
    {
        Configuration.getInstance().apply {
            load(activity, PreferenceManager.getDefaultSharedPreferences(activity))
            osmdroidBasePath = activity.externalCacheDir
            animationSpeedDefault = 500
        }
        mapView.apply {
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            tilesScaleFactor = 2F
            isFlingEnabled = true
            setMultiTouchControls(true)
        }

    }

    override fun loadBaseMap(urls: List<String>)
    {

    }

    override fun loadBaseMap()
    {
        var first = true
        for (url in config.tileUrls)
        {
            val source = object : OnlineTileSourceBase(url.hashCode().toString(), 0, 19, 256, null, arrayOf(url))
            {
                override fun getTileURLString(pMapTileIndex: Long): String
                {
                    val z = MapTileIndex.getZoom(pMapTileIndex)
                    val x = MapTileIndex.getX(pMapTileIndex)
                    val y = MapTileIndex.getY(pMapTileIndex)
                    return baseUrl.replace("{x}", x.toString()).replace("{y}", y.toString()).replace("{z}", z.toString())
                }
            }
            if (first)
            {
                mapView.setTileSource(source)
                first = false
            }
            else
            {
                val provider = MapTileProviderBasic(activity, source)
                val tilesOverlay = TilesOverlay(provider, activity)
                tilesOverlay.loadingBackgroundColor = Color.TRANSPARENT
                mapView.overlays.add(tilesOverlay)
            }
        }
        mapView.controller.animateTo(GeoPoint(0.0, 0.0), 2.0, null)

    }

    var selectedUserName: String? = null
    fun updateLocation(lat: Double, lng: Double, accuracy: Double)
    {
        if (!positionning)
        {
            return
        }
        var first = false
        if (locationOverlay == null)
        {
            first = true
            val bitmap = Bitmap.createBitmap(40, 40, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint = Paint().apply {
                color = Color.rgb(0x1E, 0x88, 0xE5)
                style = Paint.Style.FILL
            }
            val whitePaint = Paint().apply {
                color = Color.WHITE
                style = Paint.Style.FILL
            }
            canvas.drawCircle(20F, 20F, 20F, whitePaint)
            canvas.drawCircle(20F, 20F, 15F, paint)

            locationOverlay = DirectedLocationOverlay(activity).apply {
                isEnabled = true
                setDirectionArrow(bitmap)

            }
            locationOverlay!!.setAccuracy(1000)
            mapView.overlays.add(locationOverlay)
        }

        locationOverlay!!.setAccuracy(accuracy.toInt())
        locationOverlay!!.location = GeoPoint(lat, lng)
        if (first)
        {
            mapView.controller.animateTo(GeoPoint(lat, lng), 12.0, null)
        }
    }

    private val userNameToPoint = HashMap<String, LabelledGeoPoint>()
    private val pointToUserName = HashMap<LabelledGeoPoint, String>()
    override fun updatePeople(locations: List<Location>)
    {
        userNameToPoint.clear()
        pointToUserName.clear()
        val points = locations.stream().map {
            val point = LabelledGeoPoint(it.latitude!!, it.longitude!!, it.userDisplayName)
            userNameToPoint[it.username] = point
            pointToUserName[point] = it.username
            point
        }.collect(Collectors.toList())
        updateOverlay(ArrayList(points))
    }

    override fun selectFromButton(userName: String?)
    {
        selectedUserName = userName
        if (userName != null)
        {
            val point = userNameToPoint[userName]
            val marker = userNameToMarker[userName]
            if (point != null)
            {
                marker!!.isSelected = true
                if (mapView.zoomLevelDouble < 12)
                {
                    mapView.controller.animateTo(point, 12.0, null)
                }
                else
                {
                    mapView.controller.animateTo(point, null, null)

                }
            }
        }
    }

    private val personOverlays = ArrayList<Overlay>()
    private val userNameToMarker = HashMap<String, AdvancedMarker>()
    private fun updateOverlay(points: ArrayList<LabelledGeoPoint>)
    {
        personOverlays.forEach { mapView.overlays.remove(it) }
        personOverlays.clear()
        userNameToMarker.clear()
        for (point in points)
        {
            val marker = AdvancedMarker(mapView, point.label).apply {
                position = point
                setOnMarkerClickListener { marker, mapView ->
                    if (pointToUserName[point].equals(selectedUserName))
                    {
                        (marker as AdvancedMarker).isSelected = false
//                        activity.mapFragment.updatePersonDetail(null, -1, false)
                        selectedUserName=null
                    }
                    else
                    {
                        (marker as AdvancedMarker).isSelected = true
                        selectedUserName = pointToUserName[point]
//                        activity.mapFragment.updatePersonDetail(selectedUserName, 1, false)

                    }
                    true
                }
            }
            userNameToMarker[pointToUserName[point]!!] = marker
            mapView.overlays.add(marker)
            personOverlays.add(marker)
        }

    }

    internal inner class AdvancedMarker(mapView: MapView, private val label: String) : Marker(mapView)
    {
        private var initialized = false
        private lateinit var normalDrawable: BitmapDrawable
        private lateinit var selectedDrawable: BitmapDrawable

        fun init()
        {
            initialized = true
            val bitmap1 = Bitmap.createBitmap(40, 40, Bitmap.Config.ARGB_8888)
            val paint = Paint().apply {
                color = Color.parseColor("#F57C00")
                style = Paint.Style.FILL
            }
            val strokePaint1 = Paint().apply {
                color = Color.WHITE
                style = Paint.Style.FILL
            }
            Canvas(bitmap1).apply {
                drawCircle(20F, 20F, 20F, strokePaint1)
                drawCircle(20F, 20F, 15F, paint)
            }
            normalDrawable = BitmapDrawable(activity.resources, bitmap1)


            val bitmap2 = Bitmap.createBitmap(48, 48, Bitmap.Config.ARGB_8888)
            val strokePaint2 = Paint().apply {
                color = Color.YELLOW
                style = Paint.Style.FILL
            }
            Canvas(bitmap2).apply {
                drawCircle(24F, 24F, 24F, strokePaint2)
                drawCircle(24F, 24F, 15F, paint)
            }
            selectedDrawable = BitmapDrawable(activity.resources, bitmap2)
        }

        private var textPaint: Paint = Paint().apply {
            color = Color.WHITE
            textSize = 40F
            isAntiAlias = true
            textAlign = Align.CENTER
        }
        private var textStrokePaint: Paint = Paint().apply {
            color = Color.rgb(0x33, 0x33, 0x33)
            textSize = 40F
            isAntiAlias = true
            style = Paint.Style.STROKE
            textAlign = Align.CENTER
            strokeWidth = 12F
        }

        override fun draw(c: Canvas, osmv: MapView, shadow: Boolean)
        {
            draw(c, osmv)
        }

        fun draw(c: Canvas, osmv: MapView)
        {
            if (!initialized)
            {
                init()
            }
            super.draw(c, osmv, false)
            icon = normalDrawable
            val p = this.mPositionPixels  // already provisioned by Marker
            c.drawText(label, p.x.toFloat(), (p.y + 36).toFloat(), textStrokePaint)
            c.drawText(label, p.x.toFloat(), (p.y + 36).toFloat(), textPaint)
        }

        private var _isSelected: Boolean = false
        var isSelected: Boolean
            get() = _isSelected
            set(value)
            {
                if (!initialized)
                {
                    init()
                }
                icon = if (value)
                {
                    selectedDrawable
                }
                else
                {
                    normalDrawable
                }
                _isSelected = value
            }

    }
}




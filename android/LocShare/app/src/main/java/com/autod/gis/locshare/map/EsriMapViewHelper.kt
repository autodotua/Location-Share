package com.autod.gis.locshare.map

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.MotionEvent
import com.autod.gis.locshare.model.Location
import com.autod.gis.locshare.ui.MainActivity
import com.esri.arcgisruntime.arcgisservices.LabelDefinition
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.io.RequestConfiguration
import com.esri.arcgisruntime.layers.Layer
import com.esri.arcgisruntime.layers.WebTiledLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol
import com.esri.arcgisruntime.symbology.SimpleRenderer
import java.util.*

@SuppressLint("StaticFieldLeak")
class EsriMapViewHelper(override val activity: MainActivity,val mapView: MapView)
    : IMapViewHelper
{
    val locationDisplayHelper=LocationDisplayHelper(activity,this)
   override var positionning:Boolean
    get() = locationDisplayHelper.running
    set(value)
    {
        locationDisplayHelper.running=value
    }
    override fun dispose()
    {
        mapView.dispose()
    }
  override  fun centerToCurrentLocation():Boolean
    {
        return locationDisplayHelper.setPan()
    }

    override var mapScale: Double
        get() = mapView.mapScale
        set(value)
        {
            mapView.setViewpointScaleAsync(value)
        }
    override val config = activity.config

    var map: ArcGISMap? = null
        private set

    private var peopleOverlay: GraphicsOverlay? = null

    private val userIdToGraphic = HashMap<String, Graphic>()
    private var selectedUserId: String? = null

    fun linkMapAndMapView()
    {
        mapView.map = map
    }

    fun unlinkMapAndMapView()
    {
        mapView.map = null
    }

    override fun init()
    {
//        mapView = activitymain_map
        mapView.isAttributionTextVisible = false
        linkMapAndMapView()
        mapView.isMagnifierEnabled = true
        mapView.setCanMagnifierPanMap(true)
        setTouchMapView()
    }


    fun loadBaseMap(layer: String)
    {
        map = ArcGISMap(Basemap.Type.valueOf(layer), 30.0, 120.0, 1)
        linkMapAndMapView()
    }

   override fun loadBaseMap(urls: List<String>)
    {
        val layers = ArrayList<Layer>()
        for (url in urls)
        {
            val esriUrl = url.replace("{x}", "{col}").replace("{y}", "{row}").replace("{z}", "{level}")
            val layer = WebTiledLayer(esriUrl)
            val requestConfiguration = RequestConfiguration()
            requestConfiguration.headers["referer"] = "http://www.arcgis.com";
            layer.requestConfiguration = requestConfiguration
            layers.add(layer)
        }
        val basemap = Basemap(layers, null)
        basemap.loadAsync()
        basemap.addDoneLoadingListener {
            map = ArcGISMap(basemap)
            linkMapAndMapView()
        }

    }

    override fun loadBaseMap()
    {
        peopleOverlay = null
        if (!config.esriMap.isNullOrEmpty())
        {
            loadBaseMap(config.esriMap!!)

        }
        else
        {
            loadBaseMap(config.tileUrls)
        }
 }

    override fun updatePeople(locations: List<Location>)
    {
        if (map == null)
        {
            return
        }
        if (peopleOverlay == null)
        {
            peopleOverlay = GraphicsOverlay()
            val symbol = SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.rgb(255, 165, 0), 16f)
            val outLineSymbol = SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.WHITE, 4f)
            symbol.outline = outLineSymbol
            val renderer = SimpleRenderer(symbol)
            peopleOverlay!!.labelDefinitions.add(LabelDefinition.fromJson(labelJson))
            peopleOverlay!!.renderer = renderer
            peopleOverlay!!.isLabelsEnabled = true
            mapView.graphicsOverlays.add(peopleOverlay)
        }
        userIdToGraphic.clear()
        peopleOverlay!!.graphics.clear()
        for (l in locations)
        {
            if (l.longitude == null || l.latitude == null)
            {
                return
            }
            val graphic = Graphic(Point(l.longitude!!,
                    l.latitude!!,
                    SpatialReferences.getWgs84()))
            graphic.attributes["username"] = l.username
            graphic.attributes["label"] = l.userDisplayName

            userIdToGraphic[l.username] = graphic
            peopleOverlay!!.graphics.add(graphic)
            if (selectedUserId != null && selectedUserId == l.username)
            {
                graphic.isSelected = true
            }
        }
    }

    override fun selectFromButton(userId: String?)
    {
        peopleOverlay!!.clearSelection()
        selectedUserId = userId
        if (userId != null)
        {
            val graphic = userIdToGraphic[userId]
            if (graphic != null)
            {
                graphic.isSelected = true
                if (mapView.mapScale < 20000)
                {
                    mapView.setViewpointCenterAsync(graphic.geometry as Point)
                }
                else
                {
                    mapView.setViewpointCenterAsync(graphic.geometry as Point, 20000.0)

                }
            }
        }
    }

    /**
     * 设置单击地图事件
     */
    @SuppressLint("ClickableViewAccessibility")
    fun setTouchMapView()
    {

        mapView.onTouchListener = object : DefaultMapViewOnTouchListener(activity, mapView)
        {

            @SuppressLint("DefaultLocale")
            override fun onSingleTapConfirmed(e: MotionEvent?): Boolean
            {
                if (mapView.map == null || peopleOverlay == null)
                {
                    return super.onSingleTapConfirmed(e)
                }

                try
                {
                    val p = android.graphics.Point(e!!.x.toInt(), e.y.toInt())
                    val result = mapView.identifyGraphicsOverlayAsync(peopleOverlay, p, 5.0, false, 1)
                    result.addDoneListener {
                        try
                        {

                            val graphics = result.get()
                            val graphic = graphics.graphics[0]
                            if (graphic.isSelected)
                            {
                                graphic.isSelected = false
                                selectedUserId = null
//                                activity.mapFragment.updatePersonDetail(null, -1, false)
                            }
                            else
                            {

                                peopleOverlay!!.clearSelection()
                                graphic.isSelected = true
                                val userId = graphic.attributes["username"] as String?
                                selectedUserId = userId
//                                activity.mapFragment.updatePersonDetail(userId, 1, false)
                            }
                            //                            showCallout(graphic);
                        }
                        catch (ex: Exception)
                        {
                        }
                    }
                }
                catch (ex: Exception)
                {
                    ex.printStackTrace()
                }

                return super.onSingleTapConfirmed(e)
            }
        }
    }


    private val labelJson = "{\n" +
            "  \"labelExpressionInfo\": {\n" +
            "    \"expression\": \"\$feature.label\"\n" +
            "  },\n" +
            "  \"maxScale\": 0,\n" +
            "  \"minScale\": 0,\n" +
            "  \"symbol\": {\n" +
            "    \"angle\": 0,\n" +
            "    \"backgroundColor\": [\n" +
            "      0,\n" +
            "      0,\n" +
            "      0,\n" +
            "      0\n" +
            "    ],\n" +
            "    \"borderLineColor\": [\n" +
            "      0,\n" +
            "      0,\n" +
            "      0,\n" +
            "      0\n" +
            "    ],\n" +
            "    \"borderLineSize\": 0,\n" +
            "    \"color\": [\n" +
            "      0,\n" +
            "      0,\n" +
            "      0,\n" +
            "      255\n" +
            "    ],\n" +
            "    \"font\": {\n" +
            "      \"decoration\": \"none\",\n" +
            "      \"size\": 15,\n" +
            "      \"style\": \"normal\",\n" +
            "      \"weight\": \"normal\"\n" +
            "    },\n" +
            "    \"haloColor\": [\n" +
            "      255,\n" +
            "      248,\n" +
            "      220,\n" +
            "      255\n" +
            "    ],\n" +
            "    \"haloSize\": 3.0,\n" +
            "    \"horizontalAlignment\": \"center\",\n" +
            "    \"kerning\": false,\n" +
            "    \"type\": \"esriTS\",\n" +
            "    \"verticalAlignment\": \"middle\",\n" +
            "    \"xoffset\": 0,\n" +
            "    \"yoffset\": 0\n" +
            "  }\n" +
            "}"


//        val instance = EsriMapViewHelper()

}

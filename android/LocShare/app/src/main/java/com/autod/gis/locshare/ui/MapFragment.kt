package com.autod.gis.locshare.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.autod.gis.locshare.R
import com.autod.gis.locshare.map.EsriMapViewHelper
import com.autod.gis.locshare.map.IMapViewHelper
import com.autod.gis.locshare.map.OsmMapViewHelper
import com.autod.gis.locshare.model.Location
import com.autod.gis.locshare.model.User
import com.autod.gis.locshare.service.ConfigServer
import com.autod.gis.locshare.service.LocationService
import com.autod.gis.locshare.service.NetworkService
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.fragment_map_esri.*
import kotlinx.android.synthetic.main.fragment_map_esri.view.*
import kotlinx.android.synthetic.main.fragment_map_osm.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.stream.Collectors


class MapFragment : Fragment(), View.OnClickListener, View.OnLongClickListener
{

    lateinit var mapViewHelper: IMapViewHelper
//    private lateinit var locationDetailBarHelper: LocationDetailBarHelper
    private var mainLayout: RelativeLayout? = null
    private val locationButtons = HashMap<String, Button>()
//    private val locationDetails = HashMap<String, Location>()
    private var selectedUserId: String? = null
    private lateinit var config: ConfigServer
    private var initialized = false

    override fun onAttach(context: Context?)
    {
        super.onAttach(context)
        config = (activity as MainActivity).config
    }


    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View?
    {

        return when (config.mapSdk)
        {
            "esri" ->
            {
                inflater.inflate(R.layout.fragment_map_esri, container, false)
            }
            "osmdroid" ->
            {
                inflater.inflate(R.layout.fragment_map_osm, container, false)
            }
            else -> throw  java.lang.Exception()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        setMapSdk(config.mapSdk, false)
//        locationDetailBarHelper = LocationDetailBarHelper(activity!! as MainActivity)
//        locationDetailBarHelper.init()
        init()
    }

    private fun setMapSdk(sdk: String, dispose: Boolean)
    {
        when (sdk)
        {
            "esri" ->
            {
                mapViewHelper = EsriMapViewHelper(activity!! as MainActivity, map_esri)
            }
            "osmdroid" ->
            {
                mapViewHelper = OsmMapViewHelper(activity!! as MainActivity, map_osm)
            }
        }

    }

    /**
     * 菜单的点击事件
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {

//        MenuHelper.menuClick(item)

        return true
    }

    /**
     * 初始化
     */
    private fun init()
    {
        mapViewHelper.init()
        initializeControls()

        try
        {
            mapViewHelper.loadBaseMap()
        }
        catch (ex: Exception)
        {
            Toast.makeText(activity, "加载底图失败", Toast.LENGTH_SHORT).show()
        }



        LocationService.setRunning(activity!!, config.isLocationOn)
        mainLayout = view!!.map_root

        initialized = true
    }

    fun changeLocationStatus()
    {
        changeLocationStatus(config.isLocationOn)
    }

    fun changeLocationStatus(on: Boolean)
    {
        LocationService.setRunning(activity!!, on)
        mapViewHelper.positionning = on
    }




//    private fun setInfoBoxButtons(locations: List<Location>)
//    {
//        activity!!.runOnUiThread {
//            //删除更新中不再存在的按钮
//            for (id in locationButtons.keys.toSet())
//            {
//                if (locations.stream().noneMatch { p -> p.username == id })
//                {
//
//                    view!!.llt_info_person_list.removeView(locationButtons[id])
//                    locationButtons.remove(id)
//                }
//            }
//            for (l in locations)
//            {
//                if (locationButtons.containsKey(l.username))
//                {
//                    if (l.username == selectedUserId)
//                    {
//                        updatePersonDetail(l.username, 0, true)
//                    }
//                }
//                else
//                {
//                    val btn = Button(activity!!)
//                    btn.setBackgroundColor(Color.rgb(0xEE, 0xEE, 0xEE))
//                    btn.elevation = 0f
//                    btn.text = l.userDisplayName
//                    btn.setOnClickListener { v ->
//                        if (btn.isSelected)
//                        {
//                            updatePersonDetail(null, 0, true)
//                        }
//                        else
//                        {
//                            updatePersonDetail(l.username, 0, true)
//                        }
//                    }
//                    view!!.llt_info_person_list.addView(btn)
//                    locationButtons[l.username] = btn
//                }
//            }
//        }
//    }

//    @SuppressLint("SetTextI18n")
//    fun updatePersonDetail(userId: String?, expandOrShrinkPanel: Int, selectOnMap: Boolean)
//    {
//        for ((key, value) in locationButtons)
//        {
//            if (key == userId)
//            {
//                value.isSelected = true
//                value.setBackgroundColor(Color.rgb(0xCC, 0xCC, 0xCC))
//            }
//            else
//            {
//                value.isSelected = false
//                value.setBackgroundColor(Color.rgb(0xEE, 0xEE, 0xEE))
//            }
//        }
//        if (selectOnMap)
//        {
//            mapViewHelper.selectFromButton(userId)
//        }
//        selectedUserId = userId
//        if (userId == null)
//        {
//            view!!.llt_info_person_detail.visibility = View.INVISIBLE
//            view!!.tvw_info_name.text = null
//            view!!.tvw_info_time.text = null
//            view!!.tvw_info_lat.text = null
//            view!!.tvw_info_lng.text = null
//            view!!.tvw_info_alt.text = null
//            return
//        }
//        if (!locationDetails.containsKey(userId))
//        {
//            return
//        }
//        view!!.llt_info_person_detail.visibility = View.VISIBLE
//
//        val l = locationDetails[userId]!!
//
//        view!!.tvw_info_name.text = l.userDisplayName
//        try
//        {
//            val time = l.time!!
//            val now = Calendar.getInstance()
//            var timeStr = ""
//            if (now.get(Calendar.DAY_OF_YEAR) != time.get(Calendar.DAY_OF_YEAR))
//            {
//                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
//                timeStr = dateFormat.format(time.time) + "  "
//            }
//            val timerFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
//            view!!.tvw_info_time.text = timeStr + timerFormat.format(time.time)
//        }
//        catch (ex: Exception)
//        {
//            view!!.tvw_info_time.text = "时间格式错误"
//        }
//
//        val format6 = DecimalFormat("#.000000°")
//        val format2 = DecimalFormat("#.00m")
//        view!!.tvw_info_lat.text = format6.format(l.latitude)
//        view!!.tvw_info_lng.text = format6.format(l.longitude)
//        if (l.altitude != null)
//        {
//            view!!.tvw_info_alt.text = format2.format(l.altitude!!)
//        }
//        else
//        {
//            view!!.tvw_info_alt.text = "未知"
//        }
//        if (l.accuracy != null)
//        {
//            view!!.tvw_info_acc.text = l.accuracy!!.toInt().toString() + "m"
//        }
//        else
//        {
//        view!!.tvw_info_acc.text = "未知"
//    }
//
//        if (expandOrShrinkPanel == 1)
//        {
//            locationDetailBarHelper.expand()
//        }
//        else if (expandOrShrinkPanel == -1)
//        {
//            locationDetailBarHelper.shrink()
//        }
//    }


    override fun onResume()
    {
        if (initialized)
        {

            try
            {
                mapViewHelper.positionning = config.isLocationOn
            }
            catch (ex: Exception)
            {
            }

        }


        //  mapView.resume();
        super.onResume()

    }


    override fun onStop()
    {
        if (initialized)
        {

            //mapViewHelper.getInstance().unlinkMapAndMapView();
            try
            {
                if (config.isLocationOn)
                {
//                    LocationService.setRunning(config.isLocationOnBackground)
//                    locationDisplayHelper!!.setRunning(config.isLocationOnBackground)
                }
                mapViewHelper.positionning = false

            }
            catch (ex: Exception)
            {
            }

        }
        super.onStop()
    }

    override fun onDestroy()
    {
        super.onDestroy()
        if (initialized)
        {
            mapViewHelper.dispose()
            LocationService.setRunning(activity!!, false)

            mapViewHelper.positionning = false

            //应该是由于ArcGIS Runtime用了native库，内存管理有点奇怪，退出以后还会残留，所以只好退出前杀了进程
            android.os.Process.killProcess(android.os.Process.myPid())
//            mapViewHelper.unlinkMapAndMapView()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)
    }
    /**
     * 初始化字段
     */
    private fun initializeControls()
    {
        view!!.main_btn_pan.setOnClickListener(this)
        view!!.main_btn_pan.setOnLongClickListener(this)

        view!!.main_btn_zoom_in.setOnClickListener(this)
        view!!.main_btn_zoom_in.setOnLongClickListener(this)

        view!!.main_btn_zoom_out.setOnClickListener(this)
        view!!.main_btn_zoom_out.setOnLongClickListener(this)

//        locationDetailBarHelper.init()
    }


    override fun onLongClick(v: View): Boolean
    {
        val scale: Double
        when (v.id)
        {

            R.id.main_btn_pan -> pan@
            {
                if (!(mapViewHelper is EsriMapViewHelper))
                {
                    return@pan
                }
                if (!config.isLocationOn)
                {
                    Toast.makeText(activity, "没有开启定位功能", Toast.LENGTH_SHORT).show()
                    return@pan
                }
                (mapViewHelper as EsriMapViewHelper).locationDisplayHelper.showPanModeDialog()
            }

            R.id.main_btn_zoom_in -> try
            {
                mapViewHelper.mapScale = mapViewHelper.mapScale * 0.1
            }
            catch (ex: Exception)
            {

            }

            R.id.main_btn_zoom_out -> try
            {
                mapViewHelper.mapScale = mapViewHelper.mapScale * 10
            }
            catch (ex: Exception)
            {

            }

        }

        return true
    }

    override fun onClick(view: View)
    {
        val intent: Intent
        val scale: Double
        when (view.id)
        {

            R.id.main_btn_pan -> pan@
            {
                if (!config.isLocationOn)
                {
                    Toast.makeText(activity, "没有开启定位功能", Toast.LENGTH_SHORT).show()
                    return@pan
                }
                if (!mapViewHelper.centerToCurrentLocation())
                {
                    Toast.makeText(activity, "还没有定位", Toast.LENGTH_SHORT).show()
                    return@pan
                }
                if (LocationService.lastLocation == null)
                {
                    Toast.makeText(activity, "定位可能还没有成功", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.main_btn_zoom_in -> try
            {
                mapViewHelper.mapScale = mapViewHelper.mapScale * 0.5
            }
            catch (ex: Exception)
            {

            }

            R.id.main_btn_zoom_out -> try
            {
                mapViewHelper.mapScale = mapViewHelper.mapScale * 2
            }
            catch (ex: Exception)
            {

            }

        }
    }
}

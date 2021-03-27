package com.autod.gis.locshare.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.autod.gis.locshare.R
import com.autod.gis.locshare.model.User
import com.autod.gis.locshare.service.ConfigServer
import kotlinx.android.synthetic.main.activity_main.*
import android.content.*
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.preference.PreferenceManager
import com.autod.gis.locshare.model.GetOption
import com.autod.gis.locshare.service.LocationService
import com.google.android.material.snackbar.Snackbar
import java.util.*
import androidx.viewpager.widget.ViewPager
import com.autod.gis.locshare.map.OsmMapViewHelper
import com.autod.gis.locshare.model.Location
import com.autod.gis.locshare.service.NetworkService
import kotlinx.android.synthetic.main.bar_switch.view.*
import java.util.stream.Collectors


class MainActivity : AppCompatActivity()
{
    private var timer: Timer? = null
    lateinit var mapFragment: MapFragment
    private lateinit var personFragment: PersonFragment
    private lateinit var preferenceFragment: PreferenceFragment
    lateinit var config: ConfigServer

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        this.window.decorView.setBackgroundColor(getColor(R.color.colorPrimary))
        setContentView(R.layout.activity_main)
        checkPermission()
        init()
        initViews()
        checkUser()
    }


    private fun checkUser()
    {
        User.current = config.user
        if (User.current == null)
        {
            startActivity(Intent(this, LoginActivity::class.java))
            return
        }
//        NetworkService.checkToken(this) { succeed, message, response ->
//            if (!succeed)
//            {
//                Toast.makeText(this, "查询登录信息失败，可能网络有错误", Toast.LENGTH_SHORT).show()
//                return@checkToken
//            }
//            if (!response!!.succeed)
//            {
////                Toast.makeText(this,"登录信息失效，请重新登录",Toast.LENGTH_SHORT).show()
//                if (!LoginActivity.hasInstance)
//                {
//                    val intent = Intent(this, LoginActivity::class.java)
//                    intent.putExtra("message", "登录信息失效，请重新登录")
//                    startActivity(intent)
//                }
//            }
//        }
    }


//    private lateinit var menu: Menu
//    override fun onCreateOptionsMenu(menu: Menu): Boolean
//    {
//        this.menu = menu
//
//        menuInflater.inflate(R.menu.menu, menu)
//        val switchLocation = menu.findItem(R.id.menu_location).actionView.menu_switch_location
//        switchLocation.isChecked = config.isLocationOn
//        switchLocation.setOnCheckedChangeListener { _, isChecked ->
//            config.isLocationOn = isChecked
//            preferenceFragment.setLocationOn(isChecked)
//            mapFragment.changeLocationStatus(isChecked)
//        }
//
//        return true // true：允许创建的菜单显示出来，false：创建的菜单将无法显示。
//    }

    override fun onResume()
    {
        super.onResume()
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                IntentFilter("LocationServiceEvent"))
        if (timer == null)
        {
            setTimer()
        }
    }

    override fun onStop()
    {
        super.onStop()
        if (timer != null)
        {
            timer!!.cancel()
            timer = null
        }
    }

    override fun onDestroy()
    {
        super.onDestroy()
        if (timer != null)
        {
            timer!!.cancel()
        }
    }

    private var neverShowError = false
    private val mMessageReceiver = object : BroadcastReceiver()
    {
        override fun onReceive(context: Context, intent: Intent)
        {

            when (intent.getStringExtra("type"))
            {
                "UploadFailed" ->
                {
                    Snackbar.make(this@MainActivity.main_root, "上传当前位置失败：" + intent.getStringExtra("msg"), 5000)
                            .setAction("不再提醒") { neverShowError = true }.show()
                }
                "NoGPS" ->
                {
                    AlertDialog.Builder(this@MainActivity)
                            .setTitle("设备还没有开启位置信息")
                            .setMessage("需要打开设备信息功能后，才可以使用定位功能。否则，定位服务将无法开启。但是仍然可以看到其他成员的位置。")
                            .setPositiveButton("跳转到设置") { _, _ ->
                                //                                MenuHelper.setLocationSwitch(false)
                                LocationService.setRunning(this@MainActivity, false)
                                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))

                            }
                            .setNegativeButton("取消") { _, _ ->
                                //                                MenuHelper.setLocationSwitch(false)
                                LocationService.setRunning(this@MainActivity, false)
                            }
                            .create().show()

                }
                "LocationChanged" ->
                {
                    if (mapFragment.mapViewHelper is OsmMapViewHelper)
                    {
                        (mapFragment.mapViewHelper as OsmMapViewHelper)
                                .updateLocation(intent.getDoubleExtra("lat", 0.0),
                                        intent.getDoubleExtra("lng", 0.0),
                                        intent.getDoubleExtra("acc", 0.0))
                    }
                }
            }


        }
    }
    lateinit var pagerAdapter: PagerAdapter
    private fun init()
    {
        config = ConfigServer(this, PreferenceManager.getDefaultSharedPreferences(applicationContext))
        pagerAdapter = PagerAdapter(supportFragmentManager)
        mapFragment = pagerAdapter.getItem(0) as MapFragment
        personFragment = pagerAdapter.getItem(1) as PersonFragment
        preferenceFragment = pagerAdapter.getItem(2) as PreferenceFragment

//        config=ConfigServer(this,preferenceFragment!!.preferenceManager.sharedPreferences)
        title = "位置共享"
        GetOption.current = config.getOption
        User.current = config.user

        main_pagers.adapter = pagerAdapter
        main_tab.setupWithViewPager(main_pagers)
        main_pagers.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener()
        {
            override fun onPageSelected(position: Int)
            {
                super.onPageSelected(position)
                changeMenu(position)
            }
        })

        setTimer()
    }

    private fun changeMenu(position: Int)
    {
        main_swt_location.visibility = if (position == 0) View.VISIBLE else View.INVISIBLE
        main_btn_group.visibility = if (position == 1) View.VISIBLE else View.INVISIBLE
//        menu.findItem(R.id.menu_location).isVisible = position == 0
//        menu.findItem(R.id.menu_group).isVisible = position == 1
    }

    //    override fun onOptionsItemSelected(item: MenuItem?): Boolean
//    {
//        when (item!!.itemId)
//        {
//            R.id.menu_group ->
//            {
//                val ett = EditText(this)
//                        .apply {
//                            setText(User.current?.groupName)
//                        }
//                val show: Any = AlertDialog.Builder(this)
//                        .setTitle(getString(R.string.dialog_change_group_title))
//                        .setMessage(getString(R.string.dialog_change_group_message))
//                        .setView(ett)
//                        .setPositiveButton(getString(R.string.dialog_button_ok)) { _, _ ->
//                            val text = ett.text.toString()
//                            NetworkService.setUserInfo(this,User().apply { groupName = text }) { succeed, message, response ->
//                                if (succeed)
//                                {
//                                    User.current!!.groupName = text
//                                    config.user = User.current!!
//                                    personFragment.updateMembers()
//                                }
//                                else
//                                {
//                                    SnackBarHelper.show(this, "切换组失败", "changeGroup")
//                                }
//                            }
//                        }
//                        .create().show()
//            }
//        }
//        return super.onOptionsItemSelected(item)
//    }
    private fun setTimer()
    {
        timer = Timer()
        @Suppress("UNCHECKED_CAST")
        val task = object : TimerTask()
        {
            override fun run()
            {
                if (User.current == null || LoginActivity.hasInstance)
                {
                    return
                }
                getAll()
            }
        }
        timer!!.schedule(task, 0, 10000)
    }

    private fun getAll()
    {
        NetworkService.getAll(this@MainActivity) getAll@{ succeed, message, response ->
            if (response == null)
            {
                return@getAll
            }
            val userMap = response.getLinkedTreeMapListData()
            val users = userMap.stream().map { User.convertFromMap(it) }.collect(Collectors.toList())
            //                locationDetails.clear()
            //                locations.forEach { p -> locationDetails[p.username] = p }
            val now = Calendar.getInstance(TimeZone.getTimeZone("utc"))
            mapFragment.mapViewHelper.updatePeople(users.stream()
                    .filter {
                        if (it.lastLocation == null)
                        {
                            return@filter false
                        }
                        val addedTime = it.lastLocation!!.time!!.clone() as Calendar
                        addedTime.add(Calendar.SECOND, GetOption.current.time)
                        addedTime.after(now)
                    }
                    .map { it.lastLocation!! }.collect(Collectors.toList()))
            personFragment.updateData(users)
            //                    setInfoBoxButtons(locations)
        }
    }

    private fun initViews()
    {
        main_swt_location.isChecked = config.isLocationOn
        main_swt_location.setOnCheckedChangeListener { _, isChecked ->
            config.isLocationOn = isChecked
            preferenceFragment.setLocationOn(isChecked)
            mapFragment.changeLocationStatus(isChecked)
        }
//        main_btn_group.visibility = View.GONE
        main_btn_group.setOnClickListener {
            val ett = EditText(this)
                    .apply {
                        setText(User.current?.groupName)
                    }
            AlertDialog.Builder(this)
                    .setTitle(getString(R.string.dialog_change_group_title))
                    .setMessage(getString(R.string.dialog_change_group_message))
                    .setView(ett)
                    .setPositiveButton(getString(R.string.dialog_button_ok)) { _, _ ->
                        val text = ett.text.toString()
                        NetworkService.setUserInfo(this, User().apply { groupName = text }) { succeed, message, response ->
                            if (succeed)
                            {
                                User.current!!.groupName = text
                                config.user = User.current!!
                                getAll()
//                                personFragment.updateMembers()
                            }
                            else
                            {
                                SnackBarHelper.show(this, "切换组失败", "changeGroup")
                            }
                        }
                    }
                    .create().show()
        }
//        main_tvw_title.text = "地图"
//        supportActionBar!!.elevation = 0F
    }

    /**
     * 检查权限
     */
    private fun checkPermission()
    {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        )
        {
            if(android.os.Build.VERSION.SDK_INT>=29)
            {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE), 1024)
            }
            else{
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1024)

            }
        }

    }

    /**
     * 权限申请回复
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1024)
        {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                //                init();
            }
            else
            {
                //如果没有申请到权限则重复申请
                Toast.makeText(this, "必须授予所有权限才能使用本软件", Toast.LENGTH_SHORT).show()
                checkPermission()
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            moveTaskToBack(false)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}


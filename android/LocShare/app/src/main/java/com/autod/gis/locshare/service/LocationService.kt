package com.autod.gis.locshare.service

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager

import com.autod.gis.locshare.R
import com.autod.gis.locshare.model.User
import com.autod.gis.locshare.ui.MainActivity
import com.autod.gis.locshare.ui.MapFragment

import java.util.*
import java.text.SimpleDateFormat


class LocationService : Service()
{
    interface Alert
    {
        fun showAlert();
    }

    private val alert: Alert? = null
    private var isGpsOn: Boolean = false
    private var hasMinInterval = false

    private var locationManager: LocationManager? = null
    private var notificationBuilder: Notification.Builder? = null
    private var notificationManager: NotificationManager? = null
    private val notificationId = 1024
    private var noLongerShowError = false
    private val timer = Timer()

    private lateinit var config: ConfigServer

    private var listener: LocationListener = object : LocationListener
    {
        override fun onLocationChanged(location: Location)
        {
            if (User.current == null)
            {
                return
            }

            val transLocation = com.autod.gis.locshare.model.Location()
            transLocation.latitude = location.latitude
            transLocation.longitude = location.longitude
            transLocation.altitude = if (location.altitude == 0.0) null else location.altitude
            transLocation.accuracy = if (location.accuracy == 0F) null else location.accuracy.toDouble()
            lastLocation = location

            val intent = Intent("LocationServiceEvent").apply {
                putExtra("type", "LocationChanged")
                putExtra("lat",location.latitude)
                putExtra("lng",location.longitude)
                putExtra("acc",location.accuracy)
            }
            LocalBroadcastManager.getInstance(this@LocationService).sendBroadcast(intent)

            NetworkService.update(this@LocationService,transLocation) { succeed, message, response ->
                if (!succeed)
                {
                    if (!noLongerShowError)
                    {

                        val uploadFailedIntent = Intent("LocationServiceEvent").apply {
                            putExtra("type", "UploadFailed")
                            putExtra("msg", message)
                        }
                        LocalBroadcastManager.getInstance(this@LocationService).sendBroadcast(uploadFailedIntent)
                    }
                }
                else
                {
                    val format = SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                    updateNotification(null, "上次定位并上传时间：" + format.format(Calendar.getInstance().time))
                }
            }

            if (hasMinInterval)
            {
                closeGps()
            }
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle)
        {

        }

        override fun onProviderEnabled(provider: String)
        {
            if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this@LocationService, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                return
            }

        }

        override fun onProviderDisabled(provider: String)
        {

        }
    }


    override fun onBind(intent: Intent): IBinder?
    {
        // TODO: Return the communication channel to the service.
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int
    {
        config = ConfigServer(applicationContext, PreferenceManager.getDefaultSharedPreferences(applicationContext))
        initNotification()
        if (locationManager == null)
        {
            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        }
        if (!openGps())
        {
            return START_NOT_STICKY
        }

        if (config.positioningInterval > 0)
        {
            hasMinInterval = true
            updateNotification("正在间断定位：每" + config.positioningInterval / 60 + "分钟", null)
            startTimer()
        }


        return START_STICKY
    }

    private fun openGps(): Boolean
    {
        if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            Toast.makeText(this, "没有定位权限", Toast.LENGTH_SHORT).show()

            return false
        }


        locationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, listener)
        if (!locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            val uploadFailedIntent = Intent("LocationServiceEvent").apply { putExtra("type", "NoGPS") }
            LocalBroadcastManager.getInstance(this).sendBroadcast(uploadFailedIntent)

            return false
        }
        isGpsOn = true
        return true
    }

    private fun closeGps()
    {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return
        }
        locationManager!!.removeUpdates(listener)
        isGpsOn = false
    }

    private fun startTimer()
    {

        val handler=Handler()
        handler.postDelayed(object :Runnable {
            override fun run()
            {
                if (!isGpsOn)
                {
//                    Looper.prepare()
                    openGps()
//                    Looper.loop()
                }
                handler.postDelayed(this,config.positioningInterval * 1000L)
            }
        },config.positioningInterval * 1000L)

//        val task = object : TimerTask()
////        {
////            override fun run()
////            {
////                if (!isGpsOn)
////                {
////                    Looper.prepare()
////                    openGps()
////                    Looper.loop()
////                }
////            }
////        }
////        val seconds = config.positioningInterval * 1000L
////        timer.schedule(task, seconds, seconds)
    }

    override fun onDestroy()
    {
        isRunning = false
        if (notificationManager != null)
        {
            notificationManager!!.cancel(notificationId)
        }
        stopForeground(true)
        super.onDestroy()
        if (hasMinInterval)
        {
            timer.cancel()
        }


        closeGps()
    }


    private fun initNotification()
    {
        notificationBuilder = Notification.Builder(this) //获取一个Notification构造器
        val nfIntent = Intent(this, MainActivity::class.java)


        notificationBuilder!!
                .setContentIntent(PendingIntent.getActivity(this, 0, nfIntent, 0)) // 设置PendingIntent
                .setLargeIcon(BitmapFactory.decodeResource(this.resources, R.mipmap.ic_launcher)) // 设置下拉列表中的图标(大图标)
                .setContentTitle("正在不间断定位中") // 设置下拉列表里的标题
                .setContentText("正在定位中")
                .setSmallIcon(R.drawable.ic_positionning) // 设置状态栏内的小图标
                //.setContentText("要显示的内容") // 设置上下文内容
                .setWhen(System.currentTimeMillis()) // 设置该通知发生的时间


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
        {

            val channelId = "com.autod.gis.locshare"
            val channelName = "定位上传"
            val notificationChannel = NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_LOW)
            notificationChannel.enableLights(false)
            //notificationChannel.setLightColor(Color.RED);
            notificationChannel.setShowBadge(true)
            notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager!!.createNotificationChannel(notificationChannel)

            notificationBuilder!!.setChannelId(channelId)
        }

        val notification = notificationBuilder!!.build() // 获取构建好的Notification
        startForeground(notificationId, notification)


    }

    private fun updateNotification(title: String?, message: String?)
    {
        if (notificationBuilder == null || notificationManager == null)
        {
            return
        }
        if (title != null)
        {
            notificationBuilder!!.setContentTitle(title)
        }
        if (message != null)
        {
            notificationBuilder!!.setContentText(message)
        }
        notificationManager!!.notify(notificationId, notificationBuilder!!.build())
    }


    companion object
    {
        private var isRunning = false

        fun setRunning(activity: Activity, running: Boolean)
        {
            if (isRunning && !running)
            {
                activity.stopService(Intent(activity, LocationService::class.java))
            }
            else if (!isRunning && running)
            {
                val intent = Intent(activity, LocationService::class.java)
                activity.startService(Intent(activity, LocationService::class.java))
            }

            isRunning = running
        }

        fun startOrRestart(activity: Activity)
        {
            setRunning(activity, false)
            setRunning(activity, true)

        }

        var lastLocation: Location? = null
    }


}

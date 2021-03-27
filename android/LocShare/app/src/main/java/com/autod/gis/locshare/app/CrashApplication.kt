package com.autod.gis.locshare.app

import android.app.Application
import android.os.Debug

/**
 * 能够捕获异常的Application继承类
 */
class CrashApplication : Application()
{
    override fun onCreate()
    {
        super.onCreate()
        if (!Debug.isDebuggerConnected())
        {
            val crashHandler = CrashHandler(this)
            crashHandler.init(applicationContext)
        }
    }
}
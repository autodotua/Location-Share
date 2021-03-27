package com.autod.gis.locshare.service

import android.content.Context
import android.content.SharedPreferences
import com.autod.gis.locshare.R
import com.autod.gis.locshare.model.GetOption

import com.autod.gis.locshare.model.User
import com.google.gson.Gson

class ConfigServer(private val context: Context, val preferences: SharedPreferences)
{
    var isLocationOn: Boolean
        get() = preferences.getBoolean(context.getString(R.string.key_location), true)
        set(value)
        {
            val editor = preferences.edit()
            editor.putBoolean(context.getString(R.string.key_location), value)
            editor.apply()

        }

    var tileUrls: List<String>
        get()
        {
            val json = preferences.getString(context.getString(R.string.key_tiles), "http://t0.tianditu.com/vec_w/wmts?service=WMTS&request=GetTile&version=1.0.0&layer=vec&style=default&TILEMATRIXSET=w&format=tiles&height=256&width=256&tilematrix={z}&tilerow={y}&tilecol={x}&tk=4cb121d316f53f85357887949e827fd4\nhttp://t0.tianditu.com/cva_w/wmts?service=WMTS&request=GetTile&version=1.0.0&layer=cva&style=default&TILEMATRIXSET=w&format=tiles&height=256&width=256&tilematrix={z}&tilerow={y}&tilecol={x}&tk=4cb121d316f53f85357887949e827fd4")

            return json!!.split("\n").filter { it!="" }
        }
       private set(value)
        {
        }


    var user: User?
        get()
        {
            val json = preferences.getString(context.getString(R.string.key_user), null) ?: return null
            val gson = Gson()
            return gson.fromJson(json, User::class.java)
        }
        set(user)
        {
            val editor = preferences.edit()
            val gson = Gson()
            editor.putString(context.getString(R.string.key_user), gson.toJson(user))
            editor.apply()

        }

    var esriMap: String?
        get() = preferences.getString(context.getString(R.string.key_esri), null)
        set(value)
        {
            val editor = preferences.edit()
            editor.putString(context.getString(R.string.key_esri), value)
            editor.apply()
        }
    var positioningInterval: Int
        get() = preferences.getInt(context.getString(R.string.key_positioning_Interval), 0)
        set(index)
        {
            val editor = preferences.edit()
            editor.putInt(context.getString(R.string.key_positioning_Interval), index)
            editor.apply()
        }

    var getOption:GetOption
        get()
        {
            val gson = Gson()
            val json = preferences.getString(context.getString(R.string.key_get_option), null) ?: return GetOption()
            return gson.fromJson(json,GetOption::class.java)
        }
        set(value)
        {
            val editor = preferences.edit()
            val gson = Gson()
            editor.putString(context.getString(R.string.key_get_option), gson.toJson(value))
            editor.apply()
        }
    var mapSdk: String
        get() = preferences.getString(context.getString(R.string.key_map_sdk), "esri")!!
        set(value)
        {
            val editor = preferences.edit()
            editor.putString(context.getString(R.string.key_map_sdk), value)
            editor.apply()
        }
}

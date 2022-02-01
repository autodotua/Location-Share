package com.autod.gis.locshare.service

import android.content.Context
import android.content.Intent
import android.os.Looper
import com.autod.gis.locshare.model.*
import com.autod.gis.locshare.ui.LoginActivity
import java.io.InputStreamReader
import com.google.gson.Gson
import java.nio.Buffer
import java.io.BufferedReader
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL


object NetworkService
{

    private const val rootUrl = "http://192.168.1.10:6390/"
    //private val rootUrl = "http://locshare.autodotua.top/api/"
    //   private val rootUrl = "https://autodotua.top/locshare/api/"


    fun setUserInfo(context: Context, user: User, callback: (succeed: Boolean, message: String?, response: Response?) -> Unit)
    {
        post(context, "user/userInfo", user, callback)
    }


    fun getAll(context: Context, callback: (succeed: Boolean, message: String?, response: Response?) -> Unit)
    {
        get(context, "location/get?time="+ GetOption.current.time, callback)
    }

    fun update(context: Context, location: Location, callback: (succeed: Boolean, message: String?, response: Response?) -> Unit)
    {
        post(context, "location/update", location, callback)
    }

    fun hide(context: Context, callback: (succeed: Boolean, message: String?, response: Response?) -> Unit)
    {
        post(context, "location/hide", null, callback)
    }

    fun signIn(context: Context, user: User, callback: (succeed: Boolean, message: String?, response: Response?) -> Unit)
    {
        post(context, "user/signIn", user, callback)
    }

    fun signUp(context: Context, user: User, callback: (succeed: Boolean, message: String?, response: Response?) -> Unit)
    {
        post(context, "user/signUp", user, callback)
    }


    private fun get(context: Context, subUrl: String,
                         callback: (succeed: Boolean, message: String?, response: Response?) -> Unit?)
    {
        send(context, subUrl, "GET",null, callback)
    }
    private fun <T> post(context: Context, subUrl: String, data: T?,
                         callback: (succeed: Boolean, message: String?, response: Response?) -> Unit?)
    {
        send(context, subUrl, "POST",data, callback)
    }

    private fun <T> send(context: Context, subUrl: String, method: String, data: T?,
                         callback: (succeed: Boolean, message: String?, response: Response?) -> Unit?)
    {
        //开启线程，发送请求
        Thread {
            var connection: HttpURLConnection? = null
            var reader: BufferedReader? = null
            try
            {
                val url = URL(rootUrl + subUrl)
                connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = method
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                var dataBytes: ByteArray = ByteArray(0)
                if (method == "POST")
                {
                    val request = Request<T>()
                    request.data = data
                    val dataJson = Gson().toJson(request)
                    dataBytes = dataJson.toByteArray();
                }
                connection.setRequestProperty("Authorization", User.current?.token)
                if (method == "POST")
                {
                    connection.setRequestProperty("Content-Length", dataBytes.size.toString())
                }
                connection.setRequestProperty("Content-Type", "application/json")
                if (method == "POST")
                {
                    connection.doOutput = true
                    connection.outputStream.write(dataBytes)
                }
                val code = connection.responseCode

                if (code == 401)
                {
                    if (!LoginActivity.hasInstance)
                    {
                        val intent = Intent(context, LoginActivity::class.java)
                        intent.putExtra("message", "登录信息失效，请重新登陆")
                        intent.putExtra("deleteUser", true)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    }
                    return@Thread
                }
                if (code / 100 == 2)
                {                //返回输入流
                    val inputStream = connection.inputStream
                    var text = inputStream.bufferedReader().use(BufferedReader::readLine);
                    Looper.prepare()
                    val response = Gson().fromJson(text, Response::class.java)
                        callback(true, null, response)
                    Looper.loop()
                }
                else
                {
                    Looper.prepare()
                    callback(false, "返回$code", null)
                    Looper.loop()
                }
            }
            catch (ex: Exception)
            {
                if (Looper.myLooper() == null)
                {
                    Looper.prepare()
                    callback(false, ex.message, null)
                    Looper.loop()
                }
                else
                {
                    callback(false, ex.message, null)
                }
                ex.printStackTrace()
            }
            finally
            {
                if (reader != null)
                {
                    try
                    {
                        reader.close()
                    }
                    catch (e: IOException)
                    {
                        e.printStackTrace()
                    }

                }
                connection?.disconnect()
            }
        }.start()
    }


}

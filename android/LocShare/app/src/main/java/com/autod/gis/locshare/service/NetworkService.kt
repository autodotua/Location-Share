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

//    private const val rootUrl = "http://192.168.2.10:8080/api/"
    private val rootUrl = "http://locshare.autodotua.top/api/"


    fun setUserInfo(context: Context, user: User, callback: (succeed: Boolean, message: String?, response: Response?) -> Unit)
    {
        post(context, "userInfo", user, callback)
    }


    fun getAll(context: Context, callback: (succeed: Boolean, message: String?, response: Response?) -> Unit)
    {
        post(context, "get", GetOption.current, callback)
    }

//    fun getGroupMembers(context: Context, callback: (succeed: Boolean, message: String?, response: Response?) -> Unit)
//    {
//        post(context, "members", null, callback)
//    }

    fun update(context: Context, location: Location, callback: (succeed: Boolean, message: String?, response: Response?) -> Unit)
    {
        post(context, "update", location, callback)
    }

    fun hide(context: Context, callback: (succeed: Boolean, message: String?, response: Response?) -> Unit)
    {
        post(context, "hide", null, callback)
    }

    fun signIn(context: Context, user: User, callback: (succeed: Boolean, message: String?, response: Response?) -> Unit)
    {
        post(context, "signIn", user, callback)
    }

    fun signUp(context: Context, user: User, callback: (succeed: Boolean, message: String?, response: Response?) -> Unit)
    {
        post(context, "signUp", user, callback)
    }

//    fun checkToken(context: Context, callback: (succeed: Boolean, message: String?, response: Response?) -> Unit)
//    {
//        post(context, "checkToken", null, callback)
//    }

    private fun <T> post(context: Context, subUrl: String, data: T?,
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
                //设置请求方法
                connection.requestMethod = "POST"
                //设置连接超时时间（毫秒）
                connection.connectTimeout = 5000
                //设置读取超时时间（毫秒）
                connection.readTimeout = 5000
                val request = Request<T>()
                request.user = User.current
                request.data = data
                val dataJson = Gson().toJson(request)
                val dataBytes = dataJson.toByteArray();
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("Content-Length", dataBytes.size.toString())
                connection.doOutput = true
                connection.outputStream.write(dataJson.toByteArray())

                val code = connection.responseCode
                if (code == 401)
                {
                    if (!LoginActivity.hasInstance)
                    {
                        val intent = Intent(context, LoginActivity::class.java)
                        intent.putExtra("message", "登录信息失效，请重新登陆")
                        intent.putExtra("deleteUser", true)
                        context.startActivity(intent)
                    }
                    return@Thread
                }
                if (code / 100 == 2)
                {                //返回输入流
                    val inputStream = connection.inputStream
                    var text = inputStream.bufferedReader().use(BufferedReader::readLine);
                    Looper.prepare()
                    val response = Gson().fromJson(text,Response::class.java)
//                    val response = Gson().fromJson<Response<TR>>(text,
//                            object:TypeToken<Response<TR>>(){}.type)
                    try
                    {
                        callback(true, null, response)
                    }
                    catch (ex: Exception)
                    {

                    }
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

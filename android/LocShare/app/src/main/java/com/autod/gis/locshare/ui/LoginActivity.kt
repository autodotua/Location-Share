package com.autod.gis.locshare.ui

import android.graphics.Color
import android.os.Bundle
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.autod.gis.locshare.R
import kotlinx.android.synthetic.main.activity_login.*
import android.text.Editable
import androidx.preference.PreferenceManager
import com.autod.gis.locshare.model.User
import com.autod.gis.locshare.service.ConfigServer
import com.autod.gis.locshare.service.NetworkService
import java.security.MessageDigest
import java.math.BigInteger
import android.view.KeyEvent.KEYCODE_BACK
import android.view.KeyEvent
import com.google.gson.internal.LinkedTreeMap


class LoginActivity : AppCompatActivity()
{

    var config: ConfigServer? = null
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        supportActionBar!!.hide()
        hasInstance = true
        setContentView(R.layout.activity_login)
        config = ConfigServer(this, PreferenceManager.getDefaultSharedPreferences(applicationContext))
        if (intent.hasExtra("message"))
        {
            login_tvw_message.text = intent.getStringExtra("message")
        }

        login_ett_username.requestFocus()
        if (User.current != null)
        {
            login_ett_username.setText(User.current!!.name)
            login_ett_password.requestFocus()
        }
        if (intent.getBooleanExtra("deleteUser", false))
        {
            setUser(null)
        }

        login_ett_username.setOnEditorActionListener { _, actionId, _ ->
            when (actionId)
            {
                EditorInfo.IME_ACTION_DONE ->
                    login_ett_password.requestFocus()
            }
            false
        }
        login_ett_password.setOnEditorActionListener { _, actionId, _ ->
            when (actionId)
            {
                EditorInfo.IME_ACTION_DONE ->
                    signIn(
                            login_ett_username.text.toString(),
                            login_ett_password.text.toString()
                    )
            }
            false
        }
        val textWatcher = object : TextWatcher
        {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int)
            {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int)
            {
                if (login_ett_username.text.isNotEmpty() && login_ett_password.text.isNotEmpty())
                {

                    login_btn_sign_in.isEnabled = true
                    login_btn_sign_up.isEnabled = true
                }
                else
                {
                    login_btn_sign_in.isEnabled = false
                    login_btn_sign_up.isEnabled = false
                }
            }

            override fun afterTextChanged(s: Editable)
            {
            }
        }
        login_ett_username.addTextChangedListener(textWatcher)
        login_ett_password.addTextChangedListener(textWatcher)

        login_btn_sign_in.setOnClickListener {
            login_pgb_loading.visibility = View.VISIBLE
            signIn(login_ett_username.text.toString(), login_ett_password.text.toString())
        }
        login_btn_sign_up.setOnClickListener {
            login_pgb_loading.visibility = View.VISIBLE
            signUp(login_ett_username.text.toString(), login_ett_password.text.toString())
        }

    }

    private fun signIn(username: String, pswd: String)
    {
        val user = User().apply {
            name = username
            password = getMd5(pswd)
        }
        NetworkService.signIn(this, user) { succeed, message, response ->
            runOnUiThread {
                login_pgb_loading.visibility = View.INVISIBLE
                if (!succeed)
                {
                    showError("登陆失败：$message")
                    return@runOnUiThread
                }
                if (!response!!.succeed)
                {
                    showError("登陆失败：${response.message}")
                    return@runOnUiThread
                }
                setUser(User.convertFromMap(response.getLinkedTreeMapData()))
                finish()
                Toast.makeText(this, "登陆成功", Toast.LENGTH_SHORT).show()
            }

        }
    }

    private fun signUp(username: String, pswd: String)
    {
        val user = User().apply {
            name = username
            password = getMd5(pswd)
        }
        NetworkService.signUp(this, user) { succeed, message, response ->
            runOnUiThread {
                login_pgb_loading.visibility = View.INVISIBLE
                if (!succeed)
                {
                    showError("注册失败：$message")
//                Toast.makeText(this, "注册失败：$message", Toast.LENGTH_SHORT).show()
                    return@runOnUiThread
                }
                if (!response!!.succeed)
                {
                    showError("注册失败：${response.message}")
//                Toast.makeText(this, "注册失败：${response.message}", Toast.LENGTH_SHORT).show()
                    return@runOnUiThread
                }

//            Toast.makeText(this, "注册成功：${response.message}", Toast.LENGTH_SHORT).show()
                user.token = response.data as String
                setUser(user)
                finish()
                Toast.makeText(this, "注册并登录成功", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showError(message: String)
    {
        runOnUiThread {

            login_tvw_message.text = message
            login_tvw_message.setTextColor(Color.RED)
        }
    }

    private fun setUser(user: User?)
    {
        User.current = user
        config!!.user = user
    }

    private fun getMd5(text: String): String
    {
        val md5 = MessageDigest.getInstance("md5")
        md5.update(text.toByteArray())
        var hashText = BigInteger(1, md5.digest()).toString(16)
        while (hashText.length < 32)
        {
            hashText = "0$hashText"
        }
        return hashText
    }

    override fun onDestroy()
    {
        super.onDestroy()
        hasInstance = false
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean
    {
        when (keyCode)
        {
            KEYCODE_BACK -> return true
        }
        return super.onKeyDown(keyCode, event)
    }

    companion object
    {
        var hasInstance = false
            get
            private set
    }
}


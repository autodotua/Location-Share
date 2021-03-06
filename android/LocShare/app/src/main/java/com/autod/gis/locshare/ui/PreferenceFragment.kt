package com.autod.gis.locshare.ui

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.preference.*
import com.autod.gis.locshare.R
import com.autod.gis.locshare.model.GetOption
import com.autod.gis.locshare.model.User
import com.autod.gis.locshare.service.ConfigServer
import com.autod.gis.locshare.service.LocationService
import com.esri.arcgisruntime.mapping.Basemap
import kotlinx.android.synthetic.main.dialog_time_span.view.*
import java.util.*
import android.R.id.message
import android.net.Uri
import com.autod.gis.locshare.map.EsriMapViewHelper
import com.autod.gis.locshare.service.NetworkService
import android.app.PendingIntent
import android.content.Intent.getIntent
import android.app.AlarmManager


class PreferenceFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener
{
    private lateinit var config: ConfigServer
    override fun onCreatePreferences(p0: Bundle?, p1: String?)
    {

    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?)
    {
        val a = key
    }

    override fun onAttach(context: Context?)
    {
        super.onAttach(context)
        config = (activity as MainActivity).config
    }


    private fun initLocationPref()
    {
        val locationPref = findPreference(getString(R.string.key_location)) as SwitchPreference
        locationPref.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
            (activity as MainActivity).mapFragment.changeLocationStatus(newValue as Boolean)
            true
        }
    }

    fun setLocationOn(on: Boolean)
    {
        val locationPref = findPreference(getString(R.string.key_location)) as SwitchPreference
        locationPref.isChecked = on
    }

    private fun initEsriMapPref()
    {
        val esriMapPref = findPreference(getString(R.string.key_esri)) as ListPreference
        esriMapPref.entries = Arrays.toString(Basemap.Type.values()).replace("^.|.$".toRegex(), "").split(", ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray();
        esriMapPref.entryValues = esriMapPref.entries
        esriMapPref.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
            if ((activity as MainActivity).mapFragment.mapViewHelper is EsriMapViewHelper)
            {
                ((activity as MainActivity).mapFragment.mapViewHelper as EsriMapViewHelper).loadBaseMap(newValue as String)
            }
            true
        }
    }

    private fun initTileMapPref()
    {
        val tileMapPref = findPreference(getString(R.string.key_tiles)) as EditTextPreference
        tileMapPref.onPreferenceClickListener = Preference.OnPreferenceClickListener { p ->
            tileMapPref.text = config.tileUrls.joinToString("\n")
            true
        }
        tileMapPref.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
            //            esriMapPref.callChangeListener(null)
            val urls = ArrayList<String>()
            var count = 0
            for (url in (newValue as String).split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
            {
                if (url.isNotEmpty())
                {
                    urls.add(url)
                    count++
                }
            }
//            config.tileUrls = urls
            config.esriMap = ""

            (activity as MainActivity).mapFragment.mapViewHelper.loadBaseMap(urls)


            true
        }

    }

    private fun initUserPref()
    {
        val userPref = findPreference(getString(R.string.key_user))
        userPref.onPreferenceClickListener = Preference.OnPreferenceClickListener { p ->
            activity!!.startActivity(Intent(activity!!, LoginActivity::class.java))
            true
        }
    }

    private fun initDisplayNamePref()
    {
        val pref = findPreference(getString(R.string.key_display_name))
        pref.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
            NetworkService.setUserInfo(activity!!, User().apply { displayName = newValue as String? }) { succeed, message, response ->
                if (succeed)
                {
                    User.current!!.displayName = newValue as String?
                    config.user = User.current!!
                    updateSummary()
                }
                else
                {
                    SnackBarHelper.show(activity as MainActivity, "?????????????????????$message", "changeDisplayName")
                }
            }
            true
        }
    }

    private fun initKeepTime()
    {
        val pref = findPreference(getString(R.string.key_keep_time))
        pref.onPreferenceClickListener = Preference.OnPreferenceClickListener { p ->
            val layout = getTimeSpanDialogContentView(GetOption.current.time)
            layout.tsp_chk_continuing.visibility = View.GONE
            val dialog = AlertDialog.Builder(activity!!)
                    .setTitle("??????????????????")
                    .setMessage("?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????")
                    .setView(layout)
                    .setPositiveButton("??????", null)
                    .create()
            dialog.setOnShowListener { d ->
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener click@{ v ->

                    val newValue = layout.tsp_num_hour.value * 3600 + layout.tsp_num_min.value * 60
                    if (newValue == 0)
                    {
                        dialog.setMessage("???????????????0?????????");
                        return@click
                    }

                    GetOption.current.time = newValue;
                    config.getOption = GetOption.current
                    Toast.makeText(activity, "?????????????????????????????????????????????", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            }
            dialog.show()
            true
        }
    }

    private fun initPositioningInterval()
    {
        val pref = findPreference(getString(R.string.key_positioning_Interval))
        pref.onPreferenceClickListener = Preference.OnPreferenceClickListener { p ->
            val layout = getTimeSpanDialogContentView(config.positioningInterval)
            layout.tsp_chk_continuing.apply {
                setOnCheckedChangeListener { _, isChecked ->
                    layout.tsp_num_min.isEnabled = !isChecked
                    layout.tsp_num_hour.isEnabled = !isChecked
                }
                isChecked = config.positioningInterval == 0
            }
            val dialog = AlertDialog.Builder(activity!!)
                    .setTitle("????????????????????????")
                    .setMessage("????????????????????????????????????????????????")
                    .setView(layout)
                    .setPositiveButton("??????", null)
                    .create()
            dialog.setOnShowListener { d ->
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener click@{ v ->

                    var newValue = layout.tsp_num_hour.value * 3600 + layout.tsp_num_min.value * 60
                    if (layout.tsp_chk_continuing.isChecked)
                    {
                        newValue = 0
                    }
                    if (config.isLocationOn)
                    {
                        LocationService.startOrRestart(activity!!)
                    }
                    config.positioningInterval = newValue
                    dialog.dismiss()
                }
            }
            dialog.show()
            true
        }
    }

    private fun getTimeSpanDialogContentView(time: Int): View
    {
        val layout = activity!!.layoutInflater.inflate(R.layout.dialog_time_span, null)
        val minPicker = layout.tsp_num_min.apply {
            minValue = 0
            maxValue = 59
            value = (time / 60) % 60
        }
        val hourPicker = layout.tsp_num_hour.apply {
            minValue = 0
            maxValue = 23
            value = time / 3600
        }
        return layout
    }

    private fun initAboutPref()
    {
        val pref = findPreference(getString(R.string.key_about))
        pref.onPreferenceClickListener = Preference.OnPreferenceClickListener { p ->
            val intent = Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                    "mailto", "autodotua@outlook.com", null))
            intent.putExtra(Intent.EXTRA_SUBJECT, "")
            intent.putExtra(Intent.EXTRA_TEXT, message)
            startActivity(Intent.createChooser(intent, "Choose an Email client :"))
            true
        }
    }

    private fun initExitPref()
    {
        val pref = findPreference(getString(R.string.key_exit))
        pref.onPreferenceClickListener = Preference.OnPreferenceClickListener { p ->
            activity!!.finish()
            true
        }
    }

    private fun initHidePref()
    {
        val pref = findPreference(getString(R.string.key_hide))
        pref.onPreferenceClickListener = Preference.OnPreferenceClickListener { p ->
            if (config.isLocationOn)
            {
                AlertDialog.Builder(activity!!)
                        .setTitle("??????????????????")
                        .setMessage("?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????")
                        .setPositiveButton("??????", null).create().show()

            }
            else
            {
                NetworkService.hide(activity!!) { succeed, message, response ->
                    if (succeed)
                    {
                        SnackBarHelper.show(activity as MainActivity, "????????????", "hide")
                    }
                    else
                    {
                        SnackBarHelper.show(activity as MainActivity, "???????????????$message", "hide")
                    }
                }
            }
            true
        }
    }

    private fun initMapSdkPref()
    {
        val pref = findPreference(getString(R.string.key_map_sdk)) as ListPreference
        pref.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
            if (config.mapSdk != newValue)
            {
                Toast.makeText(activity!!, "?????????????????????????????????\n????????????Android????????????????????????????????????????????????", Toast.LENGTH_SHORT).show()
                val am = activity!!.getSystemService(Context.ALARM_SERVICE) as AlarmManager?
                am!!.set(AlarmManager.RTC_WAKEUP, Calendar.getInstance().timeInMillis + 1000, // one second
                        PendingIntent.getActivity(activity!!, 0, activity!!.intent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_CANCEL_CURRENT))
                activity!!.finish()
//                SnackBarHelper.show(activity as MainActivity, "????????????APP????????????????????????", "sdk")
            }
            true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.preferences)
        initLocationPref()
        initEsriMapPref()
        initTileMapPref()
        initUserPref()
        initKeepTime()
        initPositioningInterval()
        initAboutPref()
        initExitPref()
        initDisplayNamePref()
        initMapSdkPref()
        initHidePref()
    }

    private fun updateSummary()
    {
        if (User.current != null)
        {
            val userPref = findPreference(getString(R.string.key_user))
            userPref.summary = "???????????????????????????${User.current!!.name}"
            val displayNamePref = findPreference(getString(R.string.key_display_name))
            if (User.current!!.displayName.isNullOrEmpty())
            {
                displayNamePref.summary = "??????????????????"
            }
            else
            {
                displayNamePref.summary = "??????????????????${User.current!!.displayName}"
            }
        }
    }

    override fun onResume()
    {
        super.onResume()
        updateSummary()
    }


}

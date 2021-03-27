package com.autod.gis.locshare.ui

import android.view.View
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*

object SnackBarHelper
{
    private val notShowAnymore = HashMap<String, Boolean>()
    fun show(activity: MainActivity, text: String, tag: String)
    {
        show(activity.main_root,text,tag)
    }
    fun show(parent: View, text: String, tag: String)
    {
        if (notShowAnymore.containsKey(tag))
        {
            if (notShowAnymore[tag] == true)
            {
                return
            }
        }
        else
        {
            notShowAnymore[tag] = false
        }
        Snackbar.make(parent, text, Snackbar.LENGTH_SHORT).setAction("不再提醒") { v ->
            notShowAnymore[tag] = true
        }.show()
    }
}
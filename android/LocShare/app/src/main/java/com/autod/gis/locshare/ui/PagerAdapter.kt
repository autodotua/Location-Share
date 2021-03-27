package com.autod.gis.locshare.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.autod.gis.locshare.service.ConfigServer

class PagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm)
{
    override fun getItem(position: Int): Fragment
    {
        return fragments[position]
    }

    private val titles = arrayOf("地图", "成员", "设置")
    private var fragments:Array<Fragment> = arrayOf(MapFragment(),PersonFragment(),PreferenceFragment())


    override fun getCount(): Int
    {
        return 3
    }

    override fun getPageTitle(position: Int): CharSequence?
    {
        return titles[position]
    }
}
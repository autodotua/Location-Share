package com.autod.gis.locshare.ui

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

class ViewPager : ViewPager
{
    constructor(context: Context) : super(context)
    {
        offscreenPageLimit=Int.MAX_VALUE
    }
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    {
        offscreenPageLimit=Int.MAX_VALUE
    }
    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean
    {

        return false
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent?): Boolean
    {
        return false
    }



}
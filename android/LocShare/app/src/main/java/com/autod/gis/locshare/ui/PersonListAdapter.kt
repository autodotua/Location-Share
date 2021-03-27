package com.autod.gis.locshare.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListAdapter
import android.widget.SimpleAdapter
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.autod.gis.locshare.R
import com.autod.gis.locshare.model.Location
import com.autod.gis.locshare.model.User
import kotlinx.android.synthetic.main.list_person_item.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class PersonListAdapter(context: Context) :BaseAdapter()
{
    lateinit var users:List<User>
    constructor(context: Context,  users: List<User>):this(context)
    {
        update(users,false)
    }
  private  val userMap=HashMap<String,User>()

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    fun update(newUsers:List<User>)
    {
        users=newUsers
        userMap.clear()
        for(user in users)
        {
            userMap[user.name]=user
        }
            notifyDataSetChanged()
    }
    fun update(newUsers:List<User>,notify:Boolean)
    {
        users=newUsers
        userMap.clear()
        for(user in users)
        {
            userMap[user.name]=user
        }
        if(notify)
        {
            notifyDataSetChanged()
        }
    }
//    fun updateLocations(locations:List<Location>)
//    {
//      for (location in locations)
//      {
//          if(userMap.containsKey(location.username))
//          {
//              val user=userMap[location.username]
//              val index=users.indexOf(user)
//
//
//          }
//      }
//    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View
    {
        val holder: ViewHolder
        var view = convertView
        if (view == null)
        {
            view = inflater.inflate(R.layout.list_person_item, null)
            holder = ViewHolder().apply {
                tvwName = view.list_person_name
                tvwLastUpdateTime = view.list_person_last_update_time
                tvwLocation = view.list_person_location
                tvwSpeed = view.list_person_speed
            }
            view.tag = holder
        }
        else
        {
            holder = view.tag as ViewHolder
        }
        val user=users[position]
        view!!.list_person_name.text= user.displayName
        holder.tvwName.text =user.displayName
        if(user.lastLocation!=null)
        {
            holder.tvwLastUpdateTime.text = timeToFitString(user.lastLocation!!.time!!)
            holder.tvwLocation.text= String.format("%.6f , %.6f",user.lastLocation!!.longitude,user.lastLocation!!.latitude)
            if(user.lastLocation!!.speed!=null)
            {
                holder.tvwSpeed.text = String.format("%.1f m/s", user.lastLocation!!.speed)
            }
            else
            {
                holder.tvwSpeed.text="未知"
            }
        }
        else
        {
            holder.tvwLastUpdateTime.text ="从未上传过定位"
            holder.tvwLocation.text= "未知"
            holder.tvwSpeed.text="未知"

        }
        return view
    }

    override fun getItem(position: Int): Any
    {
        return users[position]
    }


    override fun getItemId(position: Int): Long
    {
        return 0
    }


    override fun getCount(): Int
    {
        return users.size
    }

    class ViewHolder
    {
        lateinit var tvwName: TextView
        lateinit var tvwLastUpdateTime: TextView
        lateinit var tvwLocation:TextView
        lateinit var tvwSpeed:TextView
    }

    companion object
    {
        val fullFormat = SimpleDateFormat("yyyy-MM-dd  HH:mm:ss", Locale.CHINA)
        val monthDayFormat = SimpleDateFormat("MM-dd  HH:mm:ss", Locale.CHINA)
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.CHINA)
        fun timeToFitString(time: Calendar?): String
        {
            if (time == null)
            {
                return "未知"
            }

//           val calendar= Calendar.getInstance()
//           val now= Calendar.getInstance()
//            val sameDay = calendar.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR) &&
//                    calendar.get(Calendar.YEAR) == now.get(Calendar.YEAR)
//            if(sameDay)
//            {
//                return timeFormat.format(time)
//            }
            return android.text.format.DateUtils.getRelativeTimeSpanString(time.timeInMillis,Calendar.getInstance(TimeZone.getTimeZone("UTC")).timeInMillis,android.text.format.DateUtils.MINUTE_IN_MILLIS).toString()

        }
    }
}
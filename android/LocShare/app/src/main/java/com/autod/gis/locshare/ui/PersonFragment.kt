package com.autod.gis.locshare.ui


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment

import com.autod.gis.locshare.R
import com.autod.gis.locshare.model.Location
import com.autod.gis.locshare.model.User
import com.autod.gis.locshare.service.ConfigServer
import com.autod.gis.locshare.service.NetworkService
import com.google.gson.internal.LinkedTreeMap
import kotlinx.android.synthetic.main.fragment_person.*
import java.lang.Exception
import java.util.*
import java.util.stream.Collectors
import kotlin.collections.ArrayList

class PersonFragment : Fragment()
{
    private lateinit var config: ConfigServer
    private var adapter: PersonListAdapter? = null
    private var timer: Timer? = null
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

    }

    override fun onAttach(context: Context?)
    {
        super.onAttach(context)
        config = (activity as MainActivity).config
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_person, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

    }


    fun updateData(users: List<User>)
    {
        activity!!.runOnUiThread {
            try
            {
                if (adapter == null)
                {
                    adapter = PersonListAdapter(activity!!, ArrayList(users))
                    person_lvw.adapter = adapter
                    person_loading.visibility=View.GONE
                }
                else
                {
                    adapter!!.update(users)
                }
            }
            catch (ex:Exception )
            {
                Toast.makeText(activity!!,ex.message,Toast.LENGTH_SHORT).show()
            }
        }

    }
}

package com.autod.gis.locshare.model

import com.autod.gis.locshare.service.ConfigServer

class GetOption
{
    var time: Int=60*30

    companion object
    {
        var current:GetOption=GetOption()
    }
}
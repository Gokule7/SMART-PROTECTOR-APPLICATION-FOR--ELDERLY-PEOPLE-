package com.raju.spawearable.presentation.utils

import java.io.Serializable


data class Elder(
    var elderName:String,
    var elderPhone:String,
    var password:String,
    var elderProfile:String = "null",
    var elderDescription:String = "null",
    var elderAge:Int = 0,
    var elderDob:String = "null",
    var caretakerFCM:String = ""
):Serializable{
    constructor(): this("","","","","",0,"","")
}

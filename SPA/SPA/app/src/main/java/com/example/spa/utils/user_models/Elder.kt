package com.example.spa.utils.user_models

import java.io.Serializable


data class Elder(
    val elderName:String,
    val elderPhone:String,
    val password:String,
    var elderProfile:String = "null",
    var elderDescription:String = "null",
    var elderAge:Int = 0,
    var elderDob:String = "null"
):Serializable

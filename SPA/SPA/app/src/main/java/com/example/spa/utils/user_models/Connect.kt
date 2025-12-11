package com.example.spa.utils.user_models

import java.io.Serializable

data class Connect(
    val caretakerName:String,
    val caretakerPhone:String,
    val elderPhone:String,
    var elderProfile:String = "Not-defined",
    var elderName:String= "Not-defined"
):Serializable

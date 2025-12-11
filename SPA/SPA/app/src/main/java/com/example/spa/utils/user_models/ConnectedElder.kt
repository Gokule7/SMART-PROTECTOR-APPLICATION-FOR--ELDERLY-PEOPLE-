package com.example.spa.utils.user_models

import java.io.Serializable


data class ConnectedElder(
    val elderProfile:String,
    val elderName:String,
    val elderPhone:String
):Serializable

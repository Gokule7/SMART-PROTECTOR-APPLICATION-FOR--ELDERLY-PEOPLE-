package com.example.spa.utils.user_models

import java.io.Serializable

data class Caretaker(
    val caretakerName:String,
    val caretakerPhone:String,
    val password:String,
    val caretakerProfile:String
):Serializable

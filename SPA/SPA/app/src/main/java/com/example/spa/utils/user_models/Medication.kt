package com.example.spa.utils.user_models

import androidx.annotation.Keep
import java.io.Serializable

@Keep
data class Medication(
    var medicineName : String ="",
    var medicinePurpose : String="",
    var medicineTime : String="",
    var medImage : String=""
):Serializable{
    constructor() : this("", "", "","")
}

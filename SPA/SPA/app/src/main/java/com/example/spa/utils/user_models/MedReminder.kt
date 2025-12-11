package com.example.spa.utils.user_models

import androidx.annotation.Keep
import java.io.Serializable

@Keep
data class MedReminder(
    var elderPhone: String = "",
    var caretakerPhone: String = "",
    var noOfMed: Int = 0,
    var medicines: List<Medication> = emptyList()
) :Serializable{
    constructor() : this("", "", 0, emptyList()) // No-arg constructor required by Firebase
}

package com.example.spa.utils.listeners

import com.example.spa.utils.user_models.ConnectedElder

interface ElderReminderListener {
    fun onElderClicked(model:ConnectedElder,type:Int);
}
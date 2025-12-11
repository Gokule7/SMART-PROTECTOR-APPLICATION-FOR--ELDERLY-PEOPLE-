package com.example.spa.utils.helpers

import android.os.Handler
import android.os.Looper
import java.util.*

class TimeUpdater(private val updateCallback: (String) -> Unit) {

    private val timer = Timer()
    private val handler = Handler(Looper.getMainLooper())

    fun start() {
        timer.schedule(object : TimerTask() {
            override fun run() {
                val currentTime = Calendar.getInstance().time // Fetch the current time
                val formattedTime = android.text.format.DateFormat.format("dd/MM/yyyy hh:mm a", currentTime)
                handler.post {
                    updateCallback(formattedTime.toString()) // Send the time update to the UI
                }
            }
        }, 0, 30000) // Schedule task to run every minute
    }

    fun stop() {
        timer.cancel() // Cancel the timer when not needed
    }
}

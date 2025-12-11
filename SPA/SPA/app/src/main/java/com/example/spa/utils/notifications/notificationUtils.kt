package com.example.spa.utils.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

private const val CHANNEL_ID = "SPA";
private const val CHANNEL_NAME = "SPA"
private const val CHANNEL_DESCRIPTION = "SPA"

fun createNotificationChannel(context: Context){
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME,NotificationManager.IMPORTANCE_HIGH)
        channel.description = CHANNEL_DESCRIPTION
        notificationManager.createNotificationChannel(channel)
    }
}
package com.example.spa.utils.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.IconCompat
import com.example.spa.R
import com.example.spa.reminders.displayers.EmergencyActivity
import com.example.spa.utils.helpers.Constants
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.Random

class FireabaseMessaging : FirebaseMessagingService() {
    private val channel_id = "SPA"
    companion object{
        var mediaPlayer:MediaPlayer? = null
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("Notification","Received")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            //createNot
            createNotificationChannel(context = this@FireabaseMessaging)
        }

        val intent = Intent(this@FireabaseMessaging,EmergencyActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.putExtra(Constants.KEY_ELDER_NAME,message.data[Constants.KEY_NAME])
        intent.putExtra(Constants.KEY_LAST_ADDRESS,message.data[Constants.KEY_LAST_ADDRESS])
        intent.putExtra(Constants.KEY_ELDER_PHONE,message.data[Constants.KEY_ELDER_PHONE])
        intent.putExtra(Constants.KEY_ALERT_MESSAGE,message.data[Constants.KEY_ALERT_MESSAGE])
        val id = 1008
        val pendingIntent = PendingIntent.getActivity(this@FireabaseMessaging,id,intent,PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = NotificationCompat.Builder(applicationContext,channel_id)
            .setSmallIcon(IconCompat.createWithResource(applicationContext, R.drawable.app_logo))
            .setColor(applicationContext.getColor(R.color.black))
            .setContentTitle(message.data[Constants.KEY_NAME])
            .setContentText(message.data[Constants.KEY_ALERT_MESSAGE])
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setBadgeIconType(R.drawable.app_logo)
            .setAutoCancel(true)
            .setOngoing(false)
            .setContentIntent(pendingIntent)
            .setLights(ContextCompat.getColor(applicationContext,R.color.black),5000,5000)



        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            with(NotificationManagerCompat.from(applicationContext)){
                if (ActivityCompat.checkSelfPermission(applicationContext,Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED) {
                    return
                }
                notify(Random().nextInt(3000),builder.build())
            }
        }else{
            NotificationManagerCompat.from(applicationContext).notify(
                Random().nextInt(3000),
                builder.build()
            )
        }

        if (mediaPlayer == null) {
            Log.d("Notification", "Initializing MediaPlayer...")

            val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setDataSource(this@FireabaseMessaging, ringtoneUri)
                prepare()
                isLooping = true
                start()
            }
        } else {
            Log.d("Notification", "Resuming MediaPlayer")
            if (!mediaPlayer!!.isPlaying) {
                mediaPlayer!!.start()
            }
        }

    }
}
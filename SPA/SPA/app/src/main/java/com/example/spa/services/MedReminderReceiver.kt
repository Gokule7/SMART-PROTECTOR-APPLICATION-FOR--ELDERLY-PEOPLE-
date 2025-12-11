package com.example.spa.services

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.CountDownTimer
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.IconCompat
import com.example.spa.R
import com.example.spa.reminders.displayers.ElderCheckReminderActivity
import com.example.spa.reminders.displayers.ElderMedReminderActivity
import com.example.spa.utils.helpers.Constants
import com.example.spa.utils.notifications.createNotificationChannel
import com.example.spa.utils.notifications.sendNotification
import java.util.Random

class MedReminderReceiver : BroadcastReceiver() {
    companion object{
        var mediaPlayer: MediaPlayer? = null
        var countDownTimer: CountDownTimer? = null
        const val TAG = "receiver"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG,"onReceive")
        if (intent!!.getStringExtra(Constants.KEY_WHAT_TO_DO)!=null && intent.getStringExtra(Constants.KEY_WHAT_TO_DO)=="medAlarm"){
            val time = intent.getStringExtra("alarmTime")
            Log.d(TAG,time!!)
            MedReminderSetService.setMedAlarmForNextDay(context!!, time)
            //postNotification
            postNotificationForMedication(context,time)
        }else{
            MedReminderSetService.setCheckNextAlarm(context!!)
            //post Notification
            postNotificationForCheckUp(context)
        }

        //start the count down
        if (mediaPlayer == null){
            val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            Log.d(TAG,"onReceive : notfication if : playing...")
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setDataSource(context, ringtoneUri)
                prepare()
                isLooping = true
                start()
            }
        }else{
            Log.d(TAG,"onReceive : notification else : playing...")
            if (!mediaPlayer!!.isPlaying) {
                mediaPlayer!!.start()
            }
        }
        startCountDown(context)
    }

    private fun postNotificationForMedication(context: Context, time:String) {
        Log.d(TAG,"notification creating!")
        val notIntent = Intent(context,ElderMedReminderActivity::class.java)
        notIntent.putExtra("data",time)
        notIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

        val pendingIntent = PendingIntent.getActivity(context,1000,notIntent,PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        createNotificationChannel(context)

        val builder = NotificationCompat.Builder(context,"SPA")
            .setSmallIcon(IconCompat.createWithResource(context, R.drawable.app_logo))
            .setColor(context.getColor(R.color.black))
            .setContentTitle("Medicine Reminder!")
            .setContentText("Time for medicine!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setBadgeIconType(R.drawable.app_logo)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setOngoing(false)
            .setLights(ContextCompat.getColor(context, R.color.black),5000,5000)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            with(NotificationManagerCompat.from(context)){
                if (ActivityCompat.checkSelfPermission(context,
                        Manifest.permission.POST_NOTIFICATIONS)!= PackageManager.PERMISSION_GRANTED) {
                    return
                }
                notify(Random().nextInt(3000),builder.build())
            }
        }else{
            NotificationManagerCompat.from(context).notify(
                Random().nextInt(3000),
                builder.build()
            )
        }
    }

    private fun postNotificationForCheckUp(context:Context){
        Log.d(TAG,"notification for check up")
        val notIntent = Intent(context,ElderCheckReminderActivity::class.java)
        notIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

        val pendingIntent = PendingIntent.getActivity(context,6000,notIntent,PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        createNotificationChannel(context)

        val builder = NotificationCompat.Builder(context,"SPA")
            .setSmallIcon(IconCompat.createWithResource(context, R.drawable.app_logo))
            .setColor(context.getColor(R.color.black))
            .setContentTitle("Checkup Reminder!")
            .setContentText("Are you alright?")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setBadgeIconType(R.drawable.app_logo)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setOngoing(false)
            .setLights(ContextCompat.getColor(context, R.color.black),5000,5000)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            with(NotificationManagerCompat.from(context)){
                if (ActivityCompat.checkSelfPermission(context,
                        Manifest.permission.POST_NOTIFICATIONS)!= PackageManager.PERMISSION_GRANTED) {
                    return
                }
                notify(Random().nextInt(3000),builder.build())
            }
        }else{
            NotificationManagerCompat.from(context).notify(
                Random().nextInt(3000),
                builder.build()
            )
        }
    }

    private fun startCountDown(context: Context) {
        Log.d(TAG, "Countdown started")

        countDownTimer?.cancel() // Cancel any existing timer before starting a new one

        countDownTimer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                Log.d("ElderHome", (millisUntilFinished / 1000).toString())
            }

            override fun onFinish() {

                mediaPlayer?.let {
                    if (it.isPlaying) {
                        it.stop()
                    }
                    it.release()
                    mediaPlayer = null
                }
                Log.d("ElderHome", "Countdown Finished")
                //sendNotification
                sendNotification(context,"Elder is not responding")
            }
        }.start()
    }
}
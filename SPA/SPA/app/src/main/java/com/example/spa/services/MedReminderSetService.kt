package com.example.spa.services

import android.app.AlarmManager
import android.app.AlarmManager.AlarmClockInfo
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.spa.utils.helpers.Constants
import com.example.spa.utils.helpers.EncoderDecoder
import com.example.spa.utils.helpers.PreferenceManager
import com.example.spa.utils.notifications.createNotificationChannel
import com.example.spa.utils.user_models.Medication

class MedReminderSetService : Service() {
    private val TAG = "MedReminderSet"
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        Log.d(TAG,"onCreate : MedReminder")
        super.onCreate()
    }

    private fun getNotification():Notification{
        Log.d(TAG,"getNotification : creating notification")
        createNotificationChannel(this@MedReminderSetService)
        return NotificationCompat.Builder(this@MedReminderSetService,"SPA")
            .setContentTitle("Reminder Service")
            .setContentText("Running...")
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG,"onStartCommand : MedReminderSet")
        val serviceId = 1000
        startForeground(serviceId,getNotification())
        setMedAlarm(this@MedReminderSetService)
        setCheckupAlarm(this@MedReminderSetService)
        return START_STICKY
    }

    companion object{
        private const val TAG = "MedReminderSet"
        fun setMedAlarm(context:Context){
            Log.d("MEd","setting up")
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val preferenceManager = PreferenceManager(context)
            val listObj = preferenceManager.getString(Constants.KEY_ELDER_MEDICINE)
            lateinit var listOfMed : List<Medication>
            try{
                listOfMed = EncoderDecoder.decodeMedicineList(listObj!!)
            }catch (e:Exception){
                Log.d(TAG,e.localizedMessage!!)
            }

            if (listOfMed.isNotEmpty()){
                var reqCode = 0
                listOfMed.forEach { med->
                    try{
                        val timeComponents = med.medicineTime.split(":")
                        val hour = timeComponents[0].toInt()
                        val mins = timeComponents[1].toInt()

                        val alarmIntent = Intent(context, MedReminderReceiver::class.java)
                        alarmIntent.putExtra("alarmTime",med.medicineTime)
                        alarmIntent.putExtra(Constants.KEY_WHAT_TO_DO,"medAlarm")
                        val pendingIntent = PendingIntent.getBroadcast(context,reqCode,alarmIntent,PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

                        val calendar = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, hour)
                            set(Calendar.MINUTE, mins)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                            if (timeInMillis < System.currentTimeMillis()) {
                                add(Calendar.DAY_OF_YEAR, 1) // ✅ Only add a day if needed
                            }
                        }

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            if (alarmManager.canScheduleExactAlarms()) {
                                try {
                                    val alarmClockInfo = AlarmManager.AlarmClockInfo(calendar.timeInMillis, pendingIntent)
                                    alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
                                    Log.d(TAG, "Exact alarm set successfully")
                                    reqCode++
                                } catch (e: SecurityException) {
                                    Log.e(TAG, "Failed to set exact alarm: ${e.message}")
                                }
                            }
                        } else {
                            val alarmClockInfo = AlarmManager.AlarmClockInfo(calendar.timeInMillis, pendingIntent)
                            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
                            Log.d(TAG, "Exact alarm set successfully")
                            reqCode++
                        }
                    }catch (e:Exception){
                        Log.e(TAG, "Error setting alarm for medication reminder: " + e.message, e);
                        Toast.makeText(context, "Error setting alarm for medication reminder: " + e.message, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }

        fun setMedAlarmForNextDay(context:Context,time:String){
            try {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val timeComponents = time.split(":")
                val hour = timeComponents[0].toInt()
                val mins = timeComponents[1].toInt()

                val alarmIntent = Intent(context, MedReminderReceiver::class.java)
                alarmIntent.putExtra(Constants.KEY_WHAT_TO_DO,"medAlarm")
                alarmIntent.putExtra("alarmTime",time)
                val pendingIntent = PendingIntent.getBroadcast(context,100,alarmIntent,PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

                val calendar = Calendar.getInstance()
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, mins)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.add(Calendar.DAY_OF_YEAR, 1)

                Handler(Looper.getMainLooper()).postDelayed({
                    val alarmClockInfo = AlarmManager.AlarmClockInfo(calendar.timeInMillis,pendingIntent)
                    alarmManager.setAlarmClock(alarmClockInfo,pendingIntent)
                    Log.d(TAG, "Setted for nex med")
                },2*60*1000)
            }catch (e:SecurityException){
                Log.d(TAG,e.localizedMessage!!)
            }
        }

        fun setCheckupAlarm(context: Context){
            Log.d(TAG,"setting check up")
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val preferenceManager = PreferenceManager(context)
            val startTime = preferenceManager.getString(Constants.KEY_CHECK_START_TIME)
            val endTime = preferenceManager.getString(Constants.KEY_CHECK_END_TIME)
            val delayTime = preferenceManager.getString(Constants.KEY_DELAY_TIME)

            if(startTime!=null && endTime!=null && delayTime!=null){
                val reqCode = 24
                val timeComponents = startTime.split(":")
                val startHour = timeComponents[0].toInt()
                val startMins = timeComponents[1].toInt()

                val endTimeComponents = endTime.split(":")
                val endHour = endTimeComponents[0].toInt()
                val endMins = endTimeComponents[1].toInt()

                val alarmIntent = Intent(context, MedReminderReceiver::class.java)
                val pendingIntent = PendingIntent.getBroadcast(context,reqCode,alarmIntent,PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

                val calendar = Calendar.getInstance()
                calendar.set(Calendar.HOUR_OF_DAY, startHour)
                calendar.set(Calendar.MINUTE, startMins)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)

                val calendar1 = Calendar.getInstance()
                calendar1.set(Calendar.HOUR_OF_DAY, endHour)
                calendar1.set(Calendar.MINUTE, endMins)
                calendar1.set(Calendar.SECOND, 0)
                calendar1.set(Calendar.MILLISECOND, 0)

                val currTimeInMillis = System.currentTimeMillis()
                Log.d("time in Millis of Curr", currTimeInMillis.toString())
                Log.d("time in Millis of Start", calendar.getTimeInMillis().toString())
                Log.d("time in Millis of End", calendar1.getTimeInMillis().toString())

                //If start time is greater than end time like for eg: start time is 5pm = (17) and end time is 9am = (9) we will check whether current time is less than start and greater than end time
                if (calendar.get(Calendar.HOUR_OF_DAY) > calendar1.get(Calendar.HOUR_OF_DAY)){
                    Log.d(TAG,"Calendar if inside 1st setCheckup like 17 > 9 right")
                    if (currTimeInMillis<calendar.timeInMillis && currTimeInMillis > calendar1.timeInMillis){
                        Log.d(TAG,"Within limits")
                        val alarmCalendar = Calendar.getInstance()
                        alarmCalendar.timeInMillis = currTimeInMillis

                        alarmCalendar.add(Calendar.MINUTE,1)
                        //just for demo we are adding 1 minute actually we should add like below
//                        alarmCalendar.add(Calendar.HOUR_OF_DAY,delayTime.toInt())
                        Log.d(TAG,"${alarmCalendar.get(Calendar.HOUR_OF_DAY)} : ${alarmCalendar.get(Calendar.MINUTE)}")
                        alarmIntent.putExtra("alarmTime","${alarmCalendar.get(Calendar.HOUR_OF_DAY)}:${alarmCalendar.get(Calendar.MINUTE)}")
                        val alarmClockInfo = AlarmManager.AlarmClockInfo(alarmCalendar.timeInMillis,pendingIntent)
                        alarmManager.setAlarmClock(alarmClockInfo,pendingIntent)
                        Log.d(TAG,"Check setted")
                    }else{
                        Log.d(TAG,"else as its out of limit")
                        calendar.add(Calendar.DAY_OF_YEAR,1)
                        val alarmClockInfo = AlarmManager.AlarmClockInfo(calendar.timeInMillis,pendingIntent)
                        alarmManager.setAlarmClock(alarmClockInfo,pendingIntent)
                    }
                }
                else{
                    if (currTimeInMillis > calendar.timeInMillis && currTimeInMillis<calendar1.timeInMillis){
                        Log.d(TAG, "Calendar Else Inside setCheckup")
                        Log.d(TAG, "Inside If1 Inside setCheckup")

                        val alarmCalendar = Calendar.getInstance()
                        alarmCalendar.timeInMillis = currTimeInMillis

                        //should be set after delay
                        alarmCalendar.add(Calendar.MINUTE,1)
                        Log.d(TAG,"${alarmCalendar.get(Calendar.HOUR_OF_DAY)} : ${alarmCalendar.get(Calendar.MINUTE)}")
                        alarmIntent.putExtra("alarmTime","${alarmCalendar.get(Calendar.HOUR_OF_DAY)}:${alarmCalendar.get(Calendar.MINUTE)}")
                        val alarmClockInfo = AlarmManager.AlarmClockInfo(
                            alarmCalendar.timeInMillis,
                            pendingIntent
                        )

                        alarmManager.setAlarmClock(alarmClockInfo,pendingIntent)
                        Log.d(TAG,"Check setted")
                    }else{
                        Log.d(TAG,"else as its out of limit")
                        calendar.add(Calendar.DAY_OF_YEAR,1)
                        val alarmClockInfo = AlarmManager.AlarmClockInfo(calendar.timeInMillis,pendingIntent)
                        alarmManager.setAlarmClock(alarmClockInfo,pendingIntent)
                    }
                }
            }
        }

        fun setCheckNextAlarm(context:Context){
            Log.d(TAG,"setNextCheckUp")
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val preferenceManager = PreferenceManager(context)
            val startTime = preferenceManager.getString(Constants.KEY_CHECK_START_TIME)
            val endTime = preferenceManager.getString(Constants.KEY_CHECK_END_TIME)
            val delayTime = preferenceManager.getString(Constants.KEY_DELAY_TIME)
            if (startTime!=null && endTime!=null && delayTime!=null){
                val reqCode = 240
                val startTimeComp = startTime.split(":")
                val startHour = startTimeComp[0].toInt()
                val startMins = startTimeComp[1].toInt()

                val endTimeComp = endTime.split(":")
                val endHour = endTimeComp[0].toInt()
                val endMins = endTimeComp[1].toInt()

                val alarmIntent = Intent(context, MedReminderReceiver::class.java)
                val pendingIntent = PendingIntent.getBroadcast(context,reqCode,alarmIntent,PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

                val calendar = Calendar.getInstance()
                calendar[Calendar.HOUR_OF_DAY] = startHour
                calendar[Calendar.MINUTE] = startMins
                calendar[Calendar.SECOND] = 0
                calendar[Calendar.MILLISECOND] = 0

                val calendar1 = Calendar.getInstance()
                calendar1[Calendar.HOUR_OF_DAY] = endHour
                calendar1[Calendar.MINUTE] = endMins
                calendar1[Calendar.SECOND] = 0
                calendar1[Calendar.MILLISECOND] = 0

                val currTimeInMillis = System.currentTimeMillis()
                if (calendar.get(Calendar.HOUR_OF_DAY) > calendar1.get(Calendar.HOUR_OF_DAY)){
                    Log.d(TAG,"Calendar 2 if inside checkup 2")
                    if (currTimeInMillis > calendar.timeInMillis && currTimeInMillis < calendar1.timeInMillis){
                        Log.d(TAG,"Inside if")
                        val alarmCalendar = Calendar.getInstance()
                        alarmCalendar.timeInMillis = currTimeInMillis
                        alarmCalendar.add(Calendar.MINUTE,2)

                        //alarm should be set after delay
                        Log.d(TAG,"${alarmCalendar.get(Calendar.HOUR_OF_DAY)} : ${alarmCalendar.get(Calendar.MINUTE)}")
                        alarmIntent.putExtra("alarmTime","${alarmCalendar.get(Calendar.HOUR_OF_DAY)}:${alarmCalendar.get(Calendar.MINUTE)}")
                        val alarmClockInfo = AlarmManager.AlarmClockInfo(
                            alarmCalendar.timeInMillis,pendingIntent
                        )
                        alarmManager.setAlarmClock(alarmClockInfo,pendingIntent)
                        Log.d(TAG,"Check setted")
                    }else{
                        setCheckupAlarm(context)
                    }
                }else{
                    Log.d(TAG, "Calendar2 Else Inside checkup 2")
                    if (currTimeInMillis > calendar.timeInMillis && currTimeInMillis < calendar1.timeInMillis) {
                        Log.d(TAG, "Inside If")
                        val alarmCalendar = Calendar.getInstance()
                        alarmCalendar.timeInMillis = currTimeInMillis
                        alarmCalendar.add(Calendar.MINUTE, 2)
                        //alarmCalendar.add(Calendar.MINUTE, 50);
                        Log.d(
                            TAG,
                            alarmCalendar[Calendar.HOUR_OF_DAY].toString() + ":" + alarmCalendar[Calendar.MINUTE].toString()
                        )
                        Log.d(TAG,"${alarmCalendar.get(Calendar.HOUR_OF_DAY)} : ${alarmCalendar.get(Calendar.MINUTE)}")
                        alarmIntent.putExtra("alarmTime","${alarmCalendar.get(Calendar.HOUR_OF_DAY)}:${alarmCalendar.get(Calendar.MINUTE)}")
                        //Setting Alarm
                        val alarmClockInfo =
                            AlarmClockInfo(alarmCalendar.timeInMillis, pendingIntent)
                        alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
                        Log.d(TAG, "Check Setted")
                    } else {
                        Log.d(TAG, "Fault")
                        setCheckupAlarm(context)
                    }
                }
            }
        }
    }
}
package com.raju.spawearable.presentation.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.os.StrictMode
import android.util.Log
import androidx.core.app.NotificationCompat
import com.raju.spawearable.R
import com.raju.spawearable.presentation.notifications.sendsNotification
import com.raju.spawearable.presentation.screens.ResponseActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.pow
import kotlin.math.sqrt

class SensorForegroundService : Service(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private lateinit var notificationManager: NotificationManager

    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null
    private var magnetometer: Sensor? = null
    private var heartRate: Sensor? = null
    private var proximitySensor: Sensor? = null
    private val PROXIMITY_THRESHOLD_CM = 5.0f

    // Data holders
    private var accelerometerData: FloatArray = FloatArray(3)
    private var gyroscopeData: FloatArray = FloatArray(3)
    private var magnetometerData: FloatArray = FloatArray(3)
    private var heartRateData: Float = 0f

    // Alert tracking
    private var lastFallDetectionTime: Long = 0
    private var lastHighHeartRateTime: Long = 0
    private var lastWatchRemovalTime: Long = 0

    override fun onCreate() {
        super.onCreate()
        Log.d("SensorService", "Service created")

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Initialize sensors
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        heartRate = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createServiceNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("SensorService", "Service started")

        // Register sensor listeners with appropriate delays
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        gyroscope?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        magnetometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        heartRate?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }

        proximitySensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        Log.d("SensorService", "Service destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            when (event.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    accelerometerData = event.values.copyOf()
                    checkForFall(event.values)
                }

                Sensor.TYPE_HEART_RATE -> {
                    heartRateData = event.values[0]
                    Log.d("HeartRate", "Current HR: $heartRateData")
                    checkForHighHeartRate(heartRateData)
                }

                Sensor.TYPE_GYROSCOPE -> gyroscopeData = event.values.copyOf()
                Sensor.TYPE_MAGNETIC_FIELD -> magnetometerData = event.values.copyOf()
                Sensor.TYPE_PROXIMITY -> {
                    val distance = event.values[0]
                    Log.d("ProximityService", "Proximity: $distance cm")

                    if (distance >= PROXIMITY_THRESHOLD_CM) {
                        Log.d("ProximityService", "Watch likely removed from wrist")
                        checkForWatchRemoval()
                    }
                }
            }
        }
    }

    private fun checkForFall(accelerationValues: FloatArray) {
        val accelerationMagnitude = sqrt(
            accelerationValues[0].pow(2) +
                    accelerationValues[1].pow(2) +
                    accelerationValues[2].pow(2)
        )

        if (accelerationMagnitude > FALL_ACCELERATION_THRESHOLD &&
            System.currentTimeMillis() - lastFallDetectionTime > COOLDOWN_PERIOD
        ) {

            lastFallDetectionTime = System.currentTimeMillis()
            sendNotification(
                "Fall Detected!",
                "Possible fall detected at ${
                    SimpleDateFormat(
                        "HH:mm:ss",
                        Locale.getDefault()
                    ).format(Date())
                }",
                FALL_NOTIFICATION_ID,
                true
            )
        }
    }

    private fun checkForHighHeartRate(heartRate: Float) {
        if (heartRate > HIGH_HEART_RATE_THRESHOLD &&
            heartRate < 250 && // Valid heart rate range
            System.currentTimeMillis() - lastHighHeartRateTime > COOLDOWN_PERIOD
        ) {

            Log.d("tag", "High heart")

            lastHighHeartRateTime = System.currentTimeMillis()
            sendNotification(
                "High Heart Rate Alert",
                "Your heart rate is ${heartRate.toInt()} BPM",
                HEART_RATE_NOTIFICATION_ID,
                true
            )
        }
    }

    private fun checkForWatchRemoval() {
        lastWatchRemovalTime = System.currentTimeMillis()
        sendNotification(
            "Watch Removal Detected",
            "Your watch may have been removed from your wrist",
            WATCH_REMOVAL_NOTIFICATION_ID,
            true
        )
    }

    private fun sendNotification(
        title: String,
        message: String,
        notificationId: Int,
        isAlert: Boolean
    ) {
        val intent = Intent(this@SensorForegroundService, ResponseActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this@SensorForegroundService,
            1111,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification = NotificationCompat.Builder(this, CHANNEL_ID).apply {
            setContentTitle(title)
            setContentText("$message\nif you're alright click this!")
            setSmallIcon(android.R.drawable.ic_dialog_alert)
            setContentIntent(pendingIntent)
            setPriority(if (isAlert) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
            setAutoCancel(true)
            if (isAlert) {
                setVibrate(longArrayOf(0, 500, 200, 500)) // Vibrate pattern for alerts
            }
        }.build()

        notificationManager.notify(notificationId, notification)
        if (isAlert) {
            playWatchAlertSound(message)
        }
    }

    private fun playWatchAlertSound(msg: String) {
        // Stop any existing playback
        stopAlertSound()

        try {
            // Create and configure media player for watch
            mediaPlayer = MediaPlayer.create(this, R.raw.alarm).apply {
                setAudioStreamType(AudioManager.STREAM_ALARM)
                isLooping = true
                start()
            }
            startCountdownTimer(msg)
        } catch (e: Exception) {
            Log.e("MediaPlayer", "Error setting up media player", e)
        }
    }

    private fun startCountdownTimer(msg: String) {
        countDownTimer?.cancel()

        countDownTimer = object : CountDownTimer(10000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                Log.d("AlertTimer", "Remaining: ${millisUntilFinished / 1000}s")
            }

            override fun onFinish() {
                stopAlertSound()
                StrictMode.setThreadPolicy(
                    StrictMode.ThreadPolicy.Builder()
                        .permitAll()  // Allows all network operations
                        .build()
                )
                sendsNotification(
                    this@SensorForegroundService,
                    msg
                )
                Log.d("Notification", "Sent")
            }
        }.start()
    }

    private fun stopAlertSound() {
        countDownTimer?.cancel()
        mediaPlayer?.let { mp ->
            if (mp.isPlaying) {
                mp.stop()
            }
            mp.release()
        }
        mediaPlayer = null
    }


    private fun createServiceNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID).apply {
            setContentTitle("Health Monitoring Active")
            setContentText("Tracking sensors in background")
            setSmallIcon(android.R.drawable.ic_dialog_info)
            setPriority(NotificationCompat.PRIORITY_LOW)
            setOngoing(true)
        }.build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Health Monitoring",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for health monitoring alerts"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Handle accuracy changes if needed
        Log.d("SensorAccuracy", "${sensor?.name} accuracy changed: $accuracy")
    }

    companion object {
        // Threshold constants
        const val FALL_ACCELERATION_THRESHOLD = 15f // m/s² (adjust based on testing)
        const val HIGH_HEART_RATE_THRESHOLD = 120 // BPM (adjust based on user profile)
        const val WATCH_REMOVAL_ACCELERATION_THRESHOLD = 0.5f // m/s²
        const val COOLDOWN_PERIOD = 2000L // 30 seconds between alerts

        // Notification IDs
        const val HEART_RATE_NOTIFICATION_ID = 12346
        const val FALL_NOTIFICATION_ID = 12347
        const val WATCH_REMOVAL_NOTIFICATION_ID = 12348
        const val CHANNEL_ID = "SPA"
        const val NOTIFICATION_ID = 12345

        public var mediaPlayer: MediaPlayer? = null
        public var countDownTimer: CountDownTimer? = null

        fun startService(context: Context) {
            val intent = Intent(context, SensorForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            Log.d("SensorService", "Service start requested")
        }

        fun stopService(context: Context) {
            val intent = Intent(context, SensorForegroundService::class.java)
            context.stopService(intent)
            Log.d("SensorService", "Service stop requested")
        }
    }
}
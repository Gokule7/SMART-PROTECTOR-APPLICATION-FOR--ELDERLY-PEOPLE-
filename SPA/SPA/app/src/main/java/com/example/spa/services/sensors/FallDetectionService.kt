package com.example.spa.services.sensors

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.CountDownTimer
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.IconCompat
import com.example.spa.R
import com.example.spa.services.FallDetectionActivity
import com.example.spa.utils.notifications.createNotificationChannel
import com.example.spa.utils.notifications.sendNotification
import java.util.Random
import java.util.Timer
import java.util.TimerTask
import kotlin.math.sqrt


class FallDetectionService : Service(),SensorEventListener{
    private val handler = Handler(Looper.getMainLooper())
    private val mPeriodicHandler = Handler()
    private val PERIODIC_EVENT_TIMEOUT:Long = 3000
    private var fuseTimer = Timer()

    //Three Sensor Fusion - Variables:
    // angular speeds from gyro
    private var gyro = FloatArray(3)
    private var degreeFloat:Float = 0.0f
    private var degreeFloat2:Float = 0.0f

    // rotation matrix from gyro data
    private var gyroMatrix = FloatArray(9)

    // orientation angles from gyro matrix
    private var gyroOrientation = FloatArray(3)

    // magnetic field vector
    private var magnet = FloatArray(3)

    // accelerometer vector
    private var accel = FloatArray(3)

    // orientation angles from accel and magnet
    private var accMagOrientation = FloatArray(3)

    // final orientation angles from sensor fusion
    private var fusedOrientation = FloatArray(3)

    // accelerometer and magnetometer based rotation matrix
    private var rotationMatrix = FloatArray(9)
    private val EPSILON = 0.000000001f
    private val TIME_CONSTANT: Long = 30
    val FILTER_COEFFICIENT: Float = 0.98f
    private val NS2S: Float = 1.0f / 1000000000.0f
    private var timestamp:Float = 0f
    private var initState = true

    //sensor variables
    private lateinit var sensorManager: SensorManager
    private lateinit var senAccelerometer:Sensor

    //to get recently sent or not
    private var sentRecently:Char = 'N'

    //Alarm and countdown
    companion object{
        var mediaPlayer:MediaPlayer? = null
        var countDownTimer: CountDownTimer? = null
    }

    private val doPeriodicTask = Runnable{
        Log.d("doPeriodicTask", "Delay Ended**********")
        Log.d("doPeriodicTask : Updating flag", "run: ")
        sentRecently = 'N'
    }

    override fun onDestroy() {
        super.onDestroy()
        mPeriodicHandler.removeCallbacks(doPeriodicTask)
        Log.d("OnDestroy", "Stopping Service")
        sensorManager.unregisterListener(this)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d("Accuracy", "Changed")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("OnStart", "Starting work")
        val serviceId = 1000
        startForeground(serviceId,getNotification())
        onTaskRemoved(intent)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        senAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!!
        initListeners()
        fuseTimer.schedule(CalculateFusedOrientationTask(),1000L,TIME_CONSTANT)
        return START_STICKY
    }

    private fun getNotification(): Notification {
        Log.d("Fall detection","getNotification : creating notification")
        createNotificationChannel(this@FallDetectionService)
        return NotificationCompat.Builder(this@FallDetectionService,"SPA")
            .setContentTitle("Reminder Service")
            .setContentText("Running...")
            .build()
    }

    private fun initListeners(){
        sensorManager.registerListener(this@FallDetectionService,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!!,
            SensorManager.SENSOR_DELAY_FASTEST
        )

        sensorManager.registerListener(this@FallDetectionService,
            sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)!!,
            SensorManager.SENSOR_DELAY_FASTEST
        )

        sensorManager.registerListener(this@FallDetectionService,
            sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)!!,
            SensorManager.SENSOR_DELAY_FASTEST
        )
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor == null) {
            Log.d("FallDetection","Event null")
            return
        }

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                // Copy new accelerometer data into accel array
                System.arraycopy(event.values, 0, accel, 0, 3)
                calculateAccMagOrientation()
            }

            Sensor.TYPE_GYROSCOPE -> {
                // Process gyro data
                gyroFunction(event)
            }

            Sensor.TYPE_MAGNETIC_FIELD -> {
                // Copy new magnetometer data into magnet array
                System.arraycopy(event.values, 0, magnet, 0, 3)
            }

            else -> {
                Log.d("SensorEvent", "Unknown sensor type: ${event.sensor.type}")
            }
        }
    }

    private fun calculateAccMagOrientation(){
        if (SensorManager.getRotationMatrix(rotationMatrix,null,accel,magnet)){
            SensorManager.getOrientation(rotationMatrix,accMagOrientation)
        }
    }

    private fun gyroFunction(event: SensorEvent) {
        // Don't start until first accelerometer/magnetometer orientation has been acquired

        // Initialization of the gyroscope-based rotation matrix
        if (initState) {
            val initMatrix = getRotationMatrixFromOrientation(accMagOrientation)
            val test = FloatArray(3)
            SensorManager.getOrientation(initMatrix, test)
            gyroMatrix = matrixMultiplication(gyroMatrix, initMatrix)
            initState = false
        }

        // Copy the new gyro values into the gyro array
        // Convert the raw gyro data into a rotation vector
        val deltaVector = FloatArray(4)
        if (timestamp != 0.toFloat()) {
            val dT = (event.timestamp - timestamp) * NS2S
            System.arraycopy(event.values, 0, gyro, 0, 3)
            getRotationVectorFromGyro(gyro, deltaVector, dT / 2.0f)
        }

        // Measurement done, save current time for next interval
        timestamp = event.timestamp.toFloat()

        // Convert rotation vector into rotation matrix
        val deltaMatrix = FloatArray(9)
        SensorManager.getRotationMatrixFromVector(deltaMatrix, deltaVector)

        // Apply the new rotation interval on the gyroscope-based rotation matrix
        gyroMatrix = matrixMultiplication(gyroMatrix, deltaMatrix)

        // Get the gyroscope-based orientation from the rotation matrix
        SensorManager.getOrientation(gyroMatrix, gyroOrientation)
    }


    private fun getRotationVectorFromGyro(
        gyroValues: FloatArray,
        deltaRotationVector: FloatArray,
        timeFactor: Float
    ) {
        val normValues = FloatArray(3)

        // Calculate the angular speed of the sample
        val omegaMagnitude = sqrt(
            gyroValues[0] * gyroValues[0] +
                    gyroValues[1] * gyroValues[1] +
                    gyroValues[2] * gyroValues[2]
        )

        // Normalize the rotation vector if it's big enough to get the axis
        if (omegaMagnitude > EPSILON) {
            normValues[0] = gyroValues[0] / omegaMagnitude
            normValues[1] = gyroValues[1] / omegaMagnitude
            normValues[2] = gyroValues[2] / omegaMagnitude
        }

        // Integrate around this axis with the angular speed by the timestep
        // Convert this axis-angle representation of the delta rotation
        // into a quaternion before turning it into the rotation matrix.
        val thetaOverTwo = omegaMagnitude * timeFactor
        val sinThetaOverTwo = kotlin.math.sin(thetaOverTwo)
        val cosThetaOverTwo = kotlin.math.cos(thetaOverTwo)

        deltaRotationVector[0] = sinThetaOverTwo * normValues[0]
        deltaRotationVector[1] = sinThetaOverTwo * normValues[1]
        deltaRotationVector[2] = sinThetaOverTwo * normValues[2]
        deltaRotationVector[3] = cosThetaOverTwo
    }

    private fun getRotationMatrixFromOrientation(o: FloatArray): FloatArray {
        val xM = FloatArray(9)
        val yM = FloatArray(9)
        val zM = FloatArray(9)

        val sinX = kotlin.math.sin(o[1])
        val cosX = kotlin.math.cos(o[1])
        val sinY = kotlin.math.sin(o[2])
        val cosY = kotlin.math.cos(o[2])
        val sinZ = kotlin.math.sin(o[0])
        val cosZ = kotlin.math.cos(o[0])

        // Rotation about x-axis (pitch)
        xM[0] = 1.0f
        xM[1] = 0.0f
        xM[2] = 0.0f
        xM[3] = 0.0f
        xM[4] = cosX
        xM[5] = sinX
        xM[6] = 0.0f
        xM[7] = -sinX
        xM[8] = cosX

        // Rotation about y-axis (roll)
        yM[0] = cosY
        yM[1] = 0.0f
        yM[2] = sinY
        yM[3] = 0.0f
        yM[4] = 1.0f
        yM[5] = 0.0f
        yM[6] = -sinY
        yM[7] = 0.0f
        yM[8] = cosY

        // Rotation about z-axis (azimuth)
        zM[0] = cosZ
        zM[1] = sinZ
        zM[2] = 0.0f
        zM[3] = -sinZ
        zM[4] = cosZ
        zM[5] = 0.0f
        zM[6] = 0.0f
        zM[7] = 0.0f
        zM[8] = 1.0f

        // Rotation order is y, x, z (roll, pitch, azimuth)
        var resultMatrix = matrixMultiplication(xM, yM)
        resultMatrix = matrixMultiplication(zM, resultMatrix)
        return resultMatrix
    }


    private fun matrixMultiplication(A: FloatArray, B: FloatArray): FloatArray {
        val result = FloatArray(9)

        result[0] = A[0] * B[0] + A[1] * B[3] + A[2] * B[6]
        result[1] = A[0] * B[1] + A[1] * B[4] + A[2] * B[7]
        result[2] = A[0] * B[2] + A[1] * B[5] + A[2] * B[8]

        result[3] = A[3] * B[0] + A[4] * B[3] + A[5] * B[6]
        result[4] = A[3] * B[1] + A[4] * B[4] + A[5] * B[7]
        result[5] = A[3] * B[2] + A[4] * B[5] + A[5] * B[8]

        result[6] = A[6] * B[0] + A[7] * B[3] + A[8] * B[6]
        result[7] = A[6] * B[1] + A[7] * B[4] + A[8] * B[7]
        result[8] = A[6] * B[2] + A[7] * B[5] + A[8] * B[8]

        return result
    }

    inner class CalculateFusedOrientationTask : TimerTask() {
        override fun run() {
            val oneMinusCoeff = 1.0f - FILTER_COEFFICIENT
            fusedOrientation[0] =
                FILTER_COEFFICIENT * gyroOrientation[0] + oneMinusCoeff * accMagOrientation[0]
            Log.d("X:", fusedOrientation[0].toString())

            fusedOrientation[1] =
                FILTER_COEFFICIENT * gyroOrientation[1] + oneMinusCoeff * accMagOrientation[1]
            Log.d("Y:", fusedOrientation[1].toString())

            fusedOrientation[2] =
                FILTER_COEFFICIENT * gyroOrientation[2] + oneMinusCoeff * accMagOrientation[2]
            Log.d("Z:", fusedOrientation[2].toString())

            // **********Sensing Danger**********
            val smv = sqrt(
                (accel[0] * accel[0] + accel[1] * accel[1] + accel[2] * accel[2]).toDouble()
            )
            Log.d("SMV:", smv.toString())

            if (smv > 25) {
                if (sentRecently == 'N') {
                    Log.d("Accelerometer vector:", smv.toString())
                    degreeFloat = (fusedOrientation[1] * 180 / Math.PI).toFloat()
                    degreeFloat2 = (fusedOrientation[2] * 180 / Math.PI).toFloat()

                    if (degreeFloat < 0) degreeFloat *= -1
                    if (degreeFloat2 < 0) degreeFloat2 *= -1

                    if (degreeFloat > 10 && degreeFloat2 > 10 && (degreeFloat > 40 || degreeFloat2 > 40)) {
                        Log.d("Degree:", "1: $degreeFloat")
                        Log.d("Degree:", "2: $degreeFloat2")
                        handler.post {
                            Toast.makeText(
                                this@FallDetectionService,
                                "Sensed Danger! Sending SMS",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        showNotificationToElder()
                    } else {
                        Log.d("Degree:", "Else 1: $degreeFloat")
                        Log.d("Degree:", "Else 2: $degreeFloat2")
                        handler.post {
                            Toast.makeText(
                                this@FallDetectionService,
                                "Sudden Movement! But looks safe",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    sentRecently = 'Y'
                    Log.d("Delay", "Delay Start**********")
                    mPeriodicHandler.postDelayed(doPeriodicTask, PERIODIC_EVENT_TIMEOUT)
                }
            }
            gyroMatrix = getRotationMatrixFromOrientation(fusedOrientation)
            System.arraycopy(fusedOrientation, 0, gyroOrientation, 0, 3)
        }
    }

    private fun showNotificationToElder(){
        Log.d("Notification", "notification creating")

        val notificationIntent = Intent(
            this,
            FallDetectionActivity::class.java
        )

        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

        val pendingIntent = PendingIntent.getActivity(
            this,
            6000,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )


        val builder = NotificationCompat.Builder(applicationContext,"SPA")
            .setSmallIcon(IconCompat.createWithResource(applicationContext, R.drawable.app_logo))
            .setColor(applicationContext.getColor(R.color.black))
            .setContentTitle("Fall Detected!")
            .setContentText("Are you alright?")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setBadgeIconType(R.drawable.app_logo)
            .setAutoCancel(true)
            .setOngoing(false)
            .setContentIntent(pendingIntent)
            .setLights(ContextCompat.getColor(applicationContext, R.color.black),5000,5000)


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            with(NotificationManagerCompat.from(applicationContext)){
                if (ActivityCompat.checkSelfPermission(applicationContext,
                        Manifest.permission.POST_NOTIFICATIONS)!= PackageManager.PERMISSION_GRANTED) {
                    return
                }
                notify(Random().nextInt(9000),builder.build())
            }
        }else{
            NotificationManagerCompat.from(applicationContext).notify(
                Random().nextInt(9000),
                builder.build()
            )
        }

        handler.post {
            startCountDown()
        }

    }

    private fun startCountDown() {
        Log.d("FallDetection", "Countdown started")
        startMediaPlayer()
        countDownTimer?.cancel() // Cancel any existing timer before starting a new one

        countDownTimer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                Log.d("FallDetection", (millisUntilFinished / 1000).toString())
            }

            override fun onFinish() {

                mediaPlayer?.let {
                    if (it.isPlaying) {
                        it.stop()
                    }
                    it.release()
                    mediaPlayer = null
                }
                Log.d("FallDetection", "Countdown Finished")
                //sendNotification
                sendNotification(this@FallDetectionService,"Fall was detected from elder phone and\nthey're not responding please check them!")
            }
        }.start()
    }

    private fun startMediaPlayer(){
        //start the count down
        if (mediaPlayer == null){
            val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            Log.d("FallDetection","onReceive : notification if : playing...")
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setDataSource(this@FallDetectionService, ringtoneUri)
                prepare()
                isLooping = true
                start()
            }
        }else{
            Log.d("FallDetection","onReceive : notification else : playing...")
            if (!mediaPlayer!!.isPlaying) {
                mediaPlayer!!.start()
            }
        }
    }
}
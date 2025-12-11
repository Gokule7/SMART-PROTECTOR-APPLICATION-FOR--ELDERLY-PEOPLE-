package com.example.spa.caretakers

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.spa.R
import com.example.spa.connected.ConnectToElderActivity
import com.example.spa.connected.ServicesActivity
import com.example.spa.connected.SetElderRemindersActivity
import com.example.spa.connected.ViewElderActivity
import com.example.spa.databinding.ActivityCaretakerHomeBinding
import com.example.spa.home.EntryActivity
import com.example.spa.utils.helpers.Constants
import com.example.spa.utils.helpers.EncoderDecoder
import com.example.spa.utils.helpers.PreferenceManager
import com.example.spa.utils.helpers.TimeUpdater
import com.example.spa.utils.notifications.Notification
import com.example.spa.utils.notifications.NotificationApi
import com.example.spa.utils.notifications.NotificationData
import com.example.spa.utils.notifications.createNotificationChannel
import com.example.spa.utils.notifications.sendNotification
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CaretakerHomeActivity : AppCompatActivity() {
    private lateinit var caretakerHomeBinding: ActivityCaretakerHomeBinding
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var timer: TimeUpdater
    private lateinit var database : FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        init()
        setContentView(caretakerHomeBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //connect to elder
        //account details (logout and all)
        //set reminders (setting reminders,check ups,doctor emails)
        //monitoring dashboard
        listeners()
        checkPermissionForNotification()
    }

    private fun init(){
        database = FirebaseFirestore.getInstance()
        caretakerHomeBinding = ActivityCaretakerHomeBinding.inflate(layoutInflater)
        preferenceManager = PreferenceManager(this@CaretakerHomeActivity)
        setTimer()
        loadAnim()
        caretakerHomeBinding.caretakerProfile.isEnabled = false
        loadProfile()
    }

    private fun loadAnim(){
        Glide.with(this@CaretakerHomeActivity)
            .asGif()
            .load(R.drawable.connect)
            .placeholder(R.drawable.place_holder)
            .into(caretakerHomeBinding.connectImg)

        Glide.with(this@CaretakerHomeActivity)
            .asGif()
            .load(R.drawable.notification)
            .placeholder(R.drawable.place_holder)
            .into(caretakerHomeBinding.reminderImg)

        Glide.with(this@CaretakerHomeActivity)
            .asGif()
            .load(R.drawable.profile)
            .placeholder(R.drawable.place_holder)
            .into(caretakerHomeBinding.caretakerAccImg)

        Glide.with(this@CaretakerHomeActivity)
            .asGif()
            .load(R.drawable.health)
            .placeholder(R.drawable.place_holder)
            .into(caretakerHomeBinding.elderHealthImg)

    }

    private fun loadProfile(){
        caretakerHomeBinding.caretakerProfile.setImageBitmap(
            EncoderDecoder.decodeImage(preferenceManager.getString(
                Constants.KEY_CARETAKER_PROFILE)!!))
        caretakerHomeBinding.caretakerProfile.isEnabled = true
    }

    private fun listeners(){
        caretakerHomeBinding.caretakerLogout.setOnClickListener {
            logout()
        }

        caretakerHomeBinding.viewElder.setOnClickListener {
            val intent = Intent(
                this@CaretakerHomeActivity,
               ViewElderActivity::class.java
            )
            Toast.makeText(this@CaretakerHomeActivity,"..",Toast.LENGTH_SHORT).show()
            startActivity(intent)
        }

        caretakerHomeBinding.caretakerProfile.setOnClickListener {
            startActivity(Intent(this@CaretakerHomeActivity,CaretakerAccountActivity::class.java))
        }

        caretakerHomeBinding.connectToElder.setOnClickListener {
            startActivity(Intent(this@CaretakerHomeActivity,ConnectToElderActivity::class.java))
        }

        caretakerHomeBinding.setReminders.setOnClickListener {
            startActivity(Intent(this@CaretakerHomeActivity,SetElderRemindersActivity::class.java))
        }


        //list elders when they are clicked show their details like meds and all and show max ,avg and min health metrics ...
        caretakerHomeBinding.elderHealth.setOnClickListener {
            startActivity(Intent(this@CaretakerHomeActivity,ServicesActivity::class.java
            ))
        }
    }

    private fun logout(){
        val intent = Intent(this@CaretakerHomeActivity, EntryActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)

        //cleared data
        preferenceManager.clear()
        startActivity(intent)
        finish()
    }

    private fun setTimer(){
        timer = TimeUpdater { time->
            caretakerHomeBinding.currTime.text = time
        }
        timer.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.stop()
    }

    private fun checkPermissionForNotification(){
        val tag = "caretakerHome"
        Log.d(tag,"checking permission for notification!")
        createNotificationChannel(this@CaretakerHomeActivity)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            Log.d(tag,"requesting permissions")
            if (ContextCompat.checkSelfPermission(this@CaretakerHomeActivity,Manifest.permission.POST_NOTIFICATIONS)==PackageManager.PERMISSION_GRANTED){
                getToken()
                Log.d(tag,"permission for notification is already granted")
            }else{
                Log.d(tag,"requesting permission for notifications")
                notReqLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }else{
            getToken()
        }
    }

    private  val notReqLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){isgranted->
        if (isgranted){
            getToken()
        }else{
            showToast("Please allow permission for notification")
        }
    }

    private fun showToast(msg:String){
        Toast.makeText(this@CaretakerHomeActivity,msg,Toast.LENGTH_SHORT).show()
    }

    private fun getToken(){
        val tag = "caretakerFCM"
        FirebaseMessaging.getInstance()
            .token
            .addOnCompleteListener { token->
                Log.d(tag, "Token ${token.result}")
                database.collection(Constants.KEY_CARETAKER_COLLECTION)
                    .document(preferenceManager.getString(Constants.KEY_CARETAKER_ID)!!)
                    .update(Constants.KEY_FCM_TOKEN,token.result)
                    .addOnSuccessListener {
                        preferenceManager.putString(Constants.KEY_FCM_TOKEN,token.result)
                        showToast("Done")
                    }
                    .addOnFailureListener {
                        showToast(it.localizedMessage!!.toString())
                    }
            }
            .addOnFailureListener { err->
                showToast(err.localizedMessage!!.toString())
            }
    }


}
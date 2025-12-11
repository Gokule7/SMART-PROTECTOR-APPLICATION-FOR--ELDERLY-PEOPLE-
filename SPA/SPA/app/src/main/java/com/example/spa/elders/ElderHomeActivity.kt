package com.example.spa.elders

import android.provider.Settings
import android.Manifest
import android.app.AlarmManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.spa.R
import com.example.spa.databinding.ActivityElderHomeBinding
import com.example.spa.home.EntryActivity
import com.example.spa.services.MedReminderSetService
import com.example.spa.services.sensors.FallDetectionService
import com.example.spa.utils.helpers.Constants
import com.example.spa.utils.helpers.EncoderDecoder
import com.example.spa.utils.helpers.PreferenceManager
import com.example.spa.utils.helpers.TimeUpdater
import com.example.spa.utils.notifications.sendNotification
import com.example.spa.utils.user_models.MedReminder
import com.example.spa.utils.user_models.Medication
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class ElderHomeActivity : AppCompatActivity() {
    private lateinit var elderHomeBinding: ActivityElderHomeBinding
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var timeUpdater: TimeUpdater
    private lateinit var database:FirebaseFirestore
    val tag = "elderHome"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
        enableEdgeToEdge()
        setContentView(elderHomeBinding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        listeners()
        checkPermissions()
    }

    private fun init(){
        database = FirebaseFirestore.getInstance()
        elderHomeBinding = ActivityElderHomeBinding.inflate(layoutInflater)
        preferenceManager = PreferenceManager(this@ElderHomeActivity)
        timeUpdater = TimeUpdater{time->
            elderHomeBinding.todayDate.text = time
        }
        timeUpdater.start()
        loadProfile()
    }

    private fun loadProfile(){
        elderHomeBinding.eldersProfile.setImageBitmap(
            EncoderDecoder.decodeImage(preferenceManager.getString(
                Constants.KEY_ELDER_PROFILE)!!))
        elderHomeBinding.elderNameGreet.text = "Hello! ${preferenceManager.getString(Constants.KEY_ELDER_NAME)}"
    }

    private fun logout(){
        loading()
        val value:MutableMap<String,Any> = HashMap()
        value[Constants.KEY_FCM_TOKEN] = FieldValue.delete()
        database.collection(Constants.KEY_ELDER_COLLECTION)
            .document(preferenceManager.getString(Constants.KEY_ELDER_ID)!!)
            .update(value)
            .addOnSuccessListener {
                unloading()
                val intent = Intent(this@ElderHomeActivity,EntryActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)

                //cleared data
                preferenceManager.clear()
                startActivity(intent)
                finish()
            }
            .addOnFailureListener {
                unloading()
                showToast(it.localizedMessage!!)
            }

    }

    private fun listeners(){
        elderHomeBinding.elderLogout.setOnClickListener {
            logout()
        }

        elderHomeBinding.eldersProfile.setOnClickListener {
            val intent = Intent(this@ElderHomeActivity,ElderAccountActivity::class.java)
            startActivity(intent)
        }

        elderHomeBinding.emergency.setOnClickListener{

        }

    }

    private fun loading(){
        elderHomeBinding.elderHomeLoad.visibility = View.VISIBLE
    }

    private fun unloading(){
        elderHomeBinding.elderHomeLoad.visibility = View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        timeUpdater.stop() // Stop the updater to avoid memory leaks
    }

    private fun loadConnectedCaretaker(){
        loading()
        database.collection(Constants.KEY_CONNECT_COLLECTION)
            .whereEqualTo(Constants.KEY_ELDER_PHONE,preferenceManager.getString(Constants.KEY_ELDER_PHONE))
            .get()
            .addOnCompleteListener { task->
                if (task.result!=null && task.isSuccessful && task.result.documents.isNotEmpty()){
                    val snapshot = task.result.documents[0]
                    preferenceManager.putString(Constants.KEY_CONNECTED_CARETAKER,snapshot.getString(Constants.KEY_CARETAKER_PHONE)!!)
                    database.collection(Constants.KEY_CARETAKER_COLLECTION)
                        .whereEqualTo(Constants.KEY_CARETAKER_PHONE,preferenceManager.getString(Constants.KEY_CONNECTED_CARETAKER))
                        .get()
                        .addOnCompleteListener {
                            if(it.result!=null && it.isSuccessful && it.result.documents.isNotEmpty()){
                                val snapshot1 = it.result.documents[0]
                                preferenceManager.putString(Constants.KEY_CARETAKER_ID,snapshot1.id)
                                preferenceManager.putString(Constants.KEY_CARETAKER_FCM,snapshot1.getString(Constants.KEY_FCM_TOKEN)!!)
                                //show toast and get checkup
                                showToast("Details has been loaded!")
                                unloading()
                                getToken()
                                getCheckUp()
                                getWatchDetails()
                                getMedicalReminders()
                            }
                        }.addOnFailureListener { err->
                            showToast(err.localizedMessage!!)
                            unloading()
                        }
                }else{
                    showToast("No connected Caretakers")
                    getToken()
                }
            }.addOnFailureListener {
                showToast(it.localizedMessage!!)
                unloading()
            }
    }

    private fun getWatchDetails(){
        database.collection(Constants.KEY_ELDER_COLLECTION)
            .whereEqualTo(Constants.KEY_ELDER_PHONE,preferenceManager.getString(Constants.KEY_ELDER_PHONE))
            .get()
            .addOnCompleteListener {
                if (it.result!=null && it.isSuccessful && it.result.documents.isNotEmpty()){
                    val doc = it.result.documents[0]
                    if(doc.getString(Constants.KEY_ELDER_SMART_WATCH)!=null){
                        preferenceManager.putString(Constants.KEY_ELDER_SMART_WATCH,doc.getString(Constants.KEY_ELDER_SMART_WATCH)!!)
                    }else{
                        preferenceManager.putString(Constants.KEY_ELDER_SMART_WATCH,"no")
                    }
                }
            }.addOnFailureListener {
                Toast.makeText(this@ElderHomeActivity,it.localizedMessage,Toast.LENGTH_SHORT).show()
            }
    }

    private fun getCheckUp(){
        loading()
        database.collection(Constants.KEY_CHECK_COLLECTION)
            .whereEqualTo(Constants.KEY_ELDER_PHONE,preferenceManager.getString(Constants.KEY_ELDER_PHONE))
            .get()
            .addOnCompleteListener {
                if (it.result!=null && it.isSuccessful && it.result.documents.isNotEmpty()){
                    val snap = it.result.documents[0]
                    preferenceManager.putString(Constants.KEY_CHECK_START_TIME,snap.getString(Constants.KEY_CHECK_START_TIME)!!)
                    preferenceManager.putString(Constants.KEY_CHECK_END_TIME,snap.getString(Constants.KEY_CHECK_END_TIME)!!)
                    preferenceManager.putString(Constants.KEY_DELAY_TIME,snap.getString(Constants.KEY_DELAY_TIME)!!)
                    unloading()
                }else{
                    unloading()
                    showToast("Empty check up")
                }
            }
            .addOnFailureListener {
                unloading()
                showToast(it.localizedMessage!!)
            }
    }

    private fun getMedicalReminders(){
        loading()
        database.collection(Constants.KEY_MEDICINE_COLLECTION)
            .whereEqualTo(Constants.KEY_ELDER_PHONE,preferenceManager.getString(Constants.KEY_ELDER_PHONE))
            .get()
            .addOnCompleteListener {
                if (it.result!=null && it.isSuccessful && it.result.documents.isNotEmpty()){
                    val snapshot = it.result.documents[0]
                    lateinit var listOfmeds:List<Medication>
                    val model: MedReminder? = snapshot.toObject(MedReminder::class.java)
                    if (model!=null)
                        listOfmeds = model.medicines

                    showToast("Serializing...")

                    try {
                        val res = EncoderDecoder.encodeMedicineList(listOfmeds)
                        showToast(res)
                        preferenceManager.putString(Constants.KEY_ELDER_MEDICINE,res)
                        startServices()
                    }catch (e:Exception){
                        showToast("Error :${e.localizedMessage!!}")
                        Log.d(tag,e.stackTraceToString())
                    }
                    startServices()
                    unloading()
                }else{
                    showToast("Null med reminders")
                }
                //start services
            }.addOnFailureListener {
                showToast(it.localizedMessage!!)
                unloading()
            }
    }

    private fun showToast(msg:String){
        Toast.makeText(this@ElderHomeActivity,msg,Toast.LENGTH_SHORT).show()
    }

    private fun getToken(){
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener {
                database.collection(Constants.KEY_ELDER_COLLECTION)
                    .document(preferenceManager.getString(Constants.KEY_ELDER_ID)!!)
                    .update(Constants.KEY_FCM_TOKEN,it.result)
                    .addOnCompleteListener { it2->
                        Log.d("Done","Done")
                        preferenceManager.putString(Constants.KEY_FCM_TOKEN,it.result)
                        showToast("Done")
                    }
                    .addOnFailureListener {err->
                        showToast(err.localizedMessage!!)
                    }
            }
            .addOnFailureListener {
                showToast(it.localizedMessage!!)
            }
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this@ElderHomeActivity, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                requestForegroundLocationPermission()
            } else {
                notReqLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            requestForegroundLocationPermission()
        }
    }

    private val notReqLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            requestForegroundLocationPermission()
        } else {
            showToast("You have to allow notification permissions!")
        }
    }

    private fun checkAlarmPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(AlarmManager::class.java)
            if (alarmManager?.canScheduleExactAlarms() == true) {
                loadConnectedCaretaker()
            } else {
                // Show user instructions to enable the permission
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent) // Directly open settings page
            }
        } else {
            loadConnectedCaretaker()
        }
    }


    private fun checkSensorPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this@ElderHomeActivity, Manifest.permission.HIGH_SAMPLING_RATE_SENSORS) == PackageManager.PERMISSION_GRANTED) {
                checkAlarmPermissions()
            } else {
                sensorLauncher.launch(Manifest.permission.HIGH_SAMPLING_RATE_SENSORS)
            }
        } else {
            checkAlarmPermissions()
        }
    }

    private val sensorLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            checkAlarmPermissions()
        } else {
            showToast("Sensors not allowed!")
        }
    }


    private val foregroundLocationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                // Precise location access granted.
                requestBackgroundLocationPermission()
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                // Approximate location access granted.
                requestBackgroundLocationPermission()
            } else -> {
            // No location access granted.
            Snackbar.make(findViewById(android.R.id.content), "Location permissions denied", Snackbar.LENGTH_LONG).show()
        }
        }
    }

    private val backgroundLocationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Background location granted.
            Snackbar.make(findViewById(android.R.id.content), "Background location granted", Snackbar.LENGTH_LONG).show()
            checkSensorPermissions()
            //Proceed with background location tasks.
        } else {
            Snackbar.make(findViewById(android.R.id.content), "Background location denied", Snackbar.LENGTH_LONG).show()
        }
    }

    private fun requestForegroundLocationPermission() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (permissions.all {
                ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
            }) {
            // Permissions are already granted.
            requestBackgroundLocationPermission()
        } else {
            foregroundLocationPermissionLauncher.launch(permissions)
        }
    }

    private fun requestBackgroundLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Background location already granted.
            Snackbar.make(findViewById(android.R.id.content), "Background location already granted", Snackbar.LENGTH_LONG).show()
            checkSensorPermissions()
            //Proceed with background location tasks.
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                backgroundLocationPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
        }
    }

    private fun startServices(){
        //start fall and alarm services
        Toast.makeText(this@ElderHomeActivity,"Starting..",Toast.LENGTH_SHORT).show()
        if(preferenceManager.getString(Constants.KEY_CARETAKER_FCM)!=null){
            Log.d("Notification","fcm not null")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                Log.d("Notification","Greater than or eq to O")
                startForegroundService(Intent(this@ElderHomeActivity, MedReminderSetService::class.java))
                startForegroundService(Intent(this@ElderHomeActivity,FallDetectionService::class.java))

            }else{
                Log.d("Notification","less than O")
                startService(Intent(this@ElderHomeActivity, MedReminderSetService::class.java))
                startService(Intent(this@ElderHomeActivity,FallDetectionService::class.java))
            }
        }
    }
}
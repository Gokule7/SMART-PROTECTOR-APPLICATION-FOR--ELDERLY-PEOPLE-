package com.example.spa.reminders.schedulers

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.spa.R
import com.example.spa.databinding.ActivityAddCheckUpBinding
import com.example.spa.utils.helpers.Constants
import com.example.spa.utils.helpers.EncoderDecoder
import com.example.spa.utils.helpers.PreferenceManager
import com.example.spa.utils.user_models.CheckUp
import com.example.spa.utils.user_models.ConnectedElder
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.firebase.firestore.FirebaseFirestore

class AddCheckUpActivity : AppCompatActivity() {
    private lateinit var binding : ActivityAddCheckUpBinding
    private lateinit var db:FirebaseFirestore
    private lateinit var picker:MaterialTimePicker
    private lateinit var model:ConnectedElder
    private lateinit var startTime:String
    private lateinit var endTime:String
    private lateinit var delayTime:String
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        init()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        listeners()
    }

    private fun init(){
        preferenceManager = PreferenceManager(this@AddCheckUpActivity)
        binding = ActivityAddCheckUpBinding.inflate(layoutInflater)
        db = FirebaseFirestore.getInstance()
        model = intent.getSerializableExtra("data") as ConnectedElder
        delayTime = "1"
    }

    private fun listeners(){
        binding.addCheckBackBtn.setOnClickListener {
            finish()
        }

        //Delay button
        binding.addDelay.setOnClickListener {
            val delays = arrayOf("1","2","3","4")
            AlertDialog.Builder(this)
                .setTitle("Select Time Delay")
                .setItems(delays) { _, which ->
                    binding.addDelay.text = "${delays[which]} Hrs"
                    delayTime = delays[which]
                }
                .create()
                .show()
        }

        //Start Time Button
        binding.addCheckStartTime.setOnClickListener {
            picker = MaterialTimePicker.Builder()
                .setHour(12)
                .setMinute(0)
                .setTitleText("Check Up Start Time")
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .build()

            picker.show(supportFragmentManager, "SPA")
            picker.addOnPositiveButtonClickListener {
                val hour = if (picker.hour == 0) 12 else if (picker.hour > 12) picker.hour - 12 else picker.hour
                val minute = String.format("%02d", picker.minute)
                val amPm = if (picker.hour >= 12) "PM" else "AM"

                binding.addCheckStartTime.text = "$hour:$minute $amPm"
                startTime = EncoderDecoder.timePickerToString(picker)
            }
        }


        //End Time Button
        binding.addCheckEnd.setOnClickListener {
            picker = MaterialTimePicker.Builder()
                .setHour(12)
                .setMinute(0)
                .setTitleText("Check Up Start Time")
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .build()

            picker.show(supportFragmentManager, "SPA")
            picker.addOnPositiveButtonClickListener {
                val hour = if (picker.hour == 0) 12 else if (picker.hour > 12) picker.hour - 12 else picker.hour
                val minute = String.format("%02d", picker.minute) // Ensure two-digit minute format
                val amPm = if (picker.hour >= 12) "PM" else "AM"

                binding.addCheckEnd.text = "$hour:$minute $amPm"

                // Store the formatted time
                endTime = EncoderDecoder.timePickerToString(picker)
            }
        }


        //Set button
        binding.addCheckup.setOnClickListener {
            loading()
            if(startTime.isNotEmpty() && endTime.isNotEmpty() && delayTime.isNotEmpty()){
                val data = CheckUp(
                    startTime = startTime,
                    endTime = endTime,
                    elderPhone = model.elderPhone,
                    delayTime = delayTime,
                    caretakerPhone = preferenceManager.getString(Constants.KEY_CARETAKER_PHONE)!!
                )
                db.collection(Constants.KEY_CHECK_COLLECTION)
                    .add(data)
                    .addOnSuccessListener {
                        unloading()
                    }
                    .addOnFailureListener {
                        unloading()
                    }
                finish()
            }
        }
    }

    private fun loading(){
        binding.addCheckProgress.visibility = View.VISIBLE
        binding.addCheckup.visibility = View.GONE
    }

    private fun unloading(){
        binding.addCheckProgress.visibility = View.GONE
        binding.addCheckup.visibility = View.VISIBLE
    }
}
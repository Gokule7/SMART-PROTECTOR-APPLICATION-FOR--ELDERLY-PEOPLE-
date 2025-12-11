package com.example.spa.reminders.displayers

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.spa.R
import com.example.spa.databinding.ActivityEmergencyBinding
import com.example.spa.utils.helpers.Constants
import com.example.spa.utils.helpers.EncoderDecoder
import com.example.spa.utils.notifications.FireabaseMessaging
import com.google.firebase.firestore.FirebaseFirestore

class EmergencyActivity : AppCompatActivity() {
    private lateinit var binding:ActivityEmergencyBinding
    private lateinit var db:FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        db = FirebaseFirestore.getInstance()
        binding = ActivityEmergencyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        stopAlert()
        loadProfile()
        init()
    }

    private fun loadProfile(){
        db.collection(Constants.KEY_ELDER_COLLECTION)
            .whereEqualTo(Constants.KEY_ELDER_PHONE,intent.getStringExtra(Constants.KEY_ELDER_PHONE))
            .get()
            .addOnCompleteListener {
                if (it.isSuccessful && it.result!=null && it.result.documents.isNotEmpty()){
                    val doc = it.result.documents[0]
                    binding.elderProfileEmg.setImageBitmap(EncoderDecoder.decodeImage(doc.getString(Constants.KEY_ELDER_PROFILE)!!))
                }
            }
            .addOnFailureListener {
                Toast.makeText(this@EmergencyActivity,it.localizedMessage,Toast.LENGTH_SHORT).show()
            }
    }

    private fun stopAlert(){
        if (FireabaseMessaging.mediaPlayer!=null){
            FireabaseMessaging.mediaPlayer!!.stop()
            FireabaseMessaging.mediaPlayer!!.release()
            FireabaseMessaging.mediaPlayer = null
        }
    }

    private fun init(){
        binding.elderNameEmg.text = intent.getStringExtra(Constants.KEY_ELDER_NAME)
        binding.elderPhoneEmg.text = intent.getStringExtra(Constants.KEY_ELDER_PHONE)
        binding.lastAddress.text = intent.getStringExtra(Constants.KEY_LAST_ADDRESS)
        binding.emgMsg.text = intent.getStringExtra(Constants.KEY_ALERT_MESSAGE)
    }
}
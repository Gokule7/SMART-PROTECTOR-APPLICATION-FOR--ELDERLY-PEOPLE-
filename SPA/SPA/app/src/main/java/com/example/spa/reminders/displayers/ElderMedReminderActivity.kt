package com.example.spa.reminders.displayers

import android.os.Bundle
import android.provider.CalendarContract.Reminders
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.spa.R
import com.example.spa.databinding.ActivityElderMedReminderBinding
import com.example.spa.services.MedReminderReceiver
import com.example.spa.utils.helpers.Constants
import com.example.spa.utils.helpers.EncoderDecoder
import com.example.spa.utils.helpers.PreferenceManager
import com.example.spa.utils.user_models.Medication

class ElderMedReminderActivity : AppCompatActivity() {
    private lateinit var binding: ActivityElderMedReminderBinding
    private lateinit var preferenceManager: PreferenceManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityElderMedReminderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferenceManager = PreferenceManager(this@ElderMedReminderActivity)
        stopAlert()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        init()
    }

    private fun stopAlert() {
        if (MedReminderReceiver.mediaPlayer != null) {
            MedReminderReceiver.mediaPlayer!!.stop()
            MedReminderReceiver.mediaPlayer!!.release()
            MedReminderReceiver.mediaPlayer = null
        }

        if (MedReminderReceiver.countDownTimer != null) {
            MedReminderReceiver.countDownTimer!!.cancel()
            MedReminderReceiver.countDownTimer = null
        }
    }

    private fun init(){
        if (intent.getStringExtra("data")!=null){
            Log.d("ElderCheck","data")
            val medTime = intent.getStringExtra("data")
            val listOb = preferenceManager.getString(Constants.KEY_ELDER_MEDICINE)

            val list:List<Medication>
            try{
                list = EncoderDecoder.decodeMedicineList(listOb!!)
            }catch (e:Exception){
                Log.e("Error",e.localizedMessage!!)
                return
            }

            if (list.isNotEmpty()){
                list.forEach {
                    if (it.medicineTime == medTime){
                        setDetails(it)
                        return
                    }
                }
            }
        }
    }

    private fun setDetails(med:Medication){
        binding.medImage.setImageBitmap(EncoderDecoder.decodeImage(med.medImage))
        binding.medName.text = med.medicineName
        binding.medPur.text = med.medicinePurpose
    }
}
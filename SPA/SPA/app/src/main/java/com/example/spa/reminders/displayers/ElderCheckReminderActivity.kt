package com.example.spa.reminders.displayers

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.spa.R
import com.example.spa.databinding.ActivityElderCheckReminderBinding
import com.example.spa.services.MedReminderReceiver

class ElderCheckReminderActivity : AppCompatActivity() {
    private lateinit var binding: ActivityElderCheckReminderBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityElderCheckReminderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        listeners()
    }

    private fun listeners() {
        binding.safe.setOnClickListener {
            stopAlert()
            binding.safe.visibility = View.GONE;
            binding.checkMessage.visibility = View.GONE;
            binding.greetMsg.visibility = View.VISIBLE;
            Toast.makeText(this@ElderCheckReminderActivity, "Ok", Toast.LENGTH_SHORT).show()
            finish()
        }
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
}
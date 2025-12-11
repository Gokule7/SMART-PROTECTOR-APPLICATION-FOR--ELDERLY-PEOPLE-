package com.example.spa.services

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.spa.R
import com.example.spa.databinding.ActivityFallDetectionBinding

class FallDetectionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFallDetectionBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityFallDetectionBinding.inflate(layoutInflater)
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
            binding.safe.visibility = View.GONE
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
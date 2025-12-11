package com.example.spa.home

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.spa.R
import com.example.spa.caretakers.CaretakerSignInActivity
import com.example.spa.databinding.ActivityEntryBinding
import com.example.spa.elders.ElderSignInActivity

class EntryActivity : AppCompatActivity() {
    private lateinit var entryBinding: ActivityEntryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        entryBinding = ActivityEntryBinding.inflate(layoutInflater)
        setContentView(entryBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        listeners();
    }

    private fun listeners(){
        entryBinding.elderLogin.setOnClickListener {
            startActivity(Intent(this@EntryActivity,ElderSignInActivity::class.java))
        }

        entryBinding.caretakerLogin.setOnClickListener {
            startActivity(Intent(this@EntryActivity,CaretakerSignInActivity::class.java))
        }

    }
}
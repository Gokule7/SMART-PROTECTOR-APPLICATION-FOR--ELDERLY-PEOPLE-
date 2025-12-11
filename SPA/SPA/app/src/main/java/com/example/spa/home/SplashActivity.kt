package com.example.spa.home

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.spa.R
import com.example.spa.caretakers.CaretakerHomeActivity
import com.example.spa.elders.ElderHomeActivity
import com.example.spa.utils.helpers.Constants
import com.example.spa.utils.helpers.PreferenceManager
import kotlinx.coroutines.*

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private lateinit var main:LinearLayout
    private lateinit var fadeOut:Animation
    private lateinit var preferenceManager: PreferenceManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        main = findViewById(R.id.main)
        preferenceManager = PreferenceManager(this@SplashActivity)
        fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out)
        lifecycleScope.launch {
            delay(1000)
            startAnim()
        }
    }

    private fun startAnim(){
        main.startAnimation(fadeOut)
        fadeOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {

            }

            override fun onAnimationEnd(animation: Animation?) {
                if(preferenceManager.getBoolean(Constants.KEY_IS_ELDER_SIGNED_IN)){
                    val intent = Intent(this@SplashActivity,ElderHomeActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }else if(preferenceManager.getBoolean(Constants.KEY_IS_CARETAKER_SIGNED_IN)){
                    val intent = Intent(this@SplashActivity,CaretakerHomeActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }else{
                    val intent = Intent(this@SplashActivity, EntryActivity::class.java)
                    val options = ActivityOptions.makeCustomAnimation(
                        this@SplashActivity,
                        android.R.anim.fade_in,
                        android.R.anim.fade_out
                    )
                    startActivity(intent, options.toBundle())
                    finish()
                }
            }

            override fun onAnimationRepeat(animation: Animation?) {

            }

        })
    }
}
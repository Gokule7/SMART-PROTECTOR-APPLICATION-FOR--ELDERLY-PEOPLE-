package com.example.spa.connected

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.spa.R
import com.example.spa.databinding.ActivityConnectToElderBinding
import com.example.spa.home.OtpActivity
import com.example.spa.utils.helpers.Constants
import com.example.spa.utils.helpers.PreferenceManager
import com.example.spa.utils.user_models.Connect

class ConnectToElderActivity : AppCompatActivity() {
    private lateinit var connectToElderBinding: ActivityConnectToElderBinding
    private lateinit var preferenceManager: PreferenceManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        connectToElderBinding = ActivityConnectToElderBinding.inflate(layoutInflater)
        init()
        setContentView(connectToElderBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        listeners()
    }

    private fun init(){
        preferenceManager = PreferenceManager(this@ConnectToElderActivity)
        connectToElderBinding.connectCcp.registerCarrierNumberEditText(connectToElderBinding.connectElderPhn.editText)
    }

    private fun listeners(){

        connectToElderBinding.connectElder.setOnClickListener {
            if(connectToElderBinding.connectCcp.isValidFullNumber){
                connectToElderBinding.connectElderPhn.error = null
                val phone = connectToElderBinding.connectCcp.fullNumberWithPlus.toString()
                val connect = Connect(
                    caretakerName = preferenceManager.getString(Constants.KEY_CARETAKER_NAME)!!,
                    caretakerPhone = preferenceManager.getString(Constants.KEY_CARETAKER_PHONE)!!,
                    elderPhone = phone
                )

                val intent = Intent(this@ConnectToElderActivity,OtpActivity::class.java)
                intent.putExtra(Constants.KEY_USER, Constants.KEY_CONNECT_COLLECTION)
                intent.putExtra(Constants.KEY_CONNECT_DATA,connect)
                startActivity(intent)
                finish()
            }
            else{
                connectToElderBinding.connectElderPhn.error = "Invalid Phone"
            }
        }
    }
}
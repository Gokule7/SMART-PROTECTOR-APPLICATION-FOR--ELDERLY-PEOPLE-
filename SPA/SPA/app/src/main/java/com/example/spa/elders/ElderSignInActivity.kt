package com.example.spa.elders

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.spa.R
import com.example.spa.databinding.ActivityElderSignInBinding
import com.example.spa.utils.helpers.Constants
import com.example.spa.utils.helpers.PreferenceManager
import com.example.spa.utils.helpers.Validation
import com.google.firebase.firestore.FirebaseFirestore

class ElderSignInActivity : AppCompatActivity() {
    private lateinit var elderSignInBinding: ActivityElderSignInBinding
    private lateinit var preferenceManager: PreferenceManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        init()
        setContentView(elderSignInBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        listeners()
    }

    private fun init(){
        preferenceManager = PreferenceManager(this@ElderSignInActivity)
        elderSignInBinding = ActivityElderSignInBinding.inflate(layoutInflater)
        elderSignInBinding.elderCcp.registerCarrierNumberEditText(elderSignInBinding.elderPhnLogin.editText)
    }

    private fun listeners(){
        elderSignInBinding.backBtn.setOnClickListener {
            finish()
        }

        elderSignInBinding.elderLoginSignup.setOnClickListener {
            startActivity(Intent(this@ElderSignInActivity,ElderSignUpActivity::class.java))
        }

        elderSignInBinding.elderLogin.setOnClickListener {
            val phone = elderSignInBinding.elderCcp.fullNumberWithPlus.toString()
            val password = elderSignInBinding.elderPassLogin.editText?.text.toString()
            loading()
            if(validate(password)){
                Toast.makeText(this@ElderSignInActivity,"Valid",Toast.LENGTH_LONG).show()
                val database = FirebaseFirestore.getInstance()
                database.collection(Constants.KEY_ELDER_COLLECTION)
                    .whereEqualTo(Constants.KEY_ELDER_PHONE,phone)
                    .whereEqualTo(Constants.KEY_PASSWORD,password)
                    .get()
                    .addOnCompleteListener {task->
                        if (task.result != null && task.isSuccessful && task.result.documents.size>0) {
                            //found
                            val document = task.result.documents.get(0)
                            preferenceManager.putBoolean(Constants.KEY_IS_ELDER_SIGNED_IN,true)
                            preferenceManager.putString(Constants.KEY_ELDER_ID,document.id)
                            preferenceManager.putString(
                                Constants.KEY_ELDER_PHONE,document.getString(
                                    Constants.KEY_ELDER_PHONE)!!)
                            preferenceManager.putString(
                                Constants.KEY_ELDER_NAME,document.getString(
                                    Constants.KEY_ELDER_NAME)!!)
                            preferenceManager.putString(
                                Constants.KEY_ELDER_DOB,document.getString(
                                    Constants.KEY_ELDER_DOB)!!)
                            preferenceManager.putString(
                                Constants.KEY_ELDER_DESCRIPTION,document.getString(
                                    Constants.KEY_ELDER_DESCRIPTION)!!)

                            val age = document.get(Constants.KEY_ELDER_AGE).toString()

                            preferenceManager.putInt(Constants.KEY_ELDER_AGE,age.toInt())
                            preferenceManager.putString(
                                Constants.KEY_ELDER_PROFILE,document.getString(
                                    Constants.KEY_ELDER_PROFILE)!!)
                            preferenceManager.putString(Constants.KEY_ELDER_SMART_WATCH,document.getString(Constants.KEY_ELDER_SMART_WATCH)!!);

                            val intent = Intent(this@ElderSignInActivity,ElderHomeActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                            unloading()
                            startActivity(intent)
                        }
                        else {
                            //not found
                            unloading()
                            Toast.makeText(this@ElderSignInActivity,"Credentials not found",Toast.LENGTH_SHORT).show()
                        }
                    }
            }else
                unloading()
            }
    }

    private fun validate(password:String?):Boolean{
        var valid = true
        if (!Validation.isPassValid(password)){
            elderSignInBinding.elderPassLogin.error = "Invalid Password"
            valid = false
        }
        else{
            elderSignInBinding.elderPassLogin.error = ""
        }

        if(!elderSignInBinding.elderCcp.isValidFullNumber){
            elderSignInBinding.elderPhnLogin.error = "Invalid Phone"
        }
        else{
            elderSignInBinding.elderPhnLogin.error = ""
        }
        return valid
    }

    private fun loading(){
        elderSignInBinding.elderLogin.visibility = View.GONE
        elderSignInBinding.elderLoginProgress.visibility = View.VISIBLE
    }

    private fun unloading(){
        elderSignInBinding.elderLoginProgress.visibility = View.GONE
        elderSignInBinding.elderLogin.visibility = View.VISIBLE
    }
}
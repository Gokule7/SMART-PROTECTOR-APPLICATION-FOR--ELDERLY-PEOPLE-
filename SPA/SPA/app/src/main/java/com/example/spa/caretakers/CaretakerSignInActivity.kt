package com.example.spa.caretakers

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.spa.R
import com.example.spa.databinding.ActivityCaretakerSignInBinding
import com.example.spa.utils.helpers.Constants
import com.example.spa.utils.helpers.PreferenceManager
import com.example.spa.utils.helpers.Validation
import com.google.firebase.firestore.FirebaseFirestore

class CaretakerSignInActivity : AppCompatActivity() {
    private lateinit var caretakerSignInBinding: ActivityCaretakerSignInBinding
    private lateinit var preferenceManager: PreferenceManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        init()
        setContentView(caretakerSignInBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        listeners()
    }

    private fun init(){
        preferenceManager = PreferenceManager(this@CaretakerSignInActivity)
        caretakerSignInBinding = ActivityCaretakerSignInBinding.inflate(layoutInflater)
        caretakerSignInBinding.caretakerLoginCcp.registerCarrierNumberEditText(caretakerSignInBinding.caretakerPhnLogin.editText)
    }

    private fun listeners(){
        caretakerSignInBinding.caretakerLoginBackBtn.setOnClickListener {
            finish()
        }

        caretakerSignInBinding.caretakerLoginSignup.setOnClickListener {
            startActivity(Intent(this@CaretakerSignInActivity,CaretakerSignUpActivity::class.java))
        }

        caretakerSignInBinding.caretakerLogin.setOnClickListener {
            val password = caretakerSignInBinding.caretakerPassLogin.editText?.text.toString()
            val phone = caretakerSignInBinding.caretakerLoginCcp.fullNumberWithPlus.toString()
            loading()
            if(validate(password,phone)){
                val database = FirebaseFirestore.getInstance()
                database.collection(Constants.KEY_CARETAKER_COLLECTION)
                    .whereEqualTo(Constants.KEY_CARETAKER_PHONE,phone)
                    .whereEqualTo(Constants.KEY_PASSWORD,password)
                    .get()
                    .addOnCompleteListener {task->
                        if(task.result!=null && task.isSuccessful && task.result.documents.size>0){
                            val document = task.result.documents.get(0)
                            preferenceManager.putBoolean(Constants.KEY_IS_CARETAKER_SIGNED_IN,true)
                            preferenceManager.putString(Constants.KEY_CARETAKER_ID,document.id)
                            preferenceManager.putString(
                                Constants.KEY_CARETAKER_NAME,document.getString(
                                    Constants.KEY_CARETAKER_NAME)!!)
                            preferenceManager.putString(
                                Constants.KEY_CARETAKER_PROFILE,document.getString(
                                    Constants.KEY_CARETAKER_PROFILE)!!)
                            preferenceManager.putString(
                                Constants.KEY_CARETAKER_PHONE,document.getString(
                                    Constants.KEY_CARETAKER_PHONE)!!)

                            val intent = Intent(this@CaretakerSignInActivity, CaretakerHomeActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                            unloading()
                            startActivity(intent)
                        }
                        else{
                            unloading()
                            Toast.makeText(this@CaretakerSignInActivity,"Not found",Toast.LENGTH_SHORT).show()
                        }
                    }
            }
            else{
                unloading()
            }
        }
    }

    private fun validate(password:String?,phone:String?):Boolean{
        var valid = true
        if(!caretakerSignInBinding.caretakerLoginCcp.isValidFullNumber){
            caretakerSignInBinding.caretakerPhnLogin.error = "Invalid Phone"
            valid = false
        }else{
            caretakerSignInBinding.caretakerPhnLogin.error=""
        }

        if (!Validation.isPassValid(password)){
            valid = false
            caretakerSignInBinding.caretakerPassLogin.error = "Invalid Password"
        }else{
            caretakerSignInBinding.caretakerPassLogin.error=""
        }
        return valid
    }

    private fun loading(){
        caretakerSignInBinding.caretakerLogin.visibility = View.GONE
        caretakerSignInBinding.caretakerLoginProgress.visibility = View.VISIBLE
    }

    private fun unloading(){
        caretakerSignInBinding.caretakerLoginProgress.visibility = View.GONE
        caretakerSignInBinding.caretakerLogin.visibility = View.VISIBLE
    }
}
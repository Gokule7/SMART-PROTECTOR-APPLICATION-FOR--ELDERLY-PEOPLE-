package com.example.spa.elders

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.spa.R
import com.example.spa.databinding.ActivityElderSignUpBinding
import com.example.spa.utils.helpers.Constants
import com.example.spa.utils.user_models.Elder
import com.example.spa.utils.helpers.Validation

class ElderSignUpActivity : AppCompatActivity() {
    private lateinit var elderSignUpBinding: ActivityElderSignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        init();
        setContentView(elderSignUpBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        listeners()
    }

    private fun init(){
        elderSignUpBinding = ActivityElderSignUpBinding.inflate(layoutInflater)
        elderSignUpBinding.elderSignupCcp.registerCarrierNumberEditText(elderSignUpBinding.elderPhnSignup.editText)
    }

    private fun listeners(){
        elderSignUpBinding.elderSignupBackBtn.setOnClickListener {
            finish()
        }

        elderSignUpBinding.elderSignupLogin.setOnClickListener {
            finish()
        }

        elderSignUpBinding.elderSignup.setOnClickListener {
            startActivity(Intent(this@ElderSignUpActivity,ElderAccountDetailsActivity::class.java))
        }

        elderSignUpBinding.elderSignup.setOnClickListener {
            val name = elderSignUpBinding.elderNameSignup.editText?.text.toString()
            val phone = elderSignUpBinding.elderSignupCcp.fullNumberWithPlus
            val password = elderSignUpBinding.elderPassSignup.editText?.text.toString()
            val confirm = elderSignUpBinding.elderConfirmPassSignup.editText?.text.toString()

            loading()
            if(validate(name,password,confirm)){
                val elderModel = Elder(
                    name,phone,password
                )

                val intent = Intent(this@ElderSignUpActivity,ElderAccountDetailsActivity::class.java)
                intent.putExtra(Constants.KEY_ELDER_PASS,elderModel)

                unloading()
                startActivity(intent)
            }
            else{
                unloading()
            }
        }
    }

    private fun validate(name:String?,pass:String?,confirm:String?):Boolean{
        var valid = true

        if(!Validation.isNameValid(name)){
            valid = false
            elderSignUpBinding.elderNameSignup.error = "Invalid Name"
        }
        else{
            elderSignUpBinding.elderNameSignup.error=""
        }

        if(!elderSignUpBinding.elderSignupCcp.isValidFullNumber){
            elderSignUpBinding.elderPhnSignup.error = "Invalid Phone"
            valid = false
        }else{
            elderSignUpBinding.elderPhnSignup.error=""
        }

        if (!Validation.isPassValid(pass)){
            valid = false
            elderSignUpBinding.elderPassSignup.error = "Invalid Password"
        }else{
            elderSignUpBinding.elderPassSignup.error=""
        }

        if (!Validation.isConfirmPassValid(pass,confirm)){
            elderSignUpBinding.elderConfirmPassSignup.error = "Invalid Custom Password"
            valid = false
        }else{
            elderSignUpBinding.elderConfirmPassSignup.error=""
        }

        return valid
    }

    private fun loading(){
        elderSignUpBinding.elderSignup.visibility = View.GONE
        elderSignUpBinding.elderSignupProgress.visibility = View.VISIBLE
    }

    private fun unloading(){
        elderSignUpBinding.elderSignupProgress.visibility = View.GONE
        elderSignUpBinding.elderSignup.visibility = View.VISIBLE
    }
}
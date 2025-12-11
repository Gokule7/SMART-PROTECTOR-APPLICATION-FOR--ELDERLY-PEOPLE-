package com.example.spa.caretakers

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.spa.R
import com.example.spa.databinding.ActivityCaretakerSignUpBinding
import com.example.spa.home.OtpActivity
import com.example.spa.utils.helpers.Constants
import com.example.spa.utils.helpers.EncoderDecoder
import com.example.spa.utils.helpers.Validation
import com.example.spa.utils.user_models.Caretaker

class CaretakerSignUpActivity : AppCompatActivity() {
    private lateinit var caretakerSignUpBinding: ActivityCaretakerSignUpBinding
    private lateinit var caretakerProfile:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        init()
        setContentView(caretakerSignUpBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        listeners()
    }

    private fun init(){
        caretakerSignUpBinding = ActivityCaretakerSignUpBinding.inflate(layoutInflater)
        caretakerSignUpBinding.caretakerSignupCcp.registerCarrierNumberEditText(caretakerSignUpBinding.caretakerPhnSignup.editText)
    }


    private fun listeners(){
        caretakerSignUpBinding.caretakerSignupBackBtn.setOnClickListener {
            finish()
        }

        caretakerSignUpBinding.caretakerSignupLogin.setOnClickListener {
            finish()
        }

        caretakerSignUpBinding.caretakerSignup.setOnClickListener {
            val name = caretakerSignUpBinding.caretakerNameSignup.editText?.text.toString()
            val phone = caretakerSignUpBinding.caretakerSignupCcp.fullNumberWithPlus.toString()
            val password = caretakerSignUpBinding.caretakerPassSignup.editText?.text.toString()
            val confirm = caretakerSignUpBinding.caretakerConfirmPassSignup.editText?.text.toString()

            loading()
            if(validate(name,phone,password,confirm)){
                if(caretakerProfile.isNotEmpty()){
                    val caretaker = Caretaker(
                        name,phone,password, caretakerProfile
                    )

                    //store it in firestore
                    val intent = Intent(this@CaretakerSignUpActivity,OtpActivity::class.java)
                    intent.putExtra(Constants.KEY_USER, Constants.KEY_CARETAKER_COLLECTION)
                    intent.putExtra(Constants.KEY_CARETAKER_PASS,caretaker)
                    startActivity(intent)
                }
                else{
                    Toast.makeText(this@CaretakerSignUpActivity,"Add Profile",Toast.LENGTH_SHORT).show()
                }
            }else {
                unloading()
            }
        }

        caretakerSignUpBinding.addCaretakerImg.setOnClickListener {
            launcher.launch("image/*")
        }

    }

    private fun validate(name:String?,phone:String?,password:String?,confirm:String?):Boolean{
        var valid = true

        if(!Validation.isNameValid(name)){
            valid = false
            caretakerSignUpBinding.caretakerNameSignup.error = "Invalid Name"
        }
        else{
            caretakerSignUpBinding.caretakerNameSignup.error=""
        }

        if(!caretakerSignUpBinding.caretakerSignupCcp.isValidFullNumber){
            caretakerSignUpBinding.caretakerPhnSignup.error = "Invalid Phone"
            valid = false
        }else{
            caretakerSignUpBinding.caretakerPhnSignup.error=""
        }

        if (!Validation.isPassValid(password)){
            valid = false
            caretakerSignUpBinding.caretakerPassSignup.error = "Invalid Password"
        }else{
            caretakerSignUpBinding.caretakerPassSignup.error=""
        }

        if (!Validation.isConfirmPassValid(password,confirm)){
            caretakerSignUpBinding.caretakerConfirmPassSignup.error = "Invalid Custom Password"
            valid = false
        }else{
            caretakerSignUpBinding.caretakerConfirmPassSignup.error=""
        }

        return valid
    }

    private val launcher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ){uri: Uri?->
        if (uri!=null){
            Glide.with(this@CaretakerSignUpActivity)
                .load(uri)
                .error(R.drawable.error)
                .placeholder(R.drawable.place_holder)
                .into(caretakerSignUpBinding.caretakerProfileImg)

            caretakerProfile = EncoderDecoder.encodeImage(uri,this@CaretakerSignUpActivity)
        }
        else{
            Toast.makeText(this@CaretakerSignUpActivity,"Uri null",Toast.LENGTH_SHORT).show()
        }
    }

    private fun loading(){
        caretakerSignUpBinding.caretakerSignup.visibility = View.GONE
        caretakerSignUpBinding.caretakerSignupProgress.visibility = View.VISIBLE
    }

    private fun unloading(){
        caretakerSignUpBinding.caretakerSignupProgress.visibility = View.GONE
        caretakerSignUpBinding.caretakerSignup.visibility = View.VISIBLE
    }
}
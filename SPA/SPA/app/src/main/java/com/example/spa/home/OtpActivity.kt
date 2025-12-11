package com.example.spa.home

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.spa.R
import com.example.spa.caretakers.CaretakerHomeActivity
import com.example.spa.databinding.ActivityOtpBinding
import com.example.spa.elders.ElderHomeActivity
import com.example.spa.utils.helpers.Constants
import com.example.spa.utils.helpers.PreferenceManager
import com.example.spa.utils.user_models.Caretaker
import com.example.spa.utils.user_models.Connect
import com.example.spa.utils.user_models.Elder
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit

class OtpActivity : AppCompatActivity() {
    private lateinit var otpBinding: ActivityOtpBinding
    private lateinit var from:String
    private lateinit var eldermodel:Elder
    private lateinit var caretakermodel:Caretaker
    private lateinit var connect: Connect
    private var storedVerificationId: String = ""
    private lateinit var auth: FirebaseAuth
    private lateinit var options:PhoneAuthOptions
    private lateinit var phone:String
    private lateinit var token :PhoneAuthProvider.ForceResendingToken
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        init()
        setContentView(otpBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        listeners()
    }

    private fun init(){
        otpBinding = ActivityOtpBinding.inflate(layoutInflater)
        from = intent.getStringExtra(Constants.KEY_USER)!!
        auth = FirebaseAuth.getInstance()
        preferenceManager = PreferenceManager(this@OtpActivity)
        loading()
        if(from== Constants.KEY_ELDER_COLLECTION){
            eldermodel = intent.getSerializableExtra(Constants.KEY_ELDER_PASS) as Elder
            phone = eldermodel.elderPhone
            otpBinding.otpNumTxt.text = phone
            generateOtp(eldermodel.elderPhone,false)
        }else if(from== Constants.KEY_CARETAKER_COLLECTION){
            caretakermodel = intent.getSerializableExtra(Constants.KEY_CARETAKER_PASS) as Caretaker
            phone = caretakermodel.caretakerPhone
            otpBinding.otpNumTxt.text = phone
            generateOtp(caretakermodel.caretakerPhone,false)
        }else{
            Log.d("OtpActivity","unknown")
            //Connected
            connect = intent.getSerializableExtra(Constants.KEY_CONNECT_DATA) as Connect
            phone = connect.elderPhone
            otpBinding.otpNumTxt.text = phone
            generateOtp(phone,false)
        }

    }

    private fun listeners(){
        otpBinding.otpBackBtn.setOnClickListener {
            finish()
        }

        otpBinding.otpVerify.setOnClickListener {
            loading()
            verify(otpBinding.otpNumBox.text.toString())
        }

        otpBinding.otpResend.setOnClickListener {
            loading()
            otpBinding.otpNumBox.text = null
            generateOtp(phone,true)
        }

    }

    private fun generateOtp(phone:String,resend:Boolean){
        if(!resend){
            options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phone)
                .setTimeout(60L,TimeUnit.SECONDS)
                .setActivity(this@OtpActivity)
                .setCallbacks(callbacks)
                .build()
        }else{
            options  = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this@OtpActivity)
                .setCallbacks(callbacks)
                .setForceResendingToken(token)
                .build();
        }
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private val callbacks = object :PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
        override fun onVerificationCompleted(credential:PhoneAuthCredential) {
            Log.d("OtpActivity", "onVerificationCompleted")
            val code = credential.smsCode
            if (code != null) {
                Log.d("OtpActivity", "Retrieved OTP automatically: $code")
                otpBinding.otpNumBox.setText(code)
                verify(code)
            } else {
                Log.d("OtpActivity", "No SMS code found in credential.")
                // Consider displaying a message to the user
                // or providing guidance for manual entry
            }
        }

        override fun onVerificationFailed(e: FirebaseException) {
            Log.d("OtpActivity",e.message.toString())
            unloading()
            Toast.makeText(this@OtpActivity, e.localizedMessage, Toast.LENGTH_SHORT).show()
        }

        override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
            Log.d("OtpActivity", "onCodeSent")
            Toast.makeText(this@OtpActivity,"Sent",Toast.LENGTH_SHORT).show()
            super.onCodeSent(verificationId, token)
            this@OtpActivity.storedVerificationId = verificationId
            this@OtpActivity.token = token
            resendDisable()
            startCountDown()
            unloading()
        }
    }

    private fun loading(){
        otpBinding.otpVerify.visibility = View.GONE
        otpBinding.otpProgress.visibility = View.VISIBLE
    }

    private fun unloading(){
        otpBinding.otpProgress.visibility = View.GONE
        otpBinding.otpVerify.visibility = View.VISIBLE
    }

    private fun resendEnable(){
        otpBinding.otpResend.isEnabled = true
    }

    private fun resendDisable(){
        otpBinding.otpResend.isEnabled = false
    }

    private fun startCountDown(){
        object : CountDownTimer(60000,1000){
            override fun onTick(millisUntilFinished: Long) {
                val sec = (millisUntilFinished/1000)%60
                otpBinding.otpResend.text = "Resend otp in $sec seconds"
            }

            override fun onFinish() {
                otpBinding.otpResend.text = "Resend"
                resendEnable()
            }

        }.start()
    }

    private fun verify(code:String){
        Log.d("OtpActivity", "verification")
        val credential = PhoneAuthProvider.getCredential(storedVerificationId, code)
        signInWithCredential(credential);
    }

    private fun signInWithCredential(credential : PhoneAuthCredential){
        auth.signInWithCredential(credential)
            .addOnCompleteListener {task->
                if (task.isSuccessful){
                    createAccount();
                }else{
                    unloading()
                    Log.d("OtpActivity", task.exception?.message.toString())
                    Toast.makeText(this@OtpActivity, "Incorrect OTP", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun createAccount(){
        Toast.makeText(this@OtpActivity,"Account Creating....",Toast.LENGTH_LONG).show()
        //Acc creation logic

        val database = FirebaseFirestore.getInstance()

        //Elder
        if(from== Constants.KEY_ELDER_COLLECTION){
            database.collection(Constants.KEY_ELDER_COLLECTION)
                .add(eldermodel)
                .addOnSuccessListener { doc ->
                    preferenceManager.putBoolean(Constants.KEY_IS_ELDER_SIGNED_IN,true)
                    preferenceManager.putString(Constants.KEY_ELDER_ID,doc.id)
                    preferenceManager.putString(Constants.KEY_ELDER_NAME,eldermodel.elderName)
                    preferenceManager.putString(Constants.KEY_ELDER_PHONE,eldermodel.elderPhone)
                    preferenceManager.putString(Constants.KEY_ELDER_PROFILE,eldermodel.elderProfile)
                    preferenceManager.putString(Constants.KEY_ELDER_DOB,eldermodel.elderDob)
                    preferenceManager.putInt(Constants.KEY_ELDER_AGE,eldermodel.elderAge)
                    preferenceManager.putString(Constants.KEY_ELDER_DESCRIPTION,eldermodel.elderDescription)

                    val intent = Intent(this@OtpActivity, ElderHomeActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    unloading()
                    startActivity(intent)
                }.addOnFailureListener {failure->
                    unloading();
                    Toast.makeText(this@OtpActivity, failure.message.toString(), Toast.LENGTH_SHORT).show();
                }
        }

        //caretaker
        else if(from== Constants.KEY_CARETAKER_COLLECTION){
            database.collection(Constants.KEY_CARETAKER_COLLECTION)
                .add(caretakermodel)
                .addOnSuccessListener { doc ->
                    preferenceManager.putBoolean(Constants.KEY_IS_CARETAKER_SIGNED_IN,true)
                    preferenceManager.putString(Constants.KEY_CARETAKER_ID,doc.id)
                    preferenceManager.putString(Constants.KEY_CARETAKER_NAME,caretakermodel.caretakerName)
                    preferenceManager.putString(Constants.KEY_CARETAKER_PHONE,caretakermodel.caretakerPhone)
                    preferenceManager.putString(Constants.KEY_CARETAKER_PROFILE,caretakermodel.caretakerProfile)

                    val intent = Intent(this@OtpActivity, CaretakerHomeActivity ::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    unloading()
                    startActivity(intent)
                }.addOnFailureListener {failure->
                    unloading();
                    Toast.makeText(this@OtpActivity, failure.message.toString(), Toast.LENGTH_SHORT).show();
                }
        }

        //connected
        else{
            database.collection(Constants.KEY_ELDER_COLLECTION)
                .whereEqualTo(Constants.KEY_ELDER_PHONE,connect.elderPhone)
                .get()
                .addOnCompleteListener {
                    if(it.result != null && it.isSuccessful && it.result.documents.size>0){
                        val doc = it.result.documents[0]
                        connect.elderName = doc.getString(Constants.KEY_ELDER_NAME)!!
                        connect.elderProfile = doc.getString(Constants.KEY_ELDER_PROFILE)!!

                        database.collection(Constants.KEY_CONNECT_COLLECTION)
                            .add(connect)
                            .addOnSuccessListener {
                                val intent = Intent(this@OtpActivity, CaretakerHomeActivity ::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                unloading()
                                startActivity(intent)
                                finish()
                                Toast.makeText(this@OtpActivity,"Elder is Connected Successfully!",Toast.LENGTH_SHORT).show()
                            }.addOnFailureListener {failure->
                                unloading();
                                Toast.makeText(this@OtpActivity, failure.message.toString(), Toast.LENGTH_SHORT).show();
                            }
                    }
                    else{
                        Toast.makeText(this@OtpActivity,"Task null",Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this@OtpActivity,it.localizedMessage,Toast.LENGTH_SHORT).show()
                }
        }
    }
}
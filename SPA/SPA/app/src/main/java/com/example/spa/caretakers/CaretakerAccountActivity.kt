package com.example.spa.caretakers

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
import com.example.spa.databinding.ActivityCaretakerAccountBinding
import com.example.spa.utils.helpers.Constants
import com.example.spa.utils.helpers.EncoderDecoder
import com.example.spa.utils.helpers.PreferenceManager
import com.example.spa.utils.helpers.Validation
import com.google.firebase.firestore.FirebaseFirestore

class CaretakerAccountActivity : AppCompatActivity() {
    private lateinit var img:String
    private lateinit var account : ActivityCaretakerAccountBinding
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var db:FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        account = ActivityCaretakerAccountBinding.inflate(layoutInflater)
        setContentView(account.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        init()
        listeners()
    }

    private fun listeners(){
        account.caretakerUpdate.setOnClickListener {
            //check details
            val name = account.caretakerNameAcc.editText?.text.toString()
            val phone = account.caretakerAccCcp.fullNumberWithPlus.toString()

            if(checkValid(name)){
                if(name!= preferenceManager.getString(Constants.KEY_CARETAKER_NAME) || phone!= preferenceManager.getString(
                        Constants.KEY_CARETAKER_PHONE) || img!= preferenceManager.getString(
                        Constants.KEY_CARETAKER_PROFILE)){
                    val id = preferenceManager.getString(Constants.KEY_CARETAKER_ID)
                    id?.let {
                        val doc = db.collection(Constants.KEY_CARETAKER_COLLECTION).document(id)
                        val map = mapOf(
                            Constants.KEY_CARETAKER_NAME to name,
                            Constants.KEY_CARETAKER_PHONE to phone,
                            Constants.KEY_CARETAKER_PROFILE to img
                        )

                        doc.update(map)
                            .addOnSuccessListener {
                                preferenceManager.putString(Constants.KEY_CARETAKER_NAME,name)
                                preferenceManager.putString(Constants.KEY_CARETAKER_PHONE,phone)
                                preferenceManager.putString(Constants.KEY_CARETAKER_PROFILE,img)

                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this@CaretakerAccountActivity,"Issue :${it.localizedMessage}",Toast.LENGTH_SHORT).show()
                            }
                    }?:Toast.makeText(this@CaretakerAccountActivity,"ID null",Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(this@CaretakerAccountActivity,"Update details before!",Toast.LENGTH_SHORT).show()
                }
            }
        }

        account.editCaretakerImg.setOnClickListener {
            launcher.launch("image/*")
        }

        account.caretakerLoginBackBtn.setOnClickListener {
            finish()
        }
    }

    private val launcher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ){uri:Uri?->
        if(uri!=null){
            Glide.with(this@CaretakerAccountActivity)
                .load(uri)
                .error(R.drawable.error)
                .placeholder(R.drawable.place_holder)
                .into(account.caretakerAccImg)

            img = EncoderDecoder.encodeImage(uri,this@CaretakerAccountActivity)
        }else{
            Toast.makeText(this@CaretakerAccountActivity,"Null ",Toast.LENGTH_SHORT).show()
        }
    }

    private fun init(){
        account.caretakerAccCcp.registerCarrierNumberEditText(account.caretakerPhnAcc.editText)
        preferenceManager = PreferenceManager(this@CaretakerAccountActivity)
        db = FirebaseFirestore.getInstance()
        setDetails()
    }

    private fun setDetails(){
        loading()
        account.caretakerAccImg.setImageBitmap(
            EncoderDecoder.decodeImage(
                preferenceManager.getString(Constants.KEY_CARETAKER_PROFILE)!!
            )
        )

        account.caretakerNameAcc.editText!!.setText(preferenceManager.getString(Constants.KEY_CARETAKER_NAME))
        account.caretakerPhnAcc.editText!!.setText(preferenceManager.getString(Constants.KEY_CARETAKER_PHONE)!!.substring(3))
        account.caretakerAccCcp.setCountryForPhoneCode(
            preferenceManager.getString(Constants.KEY_CARETAKER_PHONE)!!.substring(1,4).toInt()
        )
        unloading()
    }

    private fun loading(){
        account.caretakerAccProgress.visibility = View.VISIBLE
        account.caretakerUpdate.visibility = View.GONE
    }

    private fun unloading(){
        account.caretakerUpdate.visibility = View.VISIBLE
        account.caretakerAccProgress.visibility = View.GONE
    }

    private fun checkValid(name:String):Boolean{
        if(!Validation.isNameValid(name)){
            account.caretakerNameAcc.error = "Invalid name"
            return false
        }
        else{
            account.caretakerNameAcc.error = null
        }

        if (!account.caretakerAccCcp.isValidFullNumber){
            account.caretakerPhnAcc.error = "Invalid phone"
            return false;
        }
        else{
            account.caretakerPhnAcc.error = null
        }

        return true;
    }
}
package com.example.spa.elders

import android.app.DatePickerDialog
import android.content.Intent
import android.icu.util.Calendar
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.spa.R
import com.example.spa.databinding.ActivityElderAccountDetailsBinding
import com.example.spa.home.OtpActivity
import com.example.spa.utils.helpers.Constants
import com.example.spa.utils.helpers.EncoderDecoder
import com.example.spa.utils.user_models.Elder
import com.google.firebase.firestore.FirebaseFirestore

class ElderAccountDetailsActivity : AppCompatActivity() {

    private lateinit var elderAccBinding: ActivityElderAccountDetailsBinding
    private lateinit var elderProfile: String
    private lateinit var eldermodel: Elder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        init()
        setContentView(elderAccBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        listeners()
    }

    private fun init() {
        elderAccBinding = ActivityElderAccountDetailsBinding.inflate(layoutInflater)
        eldermodel = intent.getSerializableExtra(Constants.KEY_ELDER_PASS) as Elder
    }

    private fun listeners() {
        elderAccBinding.eldersAccBackBtn.setOnClickListener {
            finish()
        }

        elderAccBinding.elderAgeSelector.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val date = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this@ElderAccountDetailsActivity,
                { _, year1, month1, dayOfMonth ->
                    elderAccBinding.elderAgeSelector.text = "$dayOfMonth/${month1 + 1}/$year1"
                    eldermodel.elderDob = elderAccBinding.elderAgeSelector.text.toString()
                    val dob = Calendar.getInstance()
                    dob.set(year1, month1, dayOfMonth)

                    // Function to calculate age
                    calculateAge(dob)
                },
                year, month, date
            )
            datePickerDialog.show()

        }

        elderAccBinding.addElderImg.setOnClickListener {
            launcher.launch("image/*")
        }

        elderAccBinding.elderAccNext.setOnClickListener {
            loading()
            val description: String = elderAccBinding.elderMedDesc.editText?.text.toString()
            if (eldermodel.elderAge != 0 && eldermodel.elderDob != "null" && description.isNotEmpty() && elderProfile.isNotEmpty()) {
                //Create a Account here
                eldermodel.elderProfile = elderProfile
                eldermodel.elderDescription = description

                val intent = Intent(this@ElderAccountDetailsActivity,OtpActivity::class.java)
                intent.putExtra(Constants.KEY_USER, Constants.KEY_ELDER_COLLECTION)
                intent.putExtra(Constants.KEY_ELDER_PASS,eldermodel)
                unloading()
                startActivity(intent)

            }
            else{
                unloading()
//                Log.d("ElderAccountDetailsActivity",elderProfile)
//                Log.d("ElderAccountDetailsActivity",description)
//                Log.d("ElderAccountDetailsActivity",eldermodel.elderAge.toString())
//                Log.d("ElderAccountDetailsActivity",eldermodel.elderDob)
                Toast.makeText(this@ElderAccountDetailsActivity,"Enter all details",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun calculateAge(dob: Calendar) {
        val calendar = Calendar.getInstance()
        var age = calendar.get(Calendar.YEAR) - dob.get(Calendar.YEAR)
        if (calendar.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--
        }

        eldermodel.elderAge = age
        Log.d("ElderAccountDetailsActivity", "Calculated Age: $age")
    }

    private val launcher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            Glide.with(this@ElderAccountDetailsActivity)
                .load(uri)
                .error(R.drawable.error)
                .placeholder(R.drawable.place_holder)
                .into(elderAccBinding.elderImg)

            elderProfile = EncoderDecoder.encodeImage(uri, this@ElderAccountDetailsActivity)
            unloading()
        }
    }

    private fun loading() {
        elderAccBinding.elderAccNext.visibility = View.GONE
        elderAccBinding.elderAccDetailsProgress.visibility = View.VISIBLE
    }

    private fun unloading() {
        elderAccBinding.elderAccDetailsProgress.visibility = View.GONE
        elderAccBinding.elderAccNext.visibility = View.VISIBLE
    }

    private fun createAccount() {
        val database = FirebaseFirestore.getInstance()

    }
}
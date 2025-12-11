package com.example.spa.reminders.schedulers

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
import com.example.spa.databinding.ActivityAddMedicationBinding
import com.example.spa.utils.helpers.Constants
import com.example.spa.utils.helpers.EncoderDecoder
import com.example.spa.utils.helpers.PreferenceManager
import com.example.spa.utils.user_models.ConnectedElder
import com.example.spa.utils.user_models.MedReminder
import com.example.spa.utils.user_models.Medication
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class AddMedicationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddMedicationBinding
    private lateinit var model:ConnectedElder
    private var count = 0
    private var medTime = ""
    private var medImg = ""
    private lateinit var medicines : MutableList<Medication>
    private lateinit var db:FirebaseFirestore
    private lateinit var preferenceManager: PreferenceManager
    private var noOfMed = -1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        init()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        listeners()
    }

    private fun listeners(){
        binding.addMedBack.setOnClickListener {
            finish()
        }

        binding.addMedTime.setOnClickListener {
            val picker = MaterialTimePicker.Builder()
                .setTitleText("Set Medicine Time")
                .setHour(12)
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setMinute(0)
                .build()

            picker.show(supportFragmentManager,"SPA")
            picker.addOnPositiveButtonClickListener {
                if(picker.hour>12){
                    binding.addMedTime.text = "${picker.hour-12}:${picker.minute}PM"
                }else{
                    binding.addMedTime.text = "${picker.hour}:${picker.minute}PM"
                }

                medTime = EncoderDecoder.timePickerToString(picker)
            }
        }

        binding.addMedPic.setOnClickListener {
            launcher.launch("image/*")
        }

        binding.addMedNext.setOnClickListener {
            if(checkDetails()){
                loading()
                count++
                val med = Medication(
                    medImage = medImg,
                    medicineTime = medTime,
                    medicineName = binding.addMedName.editText?.text.toString(),
                    medicinePurpose = binding.addMedPurpose.editText?.text.toString(),
                )
                medicines.add(med)
                clearDetails()
                if(count==noOfMed){
                    storeInDb()
                }
                unloading()
            }
        }
    }

    private fun clearDetails(){
        medImg = ""
        medTime = ""
        binding.med.setImageBitmap(null)
        binding.addMedTime.text = "Add Medicine Time!"
        binding.addMedName.editText?.text = null
        binding.addMedPurpose.editText?.text = null
        binding.addMedName.requestFocus()
        showToast("Add another medicine!")
    }

    private fun checkDetails():Boolean{
        binding.addMedName.error = null
        binding.addMedPurpose.error = null
        val medname = binding.addMedName.editText?.text.toString()
        val medpurpose = binding.addMedPurpose.editText?.text.toString()
        if(medImg.isEmpty()){
            showToast("Add Medicine Image!")
            return false
        }

        if (medname.isEmpty()){
            binding.addMedName.error = "Add Medicine Name!"
            return false
        }

        if(medpurpose.isEmpty()){
            binding.addMedPurpose.error = "Add Medicine Purpose!"
            return false
        }

        if(medTime.isEmpty()){
            showToast("Add Medicine Time!")
            return false
        }

        return true
    }

    private fun showToast(msg:String){
        Toast.makeText(this@AddMedicationActivity,msg,Toast.LENGTH_SHORT).show()
    }

    private fun init(){
        binding = ActivityAddMedicationBinding.inflate(layoutInflater)
        preferenceManager = PreferenceManager(this@AddMedicationActivity)
        db = FirebaseFirestore.getInstance()
        medicines = ArrayList()
        model = intent.getSerializableExtra("data") as ConnectedElder
        noOfMed = intent.getIntExtra("number",-1)
        if(noOfMed<=0)
            showToast("Invalid no of medicines")
    }

    private fun loading(){
        binding.addMedProgress.visibility = View.VISIBLE
        binding.addMedNext.visibility = View.GONE
    }

    private fun unloading(){
        binding.addMedProgress.visibility = View.GONE
        binding.addMedNext.visibility = View.VISIBLE
    }

    private val launcher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ){uri:Uri?->
        if(uri!=null){
            Glide.with(this@AddMedicationActivity)
                .load(uri)
                .error(R.drawable.error)
                .placeholder(R.drawable.place_holder)
                .into(binding.med)

            medImg = EncoderDecoder.encodeImage(uri,this@AddMedicationActivity)
        }
    }

    private fun storeInDb(){
        val remmodel = MedReminder(
            elderPhone = model.elderPhone,
            caretakerPhone = preferenceManager.getString(Constants.KEY_CARETAKER_PHONE)!!,
            noOfMed = noOfMed,
            medicines = medicines
        )

        db.collection(Constants.KEY_MEDICINE_COLLECTION)
            .whereEqualTo(Constants.KEY_ELDER_PHONE,model.elderPhone)
            .get()
            .addOnSuccessListener {
                //add new doc
                if(it.isEmpty){
                    db.collection(Constants.KEY_MEDICINE_COLLECTION)
                        .add(remmodel)
                        .addOnSuccessListener {
                            showToast("Successfully added")
                            finish()
                        }
                        .addOnFailureListener {it2->
                            showToast(it2.localizedMessage!!)
                            finish()
                        }
                    finish()
                }
                //update doc
                else{
                    val document = it.documents[0]
                    db.collection(Constants.KEY_MEDICINE_COLLECTION)
                        .document(document.id)
                        .set(remmodel, SetOptions.merge())
                        .addOnSuccessListener { _ ->
                            showToast("Successfully updated!")
                        }
                        .addOnFailureListener {it2->
                            showToast(it2.localizedMessage!!)
                        }
                    finish()
                }
            }
            .addOnFailureListener {
                showToast(it.localizedMessage!!)
                finish()
            }
    }
}
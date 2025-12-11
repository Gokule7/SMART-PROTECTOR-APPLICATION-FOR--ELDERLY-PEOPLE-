package com.example.spa.connected

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.spa.R
import com.example.spa.databinding.ActivitySetElderRemindersBinding
import com.example.spa.reminders.schedulers.AddCheckUpActivity
import com.example.spa.reminders.schedulers.NoOfMedActivity
import com.example.spa.utils.adapter.ReminderElderAdapter
import com.example.spa.utils.helpers.Constants
import com.example.spa.utils.helpers.PreferenceManager
import com.example.spa.utils.listeners.ElderReminderListener
import com.example.spa.utils.user_models.ConnectedElder
import com.google.firebase.firestore.FirebaseFirestore

class SetElderRemindersActivity : AppCompatActivity(),ElderReminderListener{
    private lateinit var binding:ActivitySetElderRemindersBinding
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var phone:String
    private lateinit var db:FirebaseFirestore
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
        loadElders()
    }

    private fun listeners(){
        binding.remElderBack.setOnClickListener {
            finish()
        }
    }

    private fun init(){
        binding = ActivitySetElderRemindersBinding.inflate(layoutInflater)
        preferenceManager = PreferenceManager(this@SetElderRemindersActivity)
        phone = preferenceManager.getString(Constants.KEY_CARETAKER_PHONE)!!
        db = FirebaseFirestore.getInstance()
    }

    private fun loadElders(){
        loading()
        db.collection(Constants.KEY_CONNECT_COLLECTION)
            .whereEqualTo(Constants.KEY_CARETAKER_PHONE,phone)
            .get()
            .addOnSuccessListener { docs->
                val elders:MutableList<ConnectedElder> = ArrayList()
                for(doc in docs){
                    val model = ConnectedElder(
                        elderName = doc.getString(Constants.KEY_ELDER_NAME)!!,
                        elderProfile = doc.getString(Constants.KEY_ELDER_PROFILE)!!,
                        elderPhone = doc.getString(Constants.KEY_ELDER_PHONE)!!
                    )
                    elders.add(model)

                    if(elders.size==0){
                        showError("No Connected Elders!")
                    }else{
                        val adapter = ReminderElderAdapter(lists = elders,this@SetElderRemindersActivity)
                        binding.remEldersList.adapter = adapter
                        unloading()
                    }
                }
            }.addOnFailureListener {
                showError(it.localizedMessage!!)
                unloading()
            }
    }

    private fun loading(){
        binding.remElderProgress.visibility = View.VISIBLE
        binding.remEldersList.visibility = View.GONE
        binding.errorMsg.visibility = View.GONE
    }

    private fun unloading(){
        binding.remElderProgress.visibility = View.GONE
        binding.remEldersList.visibility = View.VISIBLE
        binding.errorMsg.visibility = View.GONE
    }

    private fun showError(msg:String){
        unloading()
        binding.remEldersList.visibility = View.GONE
        binding.errorMsg.text = msg
        binding.errorMsg.visibility = View.VISIBLE
    }

    override fun onElderClicked(model: ConnectedElder, type: Int) {
        if(type == 1){
            val intent = Intent(this@SetElderRemindersActivity, NoOfMedActivity::class.java)
            intent.putExtra("data",model)
            startActivity(intent)
        }else if(type == 2){
            val intent = Intent(this@SetElderRemindersActivity, AddCheckUpActivity::class.java)
            intent.putExtra("data",model)
            startActivity(intent)
        }
    }
}
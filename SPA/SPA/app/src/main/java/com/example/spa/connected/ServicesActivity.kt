package com.example.spa.connected

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.spa.R
import com.example.spa.databinding.ActivityServicesBinding
import com.example.spa.utils.adapter.ServicesElderAdapter
import com.example.spa.utils.helpers.Constants
import com.example.spa.utils.helpers.PreferenceManager
import com.example.spa.utils.listeners.ServicesElderListener
import com.example.spa.utils.user_models.ConnectedElder
import com.google.firebase.firestore.FirebaseFirestore

class ServicesActivity : AppCompatActivity(), ServicesElderListener{
    private lateinit var db:FirebaseFirestore
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var binding: ActivityServicesBinding
    private lateinit var phone:String
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
        binding.servElderBack.setOnClickListener {
            finish()
        }
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
                        val adapter = ServicesElderAdapter(lists = elders,this@ServicesActivity)
                        binding.servEldersList.adapter = adapter
                        unloading()
                    }
                }
            }.addOnFailureListener {
                showError(it.localizedMessage!!)
                unloading()
            }
    }

    private fun init(){
        db = FirebaseFirestore.getInstance()
        preferenceManager = PreferenceManager(this@ServicesActivity)
        binding = ActivityServicesBinding.inflate(layoutInflater)
        phone = preferenceManager.getString(Constants.KEY_CARETAKER_PHONE)!!
    }

    private fun loading(){
        binding.servElderProgress.visibility = View.VISIBLE
        binding.servEldersList.visibility = View.GONE
        binding.errorMsg.visibility = View.GONE
    }

    private fun unloading(){
        binding.servElderProgress.visibility = View.GONE
        binding.servEldersList.visibility = View.VISIBLE
        binding.errorMsg.visibility = View.GONE
    }

    private fun showError(msg:String){
        unloading()
        binding.servEldersList.visibility = View.GONE
        binding.errorMsg.text = msg
        binding.errorMsg.visibility = View.VISIBLE
    }

    override fun onElderPressed(elderPhone: String, type: Int) {
        if(type == 1){
            loading()
            db.collection(Constants.KEY_ELDER_COLLECTION)
                .whereEqualTo(Constants.KEY_ELDER_PHONE, elderPhone) // Find the document where 'phone' matches
                .get()
                .addOnSuccessListener { document ->
                   if(!document.isEmpty){
                       val doc = document.documents[0]
                       val id = doc.id
                       db.collection(Constants.KEY_ELDER_COLLECTION)
                           .document(id)
                           .update(Constants.KEY_ELDER_SMART_WATCH,"yes")
                           .addOnSuccessListener {
                               unloading()
                               Toast.makeText(this@ServicesActivity,"Done",Toast.LENGTH_SHORT).show()
                               finish()
                           }
                           .addOnFailureListener {
                               unloading()
                               Toast.makeText(this@ServicesActivity, it.localizedMessage!!,Toast.LENGTH_SHORT).show()
                           }
                   }
                }
                .addOnFailureListener { e ->
                    unloading()
                    showError(e.localizedMessage!!)
                }
        }else if(type == 0){
            loading()
            db.collection(Constants.KEY_ELDER_COLLECTION)
                .whereEqualTo(Constants.KEY_ELDER_PHONE, elderPhone) // Find the document where 'phone' matches
                .get()
                .addOnSuccessListener { document ->
                    if(!document.isEmpty){
                        val doc = document.documents[0]
                        val id = doc.id
                        db.collection(Constants.KEY_ELDER_COLLECTION)
                            .document(id)
                            .update(Constants.KEY_ELDER_SMART_WATCH,"no")
                            .addOnSuccessListener {
                                unloading()
                                Toast.makeText(this@ServicesActivity,"Done",Toast.LENGTH_SHORT).show()
                                finish()
                            }
                            .addOnFailureListener {
                                unloading()
                                Toast.makeText(this@ServicesActivity, it.localizedMessage!!,Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener { e ->
                    unloading()
                    showError(e.localizedMessage!!)
                }
        }
    }
}
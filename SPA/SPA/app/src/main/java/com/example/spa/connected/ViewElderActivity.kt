package com.example.spa.connected

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.spa.R
import com.example.spa.databinding.ActivityViewElderBinding
import com.example.spa.utils.adapter.ElderListAdapter
import com.example.spa.utils.helpers.Constants
import com.example.spa.utils.helpers.PreferenceManager
import com.example.spa.utils.listeners.ViewElderListener
import com.example.spa.utils.user_models.ConnectedElder
import com.google.firebase.firestore.FirebaseFirestore

class ViewElderActivity : AppCompatActivity(), ViewElderListener {
    private lateinit var binding: ActivityViewElderBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var preferenceManager: PreferenceManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityViewElderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        db = FirebaseFirestore.getInstance()
        preferenceManager = PreferenceManager(this@ViewElderActivity)
        listeners()
        loadElders()
    }

    private fun loadElders() {
        loading()
        db.collection(Constants.KEY_CONNECT_COLLECTION)
            .whereEqualTo(Constants.KEY_CARETAKER_PHONE,preferenceManager.getString(Constants.KEY_CARETAKER_PHONE)!!)
            .get()
            .addOnCompleteListener {

                if(it.result!=null && it.isSuccessful){
                    val elders:MutableList<ConnectedElder> = ArrayList()
                    for(snapShot in it.result){
                        val model = ConnectedElder(
                            elderProfile = snapShot.getString(Constants.KEY_ELDER_PROFILE)!!,
                            elderPhone = snapShot.getString(Constants.KEY_ELDER_PHONE)!!,
                            elderName = snapShot.getString(Constants.KEY_ELDER_NAME)!!
                        )
                        Toast.makeText(this@ViewElderActivity,"elder added",Toast.LENGTH_SHORT).show()
                        elders.add(model)
                    }

                    if(elders.isEmpty()){
                        showMsg("No connected Elder's")
                    }else{
                        Toast.makeText(this@ViewElderActivity,"on adapter",Toast.LENGTH_SHORT).show()
                        val adapter = ElderListAdapter(elders,this@ViewElderActivity)
                        binding.connectedEldersList.adapter = adapter
                        unloading()
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this@ViewElderActivity,"failed...",Toast.LENGTH_SHORT).show()
                it.localizedMessage?.let{
                        it1 -> showMsg(it1)
                }
            }
    }

    private fun listeners(){
        binding.viewElderBack.setOnClickListener {
            finish()
        }
    }


    private fun loading() {
        binding.viewElderProgress.visibility = View.VISIBLE
        binding.connectedEldersList.visibility = View.GONE
    }

    private fun unloading() {
        binding.viewElderProgress.visibility = View.GONE
        binding.connectedEldersList.visibility = View.VISIBLE
    }

    private fun showMsg(msg: String) {
        binding.viewElderProgress.visibility = View.GONE
        binding.errorMsg.text = msg
        binding.errorMsg.visibility = View.VISIBLE
    }

    override fun onElderClicked(model: ConnectedElder) {
        val intent = Intent(this@ViewElderActivity, ConnectedEldersDetailsActivity::class.java)
        intent.putExtra("data", model)
        startActivity(intent)
    }
}
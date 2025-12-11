package com.example.spa.reminders.schedulers

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.spa.R
import com.example.spa.databinding.ActivityNoOfMedBinding
import com.example.spa.utils.user_models.ConnectedElder

class NoOfMedActivity : AppCompatActivity() {
    private lateinit var binding:ActivityNoOfMedBinding
    private lateinit var model:ConnectedElder
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

    private fun init(){
        binding = ActivityNoOfMedBinding.inflate(layoutInflater)
        model = intent.getSerializableExtra("data") as ConnectedElder
        Toast.makeText(this@NoOfMedActivity,model.elderPhone, Toast.LENGTH_SHORT).show()
    }

    private fun listeners(){
        binding.noofMedBackBtn.setOnClickListener {
            finish()
        }

        binding.noOfNext.setOnClickListener {
            checkDetail()
        }
    }

    private fun checkDetail(){
        binding.noofMed.error = null
        val no = binding.noofMed.editText?.text.toString()
        if(no.isNotEmpty()){
            val number = no.toInt()
            if(number<=0){
                binding.noofMed.error = "Invalid input"
            }else{
                val intent = Intent(this@NoOfMedActivity, AddMedicationActivity::class.java)
                intent.putExtra("number",number)
                intent.putExtra("data",model)
                startActivity(intent)
                finish()
            }
        }else{
            binding.noofMed.error = "Requires input!"
        }
    }
}
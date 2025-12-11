package com.example.spa.utils.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.spa.databinding.ElderHolderForReminderBinding
import com.example.spa.utils.helpers.EncoderDecoder
import com.example.spa.utils.listeners.ElderReminderListener
import com.example.spa.utils.user_models.ConnectedElder

class ReminderElderAdapter(private val lists : List<ConnectedElder>,private val listener: ElderReminderListener) : RecyclerView.Adapter<ReminderElderAdapter.ReminderElderHolder>() {
    class ReminderElderHolder(private val view: ElderHolderForReminderBinding, private val listener: ElderReminderListener) : RecyclerView.ViewHolder(view.root){
        private val holder : ElderHolderForReminderBinding = view
        fun setDetails(model:ConnectedElder){
            holder.elderListElderName.text = model.elderName
            holder.elderListElderProfile.setImageBitmap(
                EncoderDecoder.decodeImage(
                    model.elderProfile
                )
            )

            holder.addMed.setOnClickListener {
                //1 -> Add Medication
                listener.onElderClicked(model,1)
            }

            holder.addCheckup.setOnClickListener {
                //2-> add Checkup
                listener.onElderClicked(model,2)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderElderHolder {
        val holder = ElderHolderForReminderBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ReminderElderHolder(holder,listener)
    }

    override fun getItemCount(): Int {
        return lists.size;
    }

    override fun onBindViewHolder(holder: ReminderElderHolder, position: Int) {
        holder.setDetails(lists[position])
    }
}
package com.example.spa.utils.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.spa.databinding.ViewElderHolderBinding
import com.example.spa.utils.helpers.EncoderDecoder
import com.example.spa.utils.listeners.ViewElderListener
import com.example.spa.utils.user_models.ConnectedElder

class ElderListAdapter(private var elders: List<ConnectedElder>, private var listener: ViewElderListener) : RecyclerView.Adapter<ElderListAdapter.ElderHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ElderHolder {
        val holder = ViewElderHolderBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ElderHolder(holder,listener)
    }

    override fun getItemCount(): Int {
        return elders.size
    }

    override fun onBindViewHolder(holder: ElderHolder, position: Int) {
        holder.setDetails(elders[position])
    }

    class ElderHolder(private val view: ViewElderHolderBinding,private val listener: ViewElderListener) : RecyclerView.ViewHolder(view.root) {

        private var holder:ViewElderHolderBinding = view
        fun setDetails(model:ConnectedElder){
            holder.elderListElderPhone.text = model.elderPhone
            holder.elderListElderName.text = model.elderName
            holder.elderListElderProfile.setImageBitmap(
                EncoderDecoder.decodeImage(model.elderProfile)
            )

            holder.elderCard.setOnClickListener {
                listener.onElderClicked(model)
            }
        }
    }
}
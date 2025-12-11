package com.example.spa.utils.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.spa.databinding.ServicesElderHolderBinding
import com.example.spa.utils.helpers.EncoderDecoder
import com.example.spa.utils.listeners.ServicesElderListener
import com.example.spa.utils.user_models.ConnectedElder

class ServicesElderAdapter(private val lists:List<ConnectedElder>,private val listener: ServicesElderListener) : RecyclerView.Adapter<ServicesElderAdapter.ServicesElderHolder>() {
    class ServicesElderHolder(view : ServicesElderHolderBinding, private val listener: ServicesElderListener) : RecyclerView.ViewHolder(view.root){
        private val holder = view
        fun setDetails(model:ConnectedElder){
            holder.elderListElderName.text = model.elderName
            holder.elderListElderProfile.setImageBitmap(
                EncoderDecoder.decodeImage(
                    model.elderProfile
                )
            )

            holder.havewatch.setOnClickListener {
                listener.onElderPressed(model.elderPhone,1)
            }

            holder.nowatch.setOnClickListener {
                listener.onElderPressed(model.elderPhone,0)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServicesElderHolder {
        val holder = ServicesElderHolderBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ServicesElderHolder(holder,listener)
    }

    override fun getItemCount(): Int {
        return lists.size
    }

    override fun onBindViewHolder(holder: ServicesElderHolder, position: Int) {
        holder.setDetails(lists[position])
    }
}
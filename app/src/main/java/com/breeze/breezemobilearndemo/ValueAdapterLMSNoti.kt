package com.breezemobilearndemo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.breezemobilearndemo.domain.LMSNotiEntity


class ValueAdapterLMSNoti(private val notiL: ArrayList<LMSNotiEntity>) : RecyclerView.Adapter<ValueAdapterLMSNoti.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_value_for_lms_notification, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.holderItems()
    }

    override fun getItemCount(): Int {
        return notiL.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun holderItems(){
            var value_text_header = itemView.findViewById<AppCompatTextView>(R.id.value_text_header)
            var value_text = itemView.findViewById<AppCompatTextView>(R.id.value_text)

            value_text_header.text = notiL.get(adapterPosition).noti_header
            value_text.text = notiL.get(adapterPosition).noti_message
        }
    }
}
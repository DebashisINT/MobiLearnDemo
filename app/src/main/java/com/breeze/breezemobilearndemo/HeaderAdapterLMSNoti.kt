package com.breezemobilearndemo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView


class HeaderAdapterLMSNoti(private val notiRootL: ArrayList<LMSNotiFilterData>) : RecyclerView.Adapter<HeaderAdapterLMSNoti.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_header_for_lms_notification, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems()
    }

    override fun getItemCount(): Int {
        return notiRootL.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItems(){
            try {

                var noti_header_date = itemView.findViewById<AppCompatTextView>(R.id.noti_header_date)
                var nested_recycler_view = itemView.findViewById<RecyclerView>(R.id.nested_recycler_view)

                var dt =
                if(AppUtils.getCurrentDateForShopActi().equals(notiRootL.get(adapterPosition).noti_date)) "Today"
                else if(AppUtils.getOneDayPreviousDate(AppUtils.getCurrentDateForShopActi()).equals(notiRootL.get(adapterPosition).noti_date)) "Yesterday"
                else AppUtils.getFormatedDateNew(notiRootL.get(adapterPosition).noti_date,"yyyy-mm-dd","dd-mm-yyyy")!!

                noti_header_date.text = dt

                val valueAdapter = ValueAdapterLMSNoti(notiRootL.get(adapterPosition).notiL)
                nested_recycler_view.adapter = valueAdapter
            } catch (e: Exception) {
               e.printStackTrace()
            }
        }
    }
}
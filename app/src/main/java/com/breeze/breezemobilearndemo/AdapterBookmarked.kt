package com.breezemobilearndemo

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions


class AdapterBookmarkedprivate (val mContext: Context, private val mList: ArrayList<VidBookmark>,var listner: OnClick):RecyclerView.Adapter<AdapterBookmarkedprivate.BookmarkViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarkViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.row_bookmark,parent,false)
        return BookmarkViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    override fun onBindViewHolder(holder: BookmarkViewHolder, position: Int) {
        holder.bindItems()
    }
    inner class BookmarkViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){
        fun bindItems(){
            var tv_row_book_topic_name = itemView.findViewById<AppCompatTextView>(R.id.tv_row_book_topic_name)
            var iv_row_book_img = itemView.findViewById<ImageView>(R.id.iv_row_book_img)
            var tv_row_book_content_title = itemView.findViewById<AppCompatTextView>(R.id.tv_row_book_content_title)
            var tv_row_book_content_desc = itemView.findViewById<AppCompatTextView>(R.id.tv_row_book_content_desc)
            var ll_bookmark_root = itemView.findViewById<LinearLayout>(R.id.ll_bookmark_root)
            var iv_bookmark_del = itemView.findViewById<ImageView>(R.id.iv_bookmark_del)

            tv_row_book_topic_name.text = mList.get(adapterPosition).topic_name
            if (!mList.get(adapterPosition).content_bitmap.equals("")) {
                Glide.with(mContext)
                    .load(mList.get(adapterPosition).content_bitmap)
                    .apply(RequestOptions.placeholderOf(R.drawable.ic_image).error(R.drawable.ic_image))
                    .into(iv_row_book_img)
            }
            else{
                iv_row_book_img.setImageResource(R.drawable.ic_image)
            }
            tv_row_book_content_title.text = mList.get(adapterPosition).content_name
            tv_row_book_content_desc.text = mList.get(adapterPosition).content_desc

            ll_bookmark_root.setOnClickListener {
                listner.onClick(mList.get(adapterPosition))
            }
            iv_bookmark_del.setOnClickListener {
                listner.onDelClick(mList.get(adapterPosition))
            }
        }
    }

    interface OnClick {
        fun onClick(obj:VidBookmark)
        fun onDelClick(obj:VidBookmark)
    }
}
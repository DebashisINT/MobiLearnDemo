package com.breezemobilearndemo

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.breezemobilearndemo.databinding.RowVideoCommentBinding

class AdapterComment(
    private val mContext: Context,
    private val commentL: ArrayList<CommentL>
) : RecyclerView.Adapter<AdapterComment.CommentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = RowVideoCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return commentL.size
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(commentL[position])
    }

    fun clear() {
        commentL.clear()
        notifyDataSetChanged()
    }

    inner class CommentViewHolder(private val binding: RowVideoCommentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(comment: CommentL) {
            try {
                if (!comment.comment_description.isNullOrEmpty()) {
                    binding.tvRowVideoCmt.text = comment.comment_description
                    binding.tvRowVideoCmtDateTime.text = comment.comment_date_time
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
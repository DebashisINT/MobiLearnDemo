package com.breezemobilearndemo
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.breezemobilearndemo.databinding.GridviewItemNewBinding


class LmsSearchAdapter(
    private val mContext: Context,
    private val itemList: List<LmsSearchData>,
    private val fragType: String,
    private val itemClickListener: OnItemClickListener
) : RecyclerView.Adapter<LmsSearchAdapter.LmsSearchViewHolder>() {

    private var selectedPosition: Int = RecyclerView.NO_POSITION

    interface OnItemClickListener {
        fun onItemClick(item: LmsSearchData)
    }

    class LmsSearchViewHolder(private val binding: GridviewItemNewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindItems(
            context: Context,
            categoryList: List<LmsSearchData>,
            fragType: String,
            itemClickListener: OnItemClickListener,
            selectedPosition: Int,
            onPositionChange: (Int) -> Unit
        ) {
            val position = adapterPosition
            val item = categoryList[position]

            binding.tvSearchItem.text = "${item.courseName} (${item.video_count})"
            binding.learningProgressStatusTopic.progress = item.topic_parcentage.coerceAtMost(100)
            binding.tvParcentage.text = "${item.topic_parcentage}%"

            binding.llGrdItem.setOnClickListener {
                onPositionChange(if (selectedPosition == position) RecyclerView.NO_POSITION else position)
                itemClickListener.onItemClick(item)
            }

            if (fragType == "SearchLmsFrag") {
                binding.learningProgressStatusTopic.visibility = View.VISIBLE
                binding.llStatus.visibility = View.VISIBLE
                binding.tvParcentageStatus.text =
                    if (item.topic_parcentage == 100) "Completed" else "Pending"
                binding.tvParcentageStatus.setTextColor(
                    ContextCompat.getColor(
                        context,
                        if (item.topic_parcentage == 100) R.color.lms_cmplt else R.color.red_dot
                    )
                )
            } else if (fragType == "SearchLmsKnowledgeFrag") {
                binding.learningProgressStatusTopic.visibility = View.GONE
                binding.llStatus.visibility = View.GONE
            } else {
                binding.learningProgressStatusTopic.visibility = View.VISIBLE
                binding.llStatus.visibility = View.VISIBLE
                binding.tvParcentageStatus.text =
                    if (item.topic_parcentage == 100) "Completed" else "Pending"
                binding.tvParcentageStatus.setTextColor(
                    ContextCompat.getColor(
                        context,
                        if (item.topic_parcentage == 100) R.color.lms_cmplt else R.color.red_dot
                    )
                )
            }
            binding.llGrdItem.isSelected = position == selectedPosition
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LmsSearchViewHolder {
        val binding = GridviewItemNewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LmsSearchViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LmsSearchViewHolder, position: Int) {
        holder.bindItems(
            mContext,
            itemList,
            fragType,
            itemClickListener,
            selectedPosition
        ) { newPosition ->
            val previousPosition = selectedPosition
            selectedPosition = newPosition
            notifyItemChanged(previousPosition)
            notifyItemChanged(selectedPosition)
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    fun getSelectedPosition(): Int {
        return selectedPosition
    }
}

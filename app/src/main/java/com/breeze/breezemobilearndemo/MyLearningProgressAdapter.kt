package com.breezemobilearndemo

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.breezemobilearndemo.databinding.PerformanceItemBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class MyLearningProgressAdapter(
    private val mContext: Context,
    private val mList: ArrayList<ContentL>,
    private val topicName: String,
    private val listener: OnItemClickListener,
    private val contentWiseAnswerL: ArrayList<ContentWiseAnswerL>
) : RecyclerView.Adapter<MyLearningProgressAdapter.MyLearningProgressViewHolder>() {

    var isVidQuesComplete = false
    private var contentId: List<ContentWiseAnswerL>? = null

    interface OnItemClickListener {
        fun onItemClick(item: ContentL, position: Int)
        fun onRetryClick(item: ContentL, position: Int)
    }

    class MyLearningProgressViewHolder(
        val binding: PerformanceItemBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyLearningProgressViewHolder {
        val binding = PerformanceItemBinding.inflate(LayoutInflater.from(mContext), parent, false)
        return MyLearningProgressViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyLearningProgressViewHolder, position: Int) {
        val item = mList[position]
        val binding = holder.binding
        binding.llRetryIncorrectQuizHeader.isEnabled  = false

        if (item.content_url != null) {
            Glide.with(mContext)
                .load(item.content_thumbnail)
                .apply(RequestOptions.placeholderOf(R.drawable.ic_image).error(R.drawable.ic_image))
                .thumbnail(Glide.with(mContext).load(item.content_thumbnail))
                .into(binding.performThumbnail)
        } else {
            binding.performThumbnail.setImageResource(R.drawable.ic_image)
        }

        binding.tvPerformTitle.text = item.content_title
        binding.tvTopicName.text = "Topic: $topicName"
        binding.tvPerformSubtitle.text = item.content_description

        contentId = contentWiseAnswerL.filter {
            it.content_id == item.content_id.toInt() && !it.isCorrectAnswer
        }

        try {
            if (item.question_list.isNotEmpty()) {
                if (item.Watch_Percentage.toInt() == 100) {
                    binding.tvProgressText.visibility = View.VISIBLE
                    binding.tvQuizstatus.visibility = View.VISIBLE
                } else {
                    binding.tvProgressText.visibility = View.GONE
                    binding.tvQuizstatus.visibility = View.GONE
                }


                if (item.CompletionStatus == true) {
                    binding.tvProgressText.setImageResource(R.drawable.quiz_done)
                    binding.tvQuizstatus.text = "Quiz Done"
                    if (Pref.ShowRetryIncorrectQuiz == true) {
                        binding.llRetryIncorrectQuizHeader.visibility = View.VISIBLE
                    }
                    binding.llRetryIncorrectQuizHeader.isEnabled = true

                    if (contentId?.size!! == 0) {
                        binding.llRetryIncorrectQuizHeader.visibility = View.GONE
                    }
                }
                else if (item.CompletionStatus == true && contentId?.size!! == 0) {
                    binding.llRetryIncorrectQuizHeader.visibility = View.GONE
                }
                else {
                    binding.tvProgressText.setImageResource(R.drawable.quiz_pending)
                    binding.tvQuizstatus.text = "Quiz Pending"
                    if (Pref.ShowRetryIncorrectQuiz == true) {
                        binding.llRetryIncorrectQuizHeader.visibility = View.VISIBLE
                    }
                    binding.llRetryIncorrectQuizHeader.isEnabled = false
                }
            } else {
                binding.tvProgressText.visibility = View.GONE
                binding.llRetryIncorrectQuizHeader.visibility = View.GONE
                binding.llRetryIncorrectQuizHeader.isEnabled = false
            }

            if (item.question_list.size == 0) {
                binding.llQuizHeader.visibility = View.GONE
                binding.llRetryIncorrectQuizHeader.visibility = View.GONE
                binding.llRetryIncorrectQuizHeader.isEnabled = false
            }
        } catch (e: Exception) {
            binding.tvProgressText.visibility = View.GONE
            binding.llRetryIncorrectQuizHeader.visibility = View.GONE
            binding.llRetryIncorrectQuizHeader.isEnabled = false
        }

        if (item.Watch_Percentage.isNotEmpty()) {
            binding.learningProgressStatus.progress = item.Watch_Percentage.toInt()
            if (item.Watch_Percentage == "100") {
                binding.tvProgressStatus.setImageResource(R.drawable.watch_done)
                binding.tvWatchstatus.text = "Watch Done"
            } else {
                binding.tvProgressStatus.setImageResource(R.drawable.watch_pending)
                binding.tvWatchstatus.text = "Watch Pending"
                binding.llRetryIncorrectQuizHeader.isEnabled = false
            }
        } else {
            binding.learningProgressStatus.progress = 0
            binding.tvProgressStatus.setImageResource(R.drawable.watch_pending)
            binding.tvWatchstatus.text = "Watch Pending"
        }

        binding.llContentRootPerform.setOnClickListener {
            listener.onItemClick(item, position)
        }

        if (!Pref.ShowRetryIncorrectQuiz) {
            binding.llRetryIncorrectQuizHeader.visibility = View.GONE
        }

        binding.llRetryIncorrectQuizHeader.setOnClickListener {
            listener.onRetryClick(item, position)
        }
    }

    override fun getItemCount(): Int = mList.size
}

package com.breezemobilearndemo

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.breezemobilearndemo.databinding.ItemIncorrectQuestionAnswerBinding


class RetryInCorrectQuestionAnswerAdapter(
    private val questions: List<Question_answer_fetch_list>,
    private val mContext: Context,
    private val listener: OnRetryClickListener,
    private val storeTopicName: String,
    private val contentName: String,
    private val contentThumbnail: String
) : RecyclerView.Adapter<RetryInCorrectQuestionAnswerAdapter.QuestionViewHolder>() {

    interface OnRetryClickListener {
        fun onRetryClicked(question: Question_answer_fetch_list)
    }

    inner class QuestionViewHolder(private val binding: ItemIncorrectQuestionAnswerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(question: Question_answer_fetch_list, position: Int) {
            binding.questionText.text = question.question
            binding.tvQstnNmbr.text = "${position + 1}."
            binding.tvQstnAnswr.text = question.answered

            binding.retryVideowatch.setOnClickListener {
                listener.onRetryClicked(question)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
        val binding = ItemIncorrectQuestionAnswerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return QuestionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
        holder.bind(questions[position], position)
    }

    override fun getItemCount(): Int = questions.size
}

package com.breezemobilearndemo

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.breezemobilearndemo.api.LMSRepoProvider
import com.breezemobilearndemo.databinding.FragmentMyTopicsWiseContentsBinding
import com.breezemobilearndemo.databinding.FragmentRetryIncorrectQuizBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class RetryIncorrectQuizFrag : Fragment() ,View.OnClickListener ,RetryInCorrectQuestionAnswerAdapter.OnRetryClickListener{
    private var binding : FragmentRetryIncorrectQuizBinding? = null
    private val retryIncorrectQuizFragView get() = binding!!
    private lateinit var mContext: Context
    private lateinit var adapter: RetryInCorrectQuestionAnswerAdapter

    companion object {
        var topic_id: String = ""
        var store_content_id: String = ""
        var store_topic_name: String = ""
        var previousFrag: String = ""
        var store_content_url: String = ""
        var content_name_: String = ""
        var content_thumbnail_: String = ""
        fun getInstance(objects: Any): RetryIncorrectQuizFrag {
            val retryIncorrectQuizFrag = RetryIncorrectQuizFrag()

            try {
                if (!TextUtils.isEmpty(objects.toString())) {
                    val parts = objects.toString().split("~")
                    topic_id = parts[0]
                    store_content_id = parts[1]
                    store_topic_name = parts[2]
                    store_content_url = parts[3]
                    content_name_ = parts[4]
                    content_thumbnail_ = parts[5]
                } else {
                    topic_id = ""
                    store_content_id = ""
                    store_topic_name = ""
                    store_content_url = ""
                    content_name_ = ""
                    content_thumbnail_ = ""
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return retryIncorrectQuizFrag
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext=context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentRetryIncorrectQuizBinding.inflate(inflater,container,false)
        return retryIncorrectQuizFragView.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        initView()
    }

    private fun initView() {
       retryIncorrectQuizFragView.tvIncorrectTab.setOnClickListener(this)
        retryIncorrectQuizFragView.topicName.setText("Topic : "+store_topic_name)
        retryIncorrectQuizFragView.contentName.setText(content_name_)
        retryIncorrectQuizFragView. rvIncorrectAnswerTab.layoutManager = LinearLayoutManager(mContext)


        if (store_content_url != null) {

            Glide.with(mContext)
                .load(content_thumbnail_)
                .apply(
                    RequestOptions.placeholderOf(R.drawable.ic_image).error(R.drawable.ic_image)
                )
                .thumbnail(
                    Glide.with(mContext).load(content_thumbnail_)
                )
                .into(retryIncorrectQuizFragView.contentThumbnail)
        } else {
            retryIncorrectQuizFragView.contentThumbnail.setImageResource(R.drawable.ic_image)
        }

        getTopicContentWiseAnswerListsAPICalling()

    }

    private fun getTopicContentWiseAnswerListsAPICalling() {

        try {
            retryIncorrectQuizFragView.progressWheel.spin()

            val repository = LMSRepoProvider.getTopicList()
            DashboardActivity.compositeDisposable.add(
                repository.getTopicContentWiseAnswerLists(
                    Pref.user_id!!,
                    topic_id,
                    store_content_id
                )
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ result ->
                        val response = result as TopicContentWiseAnswerListsFetchResponse
                        if (response.status == NetworkConstant.SUCCESS) {
                            retryIncorrectQuizFragView.progressWheel.stopSpinning()
                            try {
                                if (response.question_answer_fetch_list != null && response.question_answer_fetch_list.isNotEmpty()) {
                                    //val incorrectAnswers = response.question_answer_fetch_list.filter { !it.isCorrectAnswer }
                                    val correctAnswers = response.question_answer_fetch_list.filter { it.isCorrectAnswer }

                                    // Filter out the items where `isCorrectAnswer` is false and ensure uniqueness by `question_id` and `content_id`
                                    val incorrectAnswers = response.question_answer_fetch_list
                                        .filter { !it.isCorrectAnswer } // Filter out those that are not correct
                                        .distinctBy { it.question_id to it.content_id } // Ensure distinct items by question_id and content_id


                                    if (incorrectAnswers.size>0) {
                                        retryIncorrectQuizFragView.ivNoDataFoundRetry.visibility = View.GONE
                                        adapter = RetryInCorrectQuestionAnswerAdapter(incorrectAnswers ,mContext , this ,
                                            store_topic_name , content_name_ ,content_thumbnail_)
                                        retryIncorrectQuizFragView.rvIncorrectAnswerTab.adapter = adapter
                                    }else{
                                        retryIncorrectQuizFragView.ivNoDataFoundRetry.visibility = View.VISIBLE
                                    }

                                } else {
                                    retryIncorrectQuizFragView.progressWheel.stopSpinning()

                                    Toast.makeText(mContext, getString(R.string.no_data_found), Toast.LENGTH_SHORT).show()
                                }
                            } catch (ex: Exception) {
                                ex.printStackTrace()
                            }
                        } else {
                            retryIncorrectQuizFragView.progressWheel.stopSpinning()

                            Toast.makeText(mContext, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show()
                        }
                    }, { error ->
                        retryIncorrectQuizFragView.progressWheel.stopSpinning()

                        Toast.makeText(mContext, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show()
                    })
            )
        } catch (ex: Exception) {
            ex.printStackTrace()
            retryIncorrectQuizFragView.progressWheel.stopSpinning()

            Toast.makeText(mContext, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show()
        }
    }


    override fun onClick(v: View?) {


    }

    override fun onRetryClicked(question: Question_answer_fetch_list) {

        RetryPlayFrag.play_url = store_content_url
        RetryPlayFrag.question_id = question.question_id
        RetryPlayFrag.topic_id = question.topic_id
        RetryPlayFrag.content_id = question.content_id
        (mContext as DashboardActivity).loadFrag(RetryPlayFrag(),RetryPlayFrag::class.java.name, false)

    }
    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}
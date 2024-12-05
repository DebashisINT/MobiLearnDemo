package com.breezemobilearndemo

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.breezemobilearndemo.api.LMSRepoProvider
import com.breezemobilearndemo.databinding.FragmentRetryPlayBinding
import com.breezemobilearndemo.databinding.FragmentRetryQuestionBinding
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers


class RetryQuestionFrag : Fragment() {

    private var binding : FragmentRetryQuestionBinding? = null
    private val retryQuestionView get() = binding!!
    private lateinit var mContext: Context
    private var previouslySelectedOption: Int? = null
    private var selectedOption: Int = 0
    private var option_point: Int = 0
    private var isCorrect: Boolean = false
    private var option_number: String = ""


    companion object {
        var topic_id:Int=0
        var content_id:Int=0
        var question_id:Int=0
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext=context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentRetryQuestionBinding.inflate(inflater,container,false)
        return retryQuestionView.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE or WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        initView()
    }

    private fun initView() {

        questionWithAnswerViewAPICalling()

        retryQuestionView.cardOp1.setOnClickListener { selectOption(1) }
        retryQuestionView.cardOp2.setOnClickListener { selectOption(2) }
        retryQuestionView.cardOp3.setOnClickListener { selectOption(3) }
        retryQuestionView.cardOp4.setOnClickListener { selectOption(4) }
    }

    private fun selectOption(option: Int) {
        previouslySelectedOption?.let { prevOption ->
            when (prevOption) {
                1 -> {
                    if (retryQuestionView.cardOp1.isEnabled.not()) {
                        retryQuestionView.cardOp1.setCardBackgroundColor(Color.parseColor("#D3D3D3"))
                    } else {
                        retryQuestionView.cardOp1.setCardBackgroundColor(Color.parseColor("#DCF4EC"))
                    }
                }
                2 -> {
                    if (retryQuestionView.cardOp2.isEnabled.not()) {
                        retryQuestionView.cardOp2.setCardBackgroundColor(Color.parseColor("#D3D3D3"))
                    } else {
                        retryQuestionView.cardOp2.setCardBackgroundColor(Color.parseColor("#DCF4EC"))
                    }
                }
                3 -> {
                    if (retryQuestionView.cardOp3.isEnabled.not()) {
                        retryQuestionView.cardOp3.setCardBackgroundColor(Color.parseColor("#D3D3D3"))
                    } else {
                        retryQuestionView.cardOp3.setCardBackgroundColor(Color.parseColor("#DCF4EC"))
                    }
                }
                4 -> {
                    if (retryQuestionView.cardOp4.isEnabled.not()) {
                        retryQuestionView.cardOp4.setCardBackgroundColor(Color.parseColor("#D3D3D3"))
                    } else {
                        retryQuestionView.cardOp4.setCardBackgroundColor(Color.parseColor("#DCF4EC"))
                    }
                }
            }
        }

        selectedOption = option
        previouslySelectedOption = option

        when (option) {
            1 -> {
                retryQuestionView.cardOp1.setCardBackgroundColor(Color.parseColor("#ffa800"))
                option_number = retryQuestionView.tvRetryQaOp1.text.toString()
            }
            2 -> {
                retryQuestionView.cardOp2.setCardBackgroundColor(Color.parseColor("#ffa800"))
                option_number = retryQuestionView.tvRetryQaOp2.text.toString()
            }
            3 -> {
                retryQuestionView.cardOp3.setCardBackgroundColor(Color.parseColor("#ffa800"))
                option_number = retryQuestionView.tvRetryQaOp3.text.toString()
            }
            4 -> {
                retryQuestionView.cardOp4.setCardBackgroundColor(Color.parseColor("#ffa800"))
                option_number = retryQuestionView.tvRetryQaOp4.text.toString()
            }
        }
    }


    private fun topicContentWiseAnswerUpdateAPI(
        response: TopicContentWiseAnswerListsFetchResponse,
        question: String,
        questionId: Int,
        optionId: Int,
        option_point: Int,
        isCorrect: Boolean
    ) {
        try {
            retryQuestionView.progressWheel.spin()
            val repository = LMSRepoProvider.getTopicList()
            DashboardActivity.compositeDisposable.add(
                repository.getTopicContentWiseAnswerUpdate(
                    Pref.user_id!!,Pref.session_token!!, response.topic_id,response.topic_name,response.content_id,
                    questionId,
                    question,optionId,option_number,
                    option_point,
                    isCorrect
                )
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ result ->
                        val response = result as BaseResponse
                        if (response.status == NetworkConstant.SUCCESS) {
                            retryQuestionView.progressWheel.stopSpinning()
                            try {
                                (mContext as DashboardActivity).onBackPressed()
                                (mContext as DashboardActivity).onBackPressed()
                            } catch (ex: Exception) {
                                ex.printStackTrace()
                                Toast.makeText(mContext, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            retryQuestionView.progressWheel.stopSpinning()
                            Toast.makeText(mContext, getString(R.string.no_data_found), Toast.LENGTH_SHORT).show()

                        }
                    }, { error ->
                        retryQuestionView.progressWheel.stopSpinning()
                        Toast.makeText(mContext, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show()
                    })
            )
        } catch (ex: Exception) {
            ex.printStackTrace()
            retryQuestionView.progressWheel.stopSpinning()
            Toast.makeText(mContext, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show()
        }
    }
    private fun questionWithAnswerViewAPICalling() {

        try {
            retryQuestionView.progressWheel.spin()
            val repository = LMSRepoProvider.getTopicList()
            DashboardActivity.compositeDisposable.add(
                repository.getTopicContentWiseAnswerLists(
                    Pref.user_id!!,
                    topic_id.toString(),
                    content_id.toString()
                )
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ result ->
                        val response = result as TopicContentWiseAnswerListsFetchResponse
                        if (response.status == NetworkConstant.SUCCESS) {
                            retryQuestionView.progressWheel.stopSpinning()

                            try {
                                val filteredResponse =
                                    response.question_answer_fetch_list.filter { it.question_id == question_id }
                                retryQuestionView .tvRetryQaQuestion.setText(filteredResponse.get(0).question)
                                retryQuestionView.tvRetryQaOp1.setText(filteredResponse.get(0).option_list.get(0).option_no_1)
                                retryQuestionView.tvRetryQaOp2.setText(filteredResponse.get(0).option_list.get(0).option_no_2)
                                retryQuestionView.tvRetryQaOp3.setText(filteredResponse.get(0).option_list.get(0).option_no_3)
                                retryQuestionView.tvRetryQaOp4.setText(filteredResponse.get(0).option_list.get(0).option_no_4)

                                val options_list = Match_Option_list(filteredResponse.get(0).option_list.get(0).option_no_1, filteredResponse.get(0).option_list.get(0).option_no_2, filteredResponse.get(0).option_list.get(0).option_no_3, filteredResponse.get(0).option_list.get(0).option_no_4)

                                val answered = filteredResponse.get(0).answered
                                matchAnsweredOption(answered, options_list)

                                when (answered) {
                                    filteredResponse.get(0).option_list.get(0).option_no_1 -> {
                                        retryQuestionView.cardOp1.isEnabled = false
                                        retryQuestionView.cardOp1.setCardBackgroundColor(Color.parseColor("#e5e5e5"))
                                        retryQuestionView.ivImg1.setBackground(ContextCompat.getDrawable(mContext, R.drawable.letter_a));
                                        retryQuestionView.ivImg1.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#bdbdbd")));
                                        retryQuestionView.tvRetryQaOp1.setTextColor(Color.parseColor("#bababa"))

                                    }
                                    filteredResponse.get(0).option_list.get(0).option_no_2 -> {
                                        retryQuestionView.cardOp2.isEnabled = false
                                        retryQuestionView.cardOp2.setCardBackgroundColor(Color.parseColor("#e5e5e5"))
                                        retryQuestionView.ivImg2.setBackground(ContextCompat.getDrawable(mContext, R.drawable.letter_b));
                                        retryQuestionView.ivImg2.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#bdbdbd")));
                                        retryQuestionView.tvRetryQaOp2.setTextColor(Color.parseColor("#bababa"))
                                    }
                                    filteredResponse.get(0).option_list.get(0).option_no_3 -> {
                                        retryQuestionView.cardOp3.isEnabled = false
                                        retryQuestionView.cardOp3.setCardBackgroundColor(Color.parseColor("#e5e5e5"))
                                        retryQuestionView.ivImg3.setBackground(ContextCompat.getDrawable(mContext, R.drawable.letter_c));
                                        retryQuestionView.ivImg3.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#bdbdbd")));
                                        retryQuestionView.tvRetryQaOp3.setTextColor(Color.parseColor("#bababa"))

                                    }
                                    filteredResponse.get(0).option_list.get(0).option_no_4 -> {
                                        retryQuestionView.cardOp4.isEnabled = false
                                        retryQuestionView.cardOp4.setCardBackgroundColor(Color.parseColor("#e5e5e5"))
                                        retryQuestionView.ivImg4.setBackground(ContextCompat.getDrawable(mContext, R.drawable.letter_d));
                                        retryQuestionView.ivImg4.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#bdbdbd")));
                                        retryQuestionView.tvRetryQaOp4.setTextColor(Color.parseColor("#bababa"))

                                    }

                                    else -> {

                                    }
                                }

                                retryQuestionView.tvRetrySaveQstnAnswrSet.setOnClickListener {
                                    when (selectedOption) {
                                        1 -> {
                                            option_point = filteredResponse.get(0).option_list[0].option_point_1
                                            isCorrect = filteredResponse.get(0).option_list[0].isCorrect_1
                                        }
                                        2 -> {
                                            option_point = filteredResponse.get(0).option_list[0].option_point_2
                                            isCorrect = filteredResponse.get(0).option_list[0].isCorrect_2
                                        }
                                        3 -> {
                                            option_point = filteredResponse.get(0).option_list[0].option_point_3
                                            isCorrect = filteredResponse.get(0).option_list[0].isCorrect_3
                                        }
                                        4 -> {
                                            option_point = filteredResponse.get(0).option_list[0].option_point_4
                                            isCorrect = filteredResponse.get(0).option_list[0].isCorrect_4
                                        }
                                        else -> {
                                            return@setOnClickListener
                                        }
                                    }
                                    topicContentWiseAnswerUpdateAPI(response,filteredResponse.get(0).question,filteredResponse.get(0).question_id,filteredResponse.get(0).option_list.get(0).option_id,option_point,isCorrect
                                    )
                                }

                            } catch (ex: Exception) {
                                ex.printStackTrace()
                            }
                        } else {
                            retryQuestionView.progressWheel.stopSpinning()
                            Toast.makeText(mContext, getString(R.string.no_data_found), Toast.LENGTH_SHORT).show()
                        }
                    }, { error ->
                        retryQuestionView.progressWheel.stopSpinning()
                        Toast.makeText(mContext, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show()
                    })
            )
        } catch (ex: Exception) {
            ex.printStackTrace()
            retryQuestionView.progressWheel.stopSpinning()
            Toast.makeText(mContext, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show()
        }
    }

    fun matchAnsweredOption(answered: String, option: Match_Option_list) {
        val matchedOption = when (answered) {
            option.option_no_1 -> "Option 1: ${option.option_no_1}"
            option.option_no_2 -> "Option 2: ${option.option_no_2}"
            option.option_no_3 -> "Option 3: ${option.option_no_3}"
            option.option_no_4 -> "Option 4: ${option.option_no_4}"
            else -> "No match found"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}
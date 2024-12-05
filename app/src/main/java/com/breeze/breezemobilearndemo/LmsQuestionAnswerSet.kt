package com.breezemobilearndemo

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.airbnb.lottie.LottieAnimationView
import com.breeze.breezemobilearndemo.CustomStatic
import com.breezemobilearndemo.api.LMSRepoProvider
import com.breezemobilearndemo.databinding.FragmentLmsQuestionAnswerSetBinding
import com.bumptech.glide.Glide
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class LmsQuestionAnswerSet : Fragment() , View.OnClickListener{
    private var binding : FragmentLmsQuestionAnswerSetBinding? = null
    private val lmsQuestionView get() = binding!!
    private lateinit var mContext: Context

    private var questionSerialPosition = 0
    var finalL :ArrayList<QuestionL> = ArrayList()
    var correct_count = 0
    var incorrect_count = 0
    var total_points = 0
    var lastvideo:Boolean = VideoPlayLMS.lastvideo
    var question_answer_save_data :ArrayList<Question_Answer_Save_Data> = ArrayList()
    private var opSelection:Int=0
    private lateinit var popupWindow: PopupWindow

    companion object{

        var lastVideo:Boolean = false
        var topic_name:String = ""
        var question_submit:Boolean = false
        var question_submit_content_id:Int = 0
        var questionlist: ArrayList<QuestionL> = ArrayList()

        fun getInstance(objects: Any): LmsQuestionAnswerSet {
            val lmsQuestionAnswerSet = LmsQuestionAnswerSet()
            questionlist = ArrayList()
            if (!TextUtils.isEmpty(objects.toString())) {
                val parts = objects.toString().split("~")
                questionlist=objects as ArrayList<QuestionL>
            }else{
                questionlist = ArrayList()
            }
            return lmsQuestionAnswerSet
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
        binding = FragmentLmsQuestionAnswerSetBinding.inflate(inflater,container,false)
        return lmsQuestionView.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        initView()
    }

    private fun initView() {

        questionSerialPosition = 0

        Glide.with(mContext)
            .load(R.drawable.icon_pointer_gif)
            .into(lmsQuestionView.ivSaveQstnAnswrSetNext)
        lmsQuestionView.ivSaveQstnAnswrSetNext.setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_IN);

        finalL = ArrayList()
        for(i in 0..questionlist.size-1){
            if(questionlist.get(i).option_list.size>0){
                finalL.add(questionlist.get(i))
            }
        }

        var timeString : String = "00:00:30"

        val parts = timeString.split(":")
        val hours = parts[0].toInt()
        val minutes = parts[1].toInt()
        val seconds = parts[2].toInt()

        val milliseconds = (hours * 3600 + minutes * 60 + seconds) * 1000

        lmsQuestionView.tvSaveQstnAnswrSet.setOnClickListener {
            try {
                try {
                    var prevObj = finalL.get(questionSerialPosition-1)
                    var correct1 = prevObj.option_list.get(0).isCorrect_1
                    var correct2 = prevObj.option_list.get(0).isCorrect_2
                    var correct3 = prevObj.option_list.get(0).isCorrect_3
                    var correct4 = prevObj.option_list.get(0).isCorrect_4
                    var isCorrectAnsGiven = false

                    var points = 0
                    var correctAns=""
                    var answerGiven = ""
                    if(opSelection == 1){
                        answerGiven = prevObj.option_list.get(0).option_no_1
                    }else if(opSelection == 2){
                        answerGiven = prevObj.option_list.get(0).option_no_2
                    }else if(opSelection == 3){
                        answerGiven = prevObj.option_list.get(0).option_no_3
                    }else if(opSelection == 4){
                        answerGiven = prevObj.option_list.get(0).option_no_4
                    }

                    if(opSelection ==1 && correct1){
                        isCorrectAnsGiven = true
                        points = prevObj.option_list.get(0).option_point_1.toInt()
                        correctAns = prevObj.option_list.get(0).option_no_1
                        opSelection = 0
                    }else if(opSelection ==2 && correct2){
                        isCorrectAnsGiven = true
                        points = prevObj.option_list.get(0).option_point_2.toInt()
                        correctAns = prevObj.option_list.get(0).option_no_2
                        opSelection = 0
                    }else if(opSelection ==3 && correct3){
                        isCorrectAnsGiven = true
                        points = prevObj.option_list.get(0).option_point_3.toInt()
                        correctAns = prevObj.option_list.get(0).option_no_3
                        opSelection = 0
                    }else if(opSelection ==4 && correct4){
                        isCorrectAnsGiven = true
                        points = prevObj.option_list.get(0).option_point_4.toInt()
                        correctAns = prevObj.option_list.get(0).option_no_4
                        opSelection = 0
                    }
                    if(correct1){
                        correctAns = prevObj.option_list.get(0).option_no_1
                    }else if(correct2){
                        correctAns = prevObj.option_list.get(0).option_no_2
                    }else if(correct3){
                        correctAns = prevObj.option_list.get(0).option_no_3
                    }else if(correct4){
                        correctAns = prevObj.option_list.get(0).option_no_4
                    }
                    if(isCorrectAnsGiven) {
                        correct_count = correct_count + 1
                        Pref.correct_answer_count = (Pref.correct_answer_count + 1)

                        total_points=total_points+points
                        showPopup(points)
                    }
                    else {
                        incorrect_count = incorrect_count + 1
                        Pref.wrong_answer_count = (Pref.wrong_answer_count + 1)
                        showErrorPopup(correctAns, 0)
                    }

                    var obj = Question_Answer_Save_Data()
                    obj.topic_id = prevObj.topic_id.toInt()
                    obj.topic_name = topic_name
                    obj.content_id = prevObj.content_id.toInt()
                    obj.question_id = prevObj.question_id.toInt()
                    obj.question = prevObj.question
                    obj.option_id = prevObj.option_list.get(0).option_id.toInt()
                    obj.option_number = answerGiven//correctAns
                    obj.option_point = points
                    obj.isCorrect = isCorrectAnsGiven
                    obj.completionStatus = true

                    question_answer_save_data.add(obj)
                    var a =12

                } catch (e: Exception) {
                    e.printStackTrace()
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
            return@setOnClickListener

        }

        lmsQuestionView.tvQaOp1.setOnClickListener(this)
        lmsQuestionView.tvQaOp2.setOnClickListener(this)
        lmsQuestionView.tvQaOp3.setOnClickListener(this)
        lmsQuestionView.tvQaOp4.setOnClickListener(this)
        lmsQuestionView.tvSaveQstnAnswrSet.performClick()

        processloadQuestionAns()
        lmsQuestionView.tvSaveQstnAnswrSet.visibility = View.GONE
    }

    private fun showPopup( pointsListval: Int) {
        val inflater: LayoutInflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView: View = inflater.inflate(R.layout.popup_layout_correct_ans, null)
        popupWindow = PopupWindow(
            popupView,
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT,
            true
        )
        val close_button: TextView = popupView.findViewById(R.id.close_button)
        val popup_image: LottieAnimationView = popupView.findViewById(R.id.popup_image)
        val popup_title: TextView = popupView.findViewById(R.id.popup_title)
        val popup_message: TextView = popupView.findViewById(R.id.popup_message)
        popup_title.setText("Congratulation "/*+Pref.user_name*/)
        var typeFace: Typeface? = ResourcesCompat.getFont(requireContext(), R.font.remachinescript_personal_use)
        popup_title.setTypeface(typeFace)
        popup_message.setText("You get $pointsListval points")

        popup_title.visibility = View.GONE
        popup_message.setText("+$pointsListval")

        val a: Animation = AnimationUtils.loadAnimation(mContext, R.anim.scale)
        a.reset()
        popup_message.clearAnimation()
        popup_message.startAnimation(a)

        close_button.setOnClickListener {

            processloadQuestionAns()
            lmsQuestionView.progressWheel.spin()
            Handler().postDelayed(Runnable {
                lmsQuestionView.progressWheel.spin()

                popupWindow.dismiss()
            }, 400)

        }
        popup_image.visibility =View.VISIBLE
        popupWindow.setBackgroundDrawable(ColorDrawable())
        popupWindow.isOutsideTouchable = false
        popupWindow.isFocusable = false
        popupWindow.showAtLocation(lmsQuestionView.llParentQuestionAnswer, Gravity.CENTER, 0, 0)

    }

    private fun showErrorPopup( correctAns:String,pointsListval: Int) {
        val inflater: LayoutInflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView: View = inflater.inflate(R.layout.error_popup_layout_congratulation, null)
        popupWindow = PopupWindow(
            popupView,
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT,
            true
        )
        val close_button: TextView = popupView.findViewById(R.id.close_button)
        val popup_image: LottieAnimationView = popupView.findViewById(R.id.popup_image)
        val popup_title: TextView = popupView.findViewById(R.id.popup_title)
        val popup_message: TextView = popupView.findViewById(R.id.popup_message)
        val popup_message_ans: TextView = popupView.findViewById(R.id.popup_message_ans)
        var typeFace: Typeface? = ResourcesCompat.getFont(requireContext(), R.font.remachinescript_personal_use)
        popup_title.setTypeface(typeFace)

        popup_message_ans.text = "Correct answer is : "+correctAns
        close_button.setOnClickListener {
            processloadQuestionAns()
            lmsQuestionView.progressWheel.spin()
            Handler().postDelayed(Runnable {
                lmsQuestionView.progressWheel.stopSpinning()
                popupWindow.dismiss()
            }, 400)
        }
        popup_image.visibility =View.VISIBLE
        popupWindow.setBackgroundDrawable(ColorDrawable())
        popupWindow.isOutsideTouchable = false
        popupWindow.isFocusable = false
        popupWindow.showAtLocation(lmsQuestionView.llParentQuestionAnswer, Gravity.CENTER, 0, 0)
    }


    fun processloadQuestionAns(){
        lmsQuestionView.tvSaveQstnAnswrSet.visibility = View.GONE
        try {
            var question = finalL.get(questionSerialPosition).question
            var option1 = finalL.get(questionSerialPosition).option_list.get(0).option_no_1
            var option2 = finalL.get(questionSerialPosition).option_list.get(0).option_no_2
            var option3 = finalL.get(questionSerialPosition).option_list.get(0).option_no_3
            var option4 = finalL.get(questionSerialPosition).option_list.get(0).option_no_4

            questionSerialPosition++

            if(questionSerialPosition==1)
                lmsQuestionView.ivFragLmsQuesBack.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.img_quest))
            else if(questionSerialPosition==2)
                lmsQuestionView.ivFragLmsQuesBack.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.img_quest1))
            else if(questionSerialPosition==3)
                lmsQuestionView.ivFragLmsQuesBack.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.img_quest2))
            else if(questionSerialPosition==4)
                lmsQuestionView.ivFragLmsQuesBack.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.img_quest3))
            else
                lmsQuestionView.ivFragLmsQuesBack.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.img_quest))

            //(mContext as DashboardActivity).setTopBarTitle("Quiz : "+questionSerialPosition+"/"+finalL.size)

            loadQuestionAns(question,option1,option2,option3,option4,if(questionSerialPosition==finalL.size) true else false)
            lmsQuestionView.progressWheel.spin()

        } catch (e: Exception) {
            e.printStackTrace()
            summury_popup()
            saveQAAPICalling()
        }
    }

    fun loadQuestionAns(question:String,op1:String,op2:String,op3:String,op4:String,isEnd:Boolean){
        try {
            if(isEnd){
                lmsQuestionView.tvSaveQstnAnswrSetText.text = "Submit"
                lmsQuestionView.ivSaveQstnAnswrSetNext.visibility = View.GONE
                question_submit = true
                val question_submit_content_id = questionlist[0].content_id.toInt()
                saveContentId(mContext, question_submit_content_id)

            }else{
                lmsQuestionView.tvSaveQstnAnswrSetText.text = "Submit"
                lmsQuestionView.ivSaveQstnAnswrSetNext.visibility = View.VISIBLE
                question_submit = false
                question_submit_content_id=0
            }

            lmsQuestionView.cardOp1.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.color_option))
            lmsQuestionView.cardOp2.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.color_option))
            lmsQuestionView.cardOp3.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.color_option))
            lmsQuestionView.cardOp4.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.color_option))

            lmsQuestionView.tvQaQuestion.text = question
            lmsQuestionView.tvQaOp1.text = op1
            lmsQuestionView.tvQaOp2.text = op2
            lmsQuestionView.tvQaOp3.text = op3
            lmsQuestionView.tvQaOp4.text = op4

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveContentId(mContext: Context, question_submit_content_id: Int) {

        val existingContentIds = getArrayFromSharedPreferences(mContext, "saved_content_ids").toMutableList()

        val newContentId = question_submit_content_id

        if (!existingContentIds.contains(newContentId)) {
            existingContentIds.add(newContentId)
        }

        saveArrayToSharedPreferences(mContext, "saved_content_ids", existingContentIds)
    }

    fun saveArrayToSharedPreferences(context: Context, key: String, values: List<Int>) {
        val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val valueString = values.joinToString(",")
        editor.putString(key, valueString)
        editor.apply()
    }

    fun getArrayFromSharedPreferences(context: Context, key: String): List<Int> {
        val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val valueString = sharedPreferences.getString(key, "")
        return if (valueString.isNullOrEmpty()) {
            emptyList()
        } else {
            valueString.split(",").map { it.toInt() }
        }
    }

    private fun summury_popup() {

        val inflater: LayoutInflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView: View = inflater.inflate(R.layout.popup_layout_summary, null)
        var popupWindowSummary = PopupWindow(
            popupView,
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT,
            true
        )
        val close_button: TextView = popupView.findViewById(R.id.close_button)
        val popup_image: LottieAnimationView = popupView.findViewById(R.id.popup_image)
        val popup_title: TextView = popupView.findViewById(R.id.popup_title)
        val tv_total_no_qstn: TextView = popupView.findViewById(R.id.tv_total_no_qstn)
        tv_total_no_qstn.text = "Total number of question : "+finalL.size
        val tv_total_no_crrct: TextView = popupView.findViewById(R.id.tv_total_no_crrct)
        tv_total_no_crrct.text = "Total number of correct answer : "+correct_count
        val tv_total_no_incrrct: TextView = popupView.findViewById(R.id.tv_total_no_incrrct)
        tv_total_no_incrrct.text = "Total number of incorrect answer : "+incorrect_count
        val tv_total_points: TextView = popupView.findViewById(R.id.tv_total_points)
        tv_total_points.text = "You get total points : "+total_points

        var typeFace: Typeface? = ResourcesCompat.getFont(mContext, R.font.remachinescript_personal_use)
        popup_title.setTypeface(typeFace)

        close_button.setOnClickListener {

            popupWindowSummary.dismiss()

        }
        popupWindowSummary.setOnDismissListener {
            try {

            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (lastvideo==true){
                CustomStatic.IsHomeClick = false
                (mContext as DashboardActivity).loadFrag(SearchLmsFrag(),SearchLmsFrag::class.java.name, false)
            }else {
                (mContext as DashboardActivity).onBackPressed()
            }
        }
        popup_image.visibility =View.VISIBLE
        popupWindowSummary.setBackgroundDrawable(ColorDrawable())
        popupWindowSummary.isOutsideTouchable = false
        popupWindowSummary.isFocusable = false
        popupWindowSummary.showAtLocation(lmsQuestionView.llParentQuestionAnswer, Gravity.CENTER, 0, 0)

    }

    private fun saveQAAPICalling() {
        try {
            var content_wise_QA_save = CONTENT_WISE_QA_SAVE()
            content_wise_QA_save.user_id = Pref.user_id!!
            content_wise_QA_save.question_answer_save_list = question_answer_save_data

            val repository = LMSRepoProvider.getTopicList()
            DashboardActivity.compositeDisposable.add(
                repository.saveContentWiseQAApi(content_wise_QA_save)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ result ->
                        val response = result as BaseResponse
                        try {
                            if (response.status == NetworkConstant.SUCCESS) {
                                excute()
                            }else{

                            }
                        } catch (e: Exception) {

                        }
                    }, { error ->
                        Toast.makeText(mContext, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show()
                    })
            )
        } catch (ex: Exception) {
            ex.printStackTrace()
            Toast.makeText(mContext, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show()
        }
    }

    fun excute(){
        try {
            val repository = LMSRepoProvider.getTopicList()
            DashboardActivity.compositeDisposable.add(
                repository.getTopicsWiseVideo(Pref.user_id!!, VideoPlayLMS.topic_id)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ result ->
                        var response = result as VideoTopicWiseResponse
                        if (response.status == NetworkConstant.SUCCESS) {
                            try {
                                if (response.content_list != null && response.content_list.size > 0) {
                                    var temp  = response.content_list.distinctBy { it.content_play_sequence.toString() }
                                    var contentL = temp as ArrayList<ContentL>
                                    val sortedList = contentL.sortedBy { it.content_play_sequence.toInt() }.toCollection(ArrayList())
                                    VideoPlayLMS.sequenceQuestionL = ArrayList()
                                    try {
                                        for (i in 0.. sortedList.size-1){
                                            var rootObj : SequenceQuestion = SequenceQuestion()
                                            rootObj.index = i+1
                                            rootObj.completionStatus = sortedList.get(i).CompletionStatus
                                            rootObj.question_list = sortedList.get(i).question_list
                                            VideoPlayLMS.sequenceQuestionL.add(rootObj)
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        VideoPlayLMS.sequenceQuestionL = ArrayList()
                                    }
                                } else {

                                }
                            } catch (ex: Exception) {
                                ex.printStackTrace()
                            }
                        } else {
                        }
                    }, { error ->
                        error.printStackTrace()
                    })
            )
        }
        catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    override fun onClick(v: View?) {

        when (v?.id) {
            lmsQuestionView.tvQaOp1.id ->{
                opSelection=1
                lmsQuestionView.tvSaveQstnAnswrSet.visibility = View.VISIBLE

                lmsQuestionView.cardOp1.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.tfe_color_primary))
                lmsQuestionView.cardOp2.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.color_option))
                lmsQuestionView.cardOp3.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.color_option))
                lmsQuestionView.cardOp4.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.color_option))

                Glide.with(mContext)
                    .load(R.drawable.icon_pointer_gif)
                    .into(lmsQuestionView.ivSaveQstnAnswrSetNext)
                lmsQuestionView.ivSaveQstnAnswrSetNext.setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_IN);
            }
            lmsQuestionView.tvQaOp2.id ->{
                opSelection=2
                lmsQuestionView.tvSaveQstnAnswrSet.visibility = View.VISIBLE
                lmsQuestionView.cardOp1.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.color_option))
                lmsQuestionView.cardOp2.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.tfe_color_primary))
                lmsQuestionView.cardOp3.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.color_option))
                lmsQuestionView.cardOp4.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.color_option))

                Glide.with(mContext)
                    .load(R.drawable.icon_pointer_gif)
                    .into(lmsQuestionView.ivSaveQstnAnswrSetNext)
                lmsQuestionView.ivSaveQstnAnswrSetNext.setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_IN);
            }
            lmsQuestionView.tvQaOp3.id ->{
                opSelection=3
                lmsQuestionView.tvSaveQstnAnswrSet.visibility = View.VISIBLE
                lmsQuestionView.cardOp1.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.color_option))
                lmsQuestionView.cardOp2.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.color_option))
                lmsQuestionView.cardOp3.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.tfe_color_primary))
                lmsQuestionView.cardOp4.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.color_option))

                Glide.with(mContext)
                    .load(R.drawable.icon_pointer_gif)
                    .into(lmsQuestionView.ivSaveQstnAnswrSetNext)
                lmsQuestionView.ivSaveQstnAnswrSetNext.setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_IN);
            }
            lmsQuestionView.tvQaOp4.id ->{
                opSelection=4
                lmsQuestionView.tvSaveQstnAnswrSet.visibility = View.VISIBLE

                lmsQuestionView.cardOp1.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.color_option))
                lmsQuestionView.cardOp2.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.color_option))
                lmsQuestionView.cardOp3.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.color_option))
                lmsQuestionView.cardOp4.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.tfe_color_primary))

                Glide.with(mContext)
                    .load(R.drawable.icon_pointer_gif)
                    .into(lmsQuestionView.ivSaveQstnAnswrSetNext)
                lmsQuestionView.ivSaveQstnAnswrSetNext.setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_IN);
            }
        }
    }

}
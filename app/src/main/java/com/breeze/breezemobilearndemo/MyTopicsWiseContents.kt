package com.breezemobilearndemo

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.breeze.breezemobilearndemo.CustomStatic
import com.breezemobilearndemo.api.LMSRepoProvider
import com.breezemobilearndemo.databinding.FragmentMyTopicsWiseContentsBinding
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MyTopicsWiseContents : Fragment() , OnClickListener {
    private var binding : FragmentMyTopicsWiseContentsBinding? = null
    private val myTopicewiseContentView get() = binding!!
    private lateinit var mContext: Context
    var contentL: ArrayList<ContentL> = ArrayList()
    var content_idL: ArrayList<String> = ArrayList()
    var content_idListSize :Int =0
    var contentWiseAnswerL : ArrayList<ContentWiseAnswerL> =ArrayList()
    private var  suffixText:String = ""

    companion object {
        var topic_id: String = ""
        var topic_name: String = ""
        var previousFrag: String = ""
        fun getInstance(objects: Any): MyTopicsWiseContents {
            val myTopicsWiseContents = MyTopicsWiseContents()

            if (!TextUtils.isEmpty(objects.toString())) {
                val parts = objects.toString().split("~")
                if (parts.size ==2) {
                    topic_id = parts[0]
                    topic_name = parts[1]
                }else if (parts.size == 5){
                    topic_id = parts[0]
                    topic_name = parts[2]
                }
            } else {
                topic_id = ""
                topic_name = ""
            }
            return myTopicsWiseContents
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
        binding = FragmentMyTopicsWiseContentsBinding.inflate(inflater,container,false)
        return myTopicewiseContentView.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        initView()
    }

    private fun initView() {
        myTopicewiseContentView.bottomLayoutLms.ivLmsPerformance.setImageResource(R.drawable.performance_insights)
        myTopicewiseContentView.bottomLayoutLms.ivLmsMylearning .setImageResource(R.drawable.my_topics_selected)
        myTopicewiseContentView.bottomLayoutLms.ivLmsKnowledgehub .setImageResource(R.drawable.set_of_books_lms)

        myTopicewiseContentView.bottomLayoutLms.tvLmsPerformance .setTextColor(getResources().getColor(R.color.black))
        myTopicewiseContentView.bottomLayoutLms.tvLmsMylearning .setTextColor(getResources().getColor(R.color.toolbar_lms))
        myTopicewiseContentView.bottomLayoutLms.tvLmsKnowledgehub .setTextColor(getResources().getColor(R.color.black))

        getMyLarningInfoAPI()

        myTopicewiseContentView.etFragLearningSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            @SuppressLint("SuspiciousIndentation")
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                var tempSearchL = contentL.filter { it.content_title.contains(myTopicewiseContentView.etFragLearningSearch.text.toString().trim(), ignoreCase = true) || it.content_description.contains(myTopicewiseContentView.etFragLearningSearch.text.toString().trim(), ignoreCase = true)  } as ArrayList<ContentL>
                myTopicewiseContentView.progressWheel.spin()
                if(tempSearchL.size>0){
                    myTopicewiseContentView.progressWheel.stopSpinning()
                    myTopicewiseContentView.rvMylearningProgress.visibility =View.VISIBLE
                    myTopicewiseContentView.llContinueLearning.visibility =View.VISIBLE
                    myTopicewiseContentView.llNoData.visibility =View.GONE
                    setLearningData(
                        tempSearchL,
                        topic_name,
                        contentWiseAnswerL)
                }else{
                    myTopicewiseContentView.progressWheel.stopSpinning()
                    myTopicewiseContentView.rvMylearningProgress.visibility =View.GONE
                    myTopicewiseContentView.llContinueLearning.visibility =View.GONE
                    myTopicewiseContentView.llNoData.visibility =View.VISIBLE
                }
            }
            override fun afterTextChanged(s: Editable) {
                myTopicewiseContentView.progressWheel.stopSpinning()
            }
        })

        myTopicewiseContentView.bottomLayoutLms.llLmsPerformance.setOnClickListener(this)
        myTopicewiseContentView.bottomLayoutLms.llLmsMylearning.setOnClickListener(this)
        myTopicewiseContentView.bottomLayoutLms.llLmsKnowledgehub.setOnClickListener(this)
        myTopicewiseContentView. ivFragSpk.setOnClickListener(this)
        myTopicewiseContentView.llFragSearchRoot.setOnClickListener(this)


    }

    private fun getMyLarningInfoAPI() {
        try {
            if(topic_id.equals("")){
                topic_id = CustomStatic.TOPIC_SEL
            }
            myTopicewiseContentView.progressWheel.spin()
            val repository = LMSRepoProvider.getTopicList()
            DashboardActivity.compositeDisposable.add(
                repository.getTopicsWiseVideo(Pref.user_id!!, topic_id)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ result ->
                        val response = result as VideoTopicWiseResponse
                        if (response.status == NetworkConstant.SUCCESS) {
                            myTopicewiseContentView.progressWheel.stopSpinning()
                            try {
                                if (response.content_list != null && response.content_list.size > 0) {
                                    var temp = response.content_list.distinctBy { it.content_id.toString() }
                                    contentL = temp as  ArrayList<ContentL>
                                    content_idL = ArrayList()
                                    content_idL = contentL.map { it.content_id.toString() } as ArrayList<String>
                                    content_idListSize = contentL.size

                                    process()

                                } else {
                                    Toast.makeText(mContext, "No video found", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            } catch (ex: Exception) {
                                ex.printStackTrace()
                            }
                        } else {
                            myTopicewiseContentView.progressWheel.stopSpinning()
                        }
                    }, { error ->
                        myTopicewiseContentView.progressWheel.stopSpinning()
                        Toast.makeText(mContext, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT)
                            .show()
                    })
            )
        } catch (ex: Exception) {
            ex.printStackTrace()
            myTopicewiseContentView.progressWheel.stopSpinning()
            Toast.makeText(mContext, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun process(){
        getTopicContentWiseAnswerListsAPICalling()
    }

    private fun getTopicContentWiseAnswerListsAPICalling(
    ) {
        if (content_idListSize>0){
            try {
                myTopicewiseContentView.progressWheel.spin()
                val repository = LMSRepoProvider.getTopicList()
                DashboardActivity.compositeDisposable.add(
                    repository.getTopicContentWiseAnswerLists(Pref.user_id!!, topic_id , content_idL.get(content_idListSize-1))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({ result ->
                            val response = result as TopicContentWiseAnswerListsFetchResponse
                            if (response.status == NetworkConstant.SUCCESS) {
                                myTopicewiseContentView.progressWheel.stopSpinning()

                                try {
                                    var content_Id = response.content_id
                                    for (i in 0.. response.question_answer_fetch_list.size-1){
                                        contentWiseAnswerL.add(ContentWiseAnswerL(content_Id,response.question_answer_fetch_list.get(i).isCorrectAnswer))
                                    }

                                } catch (ex: Exception) {
                                    ex.printStackTrace()
                                }
                                content_idListSize--
                                getTopicContentWiseAnswerListsAPICalling()

                            } else {
                                myTopicewiseContentView.progressWheel.stopSpinning()
                                Toast.makeText(mContext, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show()

                            }
                        }, { error ->
                            myTopicewiseContentView.progressWheel.stopSpinning()
                            Toast.makeText(mContext, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show()
                        })
                )
            } catch (ex: Exception) {
                ex.printStackTrace()
                myTopicewiseContentView.progressWheel.stopSpinning()
                Toast.makeText(mContext, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show()
            }
        }
        else{
            val sortedList = contentL.sortedBy { it.content_play_sequence.toInt() }.toCollection(ArrayList())
            setLearningData(sortedList ,topic_name , contentWiseAnswerL)
        }
    }

    private fun setLearningData(
        finalDatal: ArrayList<ContentL>,
        topic_name: String,
        contentWiseAnswerL: ArrayList<ContentWiseAnswerL>
    ) {
        myTopicewiseContentView.rvMylearningProgress.layoutManager = LinearLayoutManager(mContext)

        val adapter = MyLearningProgressAdapter(
            mContext, finalDatal, topic_name,
            listener = object : MyLearningProgressAdapter.OnItemClickListener {
                override fun onItemClick(item: ContentL, position: Int) {
                    val store_topic_id = topic_id
                    val store_topic_name = topic_name
                    val store_content_id = item.content_id
                    VideoPlayLMS.loadedFrom = "MyTopicsWiseContents"
                    CustomStatic.VideoPosition = position
                    Pref.videoCompleteCount = "0"
                    (mContext as DashboardActivity).loadFrag(VideoPlayLMS.getInstance(topic_id +"~"+ topic_name), VideoPlayLMS::class.java.name,isAdd = true)
                }

                override fun onRetryClick(item: ContentL, position: Int) {
                    val store_content_id = item.content_id
                    VideoPlayLMS.loadedFrom = "MyTopicsWiseContents"
                    CustomStatic.VideoPosition = position
                    CustomStatic.RetryTopicId = topic_id.toInt()
                    CustomStatic. RetryTopicName= topic_name
                    CustomStatic.RetryContentId = store_content_id.toInt()
                    CustomStatic.RetryContentName = item.content_title
                    CustomStatic.RetryContentURL = item.content_url
                    (mContext as DashboardActivity).loadFrag(RetryIncorrectQuizFrag.getInstance(topic_id +"~"+ store_content_id +"~"+topic_name + "~"+item.content_url +"~"+item.content_title+"~"+item.content_thumbnail),RetryIncorrectQuizFrag::class.java.name, true)

                }
            },
            contentWiseAnswerL
        )
        myTopicewiseContentView.rvMylearningProgress.adapter = adapter
        myTopicewiseContentView.rvMylearningProgress.visibility =View.VISIBLE

    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            myTopicewiseContentView.bottomLayoutLms.llLmsMylearning.id -> {
                (mContext as DashboardActivity).loadFrag(SearchLmsFrag(),SearchLmsFrag::class.java.name)
            }
            myTopicewiseContentView.bottomLayoutLms.llLmsKnowledgehub.id -> {
                (mContext as DashboardActivity).loadFrag(SearchLmsKnowledgeFrag(),SearchLmsKnowledgeFrag::class.java.name)
            }
            myTopicewiseContentView.bottomLayoutLms.llLmsPerformance.id -> {
                (mContext as DashboardActivity).loadFrag(PerformanceInsightPage(),PerformanceInsightPage::class.java.name)
            }
            myTopicewiseContentView.llFragSearchRoot.id -> {
                AppUtils.hideSoftKeyboard(mContext as DashboardActivity)
                myTopicewiseContentView.progressWheel.spin()
                GlobalScope.launch(Dispatchers.IO) {
                    var tempSearchL = contentL.filter { it.content_title.contains(myTopicewiseContentView.etFragLearningSearch.text.toString().trim(), ignoreCase = true) || it.content_description.contains(myTopicewiseContentView.etFragLearningSearch.text.toString().trim(), ignoreCase = true)  } as ArrayList<ContentL>
                    withContext(Dispatchers.Main) {
                        myTopicewiseContentView.progressWheel.stopSpinning()
                        if(tempSearchL.size>0){
                            myTopicewiseContentView.rvMylearningProgress.visibility =View.VISIBLE
                            myTopicewiseContentView.llContinueLearning.visibility =View.VISIBLE
                            myTopicewiseContentView.llNoData.visibility =View.GONE
                            setLearningData(
                                tempSearchL,
                                topic_name,
                                contentWiseAnswerL)
                        }else{
                            myTopicewiseContentView.rvMylearningProgress.visibility =View.GONE
                            myTopicewiseContentView.llContinueLearning.visibility =View.GONE
                            myTopicewiseContentView.llNoData.visibility =View.VISIBLE
                        }
                    }
                }
            }
            myTopicewiseContentView.ivFragSpk.id ->{
                suffixText = myTopicewiseContentView.etFragLearningSearch .text.toString().trim()
                startVoiceInput()
            }
        }
    }

    fun updateList(){
        getMyLarningInfoAPI()
    }

    private fun startVoiceInput() {
        val intent: Intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"en-US")
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US")

        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hello, How can I help you?")
        try {
            startActivityForResult(intent, 7009)
        } catch (a: ActivityNotFoundException) {
            a.printStackTrace()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?){
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 7009){
            try{
                val result = data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                var t= result!![0]
                if(suffixText.length>0 && !suffixText.equals("")){
                    var setFullText = suffixText+t
                    myTopicewiseContentView.etFragLearningSearch .setText(suffixText+t)
                    myTopicewiseContentView.etFragLearningSearch.setSelection(setFullText.length);
                }else{
                    var SuffixPostText = t+myTopicewiseContentView.etFragLearningSearch.text.toString()
                    myTopicewiseContentView.etFragLearningSearch.setText(SuffixPostText)
                    myTopicewiseContentView.etFragLearningSearch.setSelection(SuffixPostText.length);
                }
            }
            catch (ex:Exception) {
                ex.printStackTrace()
            }
        }
    }
}
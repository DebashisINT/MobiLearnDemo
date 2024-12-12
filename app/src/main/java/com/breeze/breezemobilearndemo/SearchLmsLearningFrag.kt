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
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.breeze.breezemobilearndemo.CustomStatic
import com.breezemobilearndemo.api.LMSRepoProvider
import com.breezemobilearndemo.databinding.FragmentSearchLmsLearningBinding
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class SearchLmsLearningFrag : Fragment(), View.OnClickListener{

    var binding : FragmentSearchLmsLearningBinding? = null
    val myLearningwiseContentView get() = binding!!
    lateinit var mContext: Context
    var contentL: ArrayList<ContentL> = ArrayList()
    var contentWiseAnswerL : ArrayList<ContentWiseAnswerL> =ArrayList()
    var content_idL: ArrayList<String> = ArrayList()
    var  suffixText:String = ""
    var content_idListSize :Int =0

    companion object {
        var topic_id: String = ""
        var topic_name: String = ""
        fun getInstance(objects: Any): SearchLmsLearningFrag {
            val searchLmsLearningFrag = SearchLmsLearningFrag()

            if (!TextUtils.isEmpty(objects.toString())) {
                val parts = objects.toString().split("~")
                topic_id = parts[0]
                topic_name = parts[1]
            } else {
                topic_id = ""
                topic_name = ""
            }

            return searchLmsLearningFrag
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
        binding = FragmentSearchLmsLearningBinding.inflate(inflater,container,false)
        return myLearningwiseContentView.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        initView()
    }

    fun initView() {
        myLearningwiseContentView.includeBottomTabLms.ivLmsPerformance.setImageResource(R.drawable.performance_insights_checked)
        myLearningwiseContentView.includeBottomTabLms.ivLmsMylearning.setImageResource(R.drawable.open_book_lms_)
        myLearningwiseContentView.includeBottomTabLms.ivLmsKnowledgehub.setImageResource(R.drawable.set_of_books_lms)

        myLearningwiseContentView.includeBottomTabLms.tvLmsPerformance.setTextColor(getResources().getColor(R.color.toolbar_lms))
        myLearningwiseContentView.includeBottomTabLms.tvLmsMylearning.setTextColor(getResources().getColor(R.color.black))
        myLearningwiseContentView.includeBottomTabLms.tvLmsKnowledgehub.setTextColor(getResources().getColor(R.color.black))

        myLearningwiseContentView.llFragMyLearningProgressWheel .stopSpinning()
        getMyLarningInfoAPI()
        myLearningwiseContentView.etFragLearningSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            @SuppressLint("SuspiciousIndentation")
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                //setLearningData(final_dataL)
                var tempSearchL = contentL.filter { it.content_title.contains(myLearningwiseContentView.etFragLearningSearch.text.toString().trim(), ignoreCase = true) || it.content_description.contains(myLearningwiseContentView.etFragLearningSearch.text.toString().trim(), ignoreCase = true)  } as ArrayList<ContentL>
                myLearningwiseContentView.llFragMyLearningProgressWheel.stopSpinning()
                if(tempSearchL.size>0){
                    myLearningwiseContentView.rvMylearningProgress.visibility =View.VISIBLE
                    myLearningwiseContentView.llContinueLearning.visibility =View.VISIBLE
                    myLearningwiseContentView.llNoData.visibility =View.GONE
                    setLearningData(tempSearchL, topic_name, contentWiseAnswerL)
                }else{
                    myLearningwiseContentView.rvMylearningProgress.visibility =View.GONE
                    myLearningwiseContentView.llContinueLearning.visibility =View.GONE
                    myLearningwiseContentView.llNoData.visibility =View.VISIBLE
                }
            }
            override fun afterTextChanged(s: Editable) {}
        })
        myLearningwiseContentView.includeBottomTabLms.llLmsPerformance.setOnClickListener(this)
        myLearningwiseContentView.includeBottomTabLms.llLmsMylearning.setOnClickListener(this)
        myLearningwiseContentView.includeBottomTabLms.llLmsKnowledgehub.setOnClickListener(this)
        myLearningwiseContentView.ivFragSpk.setOnClickListener(this)
        myLearningwiseContentView.llFragSearchRoot.setOnClickListener(this)

        val sortedList = contentL.sortedBy { it.content_play_sequence.toInt() }.toCollection(ArrayList())
        setLearningData(sortedList, MyTopicsWiseContents.topic_name, contentWiseAnswerL)


    }

    fun getMyLarningInfoAPI() {
        try {
            if(topic_id.equals("")){
                topic_id = CustomStatic.TOPIC_SEL
            }
            myLearningwiseContentView.llFragMyLearningProgressWheel.visibility = View.VISIBLE
            val repository = LMSRepoProvider.getTopicList()
            DashboardActivity.compositeDisposable.add(
                repository.getTopicsWiseVideo(Pref.user_id!!, topic_id)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ result ->
                        val response = result as VideoTopicWiseResponse
                        if (response.status == NetworkConstant.SUCCESS) {
                            myLearningwiseContentView.llFragMyLearningProgressWheel.visibility = View.GONE
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
                            myLearningwiseContentView.llFragMyLearningProgressWheel.visibility = View.GONE
                            Toast.makeText(mContext, getString(R.string.no_data_found), Toast.LENGTH_SHORT).show()

                        }
                    }, { error ->
                        myLearningwiseContentView.llFragMyLearningProgressWheel.visibility = View.GONE
                        Toast.makeText(mContext, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show()
                    })
            )
        } catch (ex: Exception) {
            ex.printStackTrace()
            myLearningwiseContentView.llFragMyLearningProgressWheel.visibility = View.GONE
            Toast.makeText(mContext, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show()
        }
    }

    fun process() {
        getTopicContentWiseAnswerListsAPICalling()
    }

    fun getTopicContentWiseAnswerListsAPICalling(
    ) {

        if (content_idListSize>0){
            try {
                myLearningwiseContentView.llFragMyLearningProgressWheel.visibility = View.VISIBLE
                val repository = LMSRepoProvider.getTopicList()
                DashboardActivity.compositeDisposable.add(
                    repository.getTopicContentWiseAnswerLists(Pref.user_id!!,
                        topic_id, content_idL.get(content_idListSize-1))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({ result ->
                            val response = result as TopicContentWiseAnswerListsFetchResponse
                            if (response.status == NetworkConstant.SUCCESS) {
                                myLearningwiseContentView.llFragMyLearningProgressWheel.visibility = View.GONE
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
                                myLearningwiseContentView.llFragMyLearningProgressWheel.visibility = View.GONE
                                Toast.makeText(mContext, getString(R.string.no_data_found), Toast.LENGTH_SHORT).show()

                            }
                        }, { error ->
                            myLearningwiseContentView.llFragMyLearningProgressWheel.visibility = View.GONE
                            Toast.makeText(mContext, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show()
                        })
                )
            } catch (ex: Exception) {
                ex.printStackTrace()
                myLearningwiseContentView.llFragMyLearningProgressWheel.visibility = View.GONE
                Toast.makeText(mContext, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show()
            }
        }
        else{
            val sortedList = contentL.sortedBy { it.content_play_sequence.toInt() }.toCollection(ArrayList())
            Log.d("sortedList", "" + sortedList)
            setLearningData(sortedList , topic_name, contentWiseAnswerL)
        }
    }

    fun setLearningData(
        finalDatal: ArrayList<ContentL>,
        topic_name: String,
        contentWiseAnswerL: ArrayList<ContentWiseAnswerL>
    ) {
        myLearningwiseContentView.rvMylearningProgress.layoutManager = LinearLayoutManager(mContext)
            val adapter = MyLearningProgressAdapter(
                mContext,
                finalDatal,
                topic_name,
                listener = object : MyLearningProgressAdapter.OnItemClickListener {
                    override fun onItemClick(item: ContentL, position: Int) {
                        val store_topic_id = topic_id
                        val store_topic_name = Companion.topic_name
                        val store_content_id = item.content_id
                        VideoPlayLMS.loadedFrom = "SearchLmsLearningFrag"
                        CustomStatic.VideoPosition = position
                        Pref.videoCompleteCount = "0"
                        (mContext as DashboardActivity).loadFrag(VideoPlayLMS.getInstance(topic_id+"~"+ Companion.topic_name), VideoPlayLMS::class.java.name,true)

                    }

                    override fun onRetryClick(item: ContentL, position: Int) {

                        val store_topic_id = AllTopicsWiseContents.topic_id
                        val store_topic_name = AllTopicsWiseContents.topic_name
                        val store_content_id = item.content_id
                        VideoPlayLMS.loadedFrom = "SearchLmsLearningFrag"
                        CustomStatic.VideoPosition = position

                        CustomStatic.RetryTopicId = topic_id.toInt()
                        CustomStatic. RetryTopicName= Companion.topic_name
                        CustomStatic.RetryContentId = store_content_id.toInt()
                        CustomStatic.RetryContentName = item.content_title
                        CustomStatic.RetryContentURL = item.content_url
                        (mContext as DashboardActivity).loadFrag(RetryIncorrectQuizFrag.getInstance( topic_id +"~"+ store_content_id +"~"+ Companion.topic_name +"~"+item.content_url +"~"+item.content_title),RetryIncorrectQuizFrag::class.java.name, true)

                    }
                },
                contentWiseAnswerL
            )
        myLearningwiseContentView.rvMylearningProgress.adapter = adapter
        myLearningwiseContentView.rvMylearningProgress.visibility =View.VISIBLE
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {

            myLearningwiseContentView.includeBottomTabLms.llLmsMylearning.id -> {
                (mContext as DashboardActivity).loadFrag(
                    SearchLmsFrag(),SearchLmsFrag::class.java.name, true)
            }

            myLearningwiseContentView.includeBottomTabLms.llLmsKnowledgehub.id -> {
                (mContext as DashboardActivity).loadFrag(
                    SearchLmsKnowledgeFrag(),SearchLmsKnowledgeFrag::class.java.name, true
                )
            }
            myLearningwiseContentView.includeBottomTabLms.llLmsPerformance.id -> {
                (mContext as DashboardActivity).loadFrag(
                    PerformanceInsightPage(),PerformanceInsightPage::class.java.name, true)
            }
            myLearningwiseContentView.llFragSearchRoot.id -> {
                AppUtils.hideSoftKeyboard(mContext as DashboardActivity)
                myLearningwiseContentView.llFragMyLearningProgressWheel.spin()
                GlobalScope.launch(Dispatchers.IO) {
                    var tempSearchL = contentL.filter { it.content_title.contains(myLearningwiseContentView.etFragLearningSearch.text.toString().trim(), ignoreCase = true) || it.content_description.contains(myLearningwiseContentView.etFragLearningSearch.text.toString().trim(), ignoreCase = true)  } as ArrayList<ContentL>
                    withContext(Dispatchers.Main) {
                        myLearningwiseContentView.llFragMyLearningProgressWheel.stopSpinning()
                        if(tempSearchL.size>0){
                            myLearningwiseContentView.rvMylearningProgress.visibility =View.VISIBLE
                            myLearningwiseContentView.llContinueLearning.visibility =View.VISIBLE
                            myLearningwiseContentView.llNoData.visibility =View.GONE
                            setLearningData(tempSearchL, topic_name, contentWiseAnswerL)
                        }else{
                            myLearningwiseContentView.rvMylearningProgress.visibility =View.GONE
                            myLearningwiseContentView.llContinueLearning.visibility =View.GONE
                            myLearningwiseContentView.llNoData.visibility =View.VISIBLE
                        }
                    }
                }
            }

            myLearningwiseContentView.ivFragSpk.id ->{
                suffixText = myLearningwiseContentView.etFragLearningSearch.text.toString().trim()
                startVoiceInput()
            }
        }
    }

    fun updateList(){
        getMyLarningInfoAPI()
    }

    fun startVoiceInput() {
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
                    myLearningwiseContentView.etFragLearningSearch.setText(suffixText+t)
                    myLearningwiseContentView.etFragLearningSearch.setSelection(setFullText.length);
                }else{
                    var SuffixPostText = t+myLearningwiseContentView.etFragLearningSearch.text.toString()
                    myLearningwiseContentView.etFragLearningSearch.setText(SuffixPostText)
                    myLearningwiseContentView.etFragLearningSearch.setSelection(SuffixPostText.length);
                }
            }
            catch (ex:Exception) {
                ex.printStackTrace()
            }
        }
    }
}
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
import com.breezemobilearndemo.databinding.FragmentAllTopicsWiseContentsBinding
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class AllTopicsWiseContents : Fragment(), OnClickListener , MyLearningProgressAdapter.OnItemClickListener{

    private var binding : FragmentAllTopicsWiseContentsBinding? = null
    private val alltopicsWiseContentView get() = binding!!

    private lateinit var mContext: Context
    var contentL: ArrayList<ContentL> = ArrayList()
    var content_idListSize :Int =0
    var contentWiseAnswerL : ArrayList<ContentWiseAnswerL> =ArrayList()
    var content_idL: ArrayList<String> = ArrayList()
    private var  suffixText:String = ""

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext=context
    }

    companion object {
        var topic_id: String = ""
        var topic_name: String = ""

        fun getInstance(objects: Any): AllTopicsWiseContents {
            val allTopicsWiseContents = AllTopicsWiseContents()

            if (!TextUtils.isEmpty(objects.toString())) {
                val parts = objects.toString().split("~")
                topic_id = parts[0]
                topic_name = parts[1]
            } else {
                topic_id = ""
                topic_name = ""
            }

            return allTopicsWiseContents
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentAllTopicsWiseContentsBinding.inflate(inflater,container,false)
        return alltopicsWiseContentView.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        requireActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

        alltopicsWiseContentView.includeBottomTabLms.ivLmsPerformance.setImageResource(R.drawable.performance_insights)
        alltopicsWiseContentView.includeBottomTabLms.ivLmsMylearning.setImageResource(R.drawable.open_book_lms_)
        alltopicsWiseContentView.includeBottomTabLms.ivLmsKnowledgehub.setImageResource(R.drawable.all_topics_selected)

        alltopicsWiseContentView.includeBottomTabLms.tvLmsPerformance.setTextColor(getResources().getColor(R.color.black))
        alltopicsWiseContentView.includeBottomTabLms.tvLmsMylearning.setTextColor(getResources().getColor(R.color.black))
        alltopicsWiseContentView.includeBottomTabLms.tvLmsKnowledgehub.setTextColor(getResources().getColor(R.color.toolbar_lms))

        alltopicsWiseContentView.progressWheel.stopSpinning()

        getMyLarningInfoAPI()

        alltopicsWiseContentView.etFragLearningSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            @SuppressLint("SuspiciousIndentation")
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                var tempSearchL = contentL.filter { it.content_title.contains(alltopicsWiseContentView.etFragLearningSearch.text.toString().trim(), ignoreCase = true) || it.content_description.contains(alltopicsWiseContentView.etFragLearningSearch.text.toString().trim(), ignoreCase = true)  } as ArrayList<ContentL>
                alltopicsWiseContentView.progressWheel.stopSpinning()
                if(tempSearchL.size>0){
                    alltopicsWiseContentView.rvMylearningProgress.visibility =View.VISIBLE
                    alltopicsWiseContentView.llContinueLearning.visibility =View.VISIBLE
                    alltopicsWiseContentView.llNoData.visibility =View.GONE
                    setLearningData(tempSearchL, topic_name, contentWiseAnswerL)
                }else{
                    alltopicsWiseContentView.rvMylearningProgress.visibility =View.GONE
                    alltopicsWiseContentView.llContinueLearning.visibility =View.GONE
                    alltopicsWiseContentView.llNoData.visibility =View.VISIBLE
                }
            }
            override fun afterTextChanged(s: Editable) {}
        })

        alltopicsWiseContentView.includeBottomTabLms.llLmsPerformance.setOnClickListener(this)
        alltopicsWiseContentView.includeBottomTabLms.llLmsMylearning.setOnClickListener(this)
        alltopicsWiseContentView.includeBottomTabLms.llLmsKnowledgehub.setOnClickListener(this)
        alltopicsWiseContentView.ivFragSpk.setOnClickListener(this)
        alltopicsWiseContentView.llFragSearchRoot.setOnClickListener(this)

    }

    private fun getMyLarningInfoAPI() {
        try {
            if(topic_id.equals("")){
                topic_id = CustomStatic.TOPIC_SEL
            }
            alltopicsWiseContentView.progressWheel.spin()
            val repository = LMSRepoProvider.getTopicList()
            DashboardActivity.compositeDisposable.add(
                repository.getTopicsWiseVideo(Pref.user_id!!, topic_id)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ result ->
                        val response = result as VideoTopicWiseResponse
                        if (response.status == NetworkConstant.SUCCESS) {
                            alltopicsWiseContentView.progressWheel.stopSpinning()
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
                            alltopicsWiseContentView.progressWheel.stopSpinning()
                            Toast.makeText(mContext, getString(R.string.no_data_found), Toast.LENGTH_SHORT).show()

                        }
                    }, { error ->
                        alltopicsWiseContentView.progressWheel.stopSpinning()
                        Toast.makeText(mContext, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show()
                    })
            )
        } catch (ex: Exception) {
            ex.printStackTrace()
            alltopicsWiseContentView.progressWheel.stopSpinning()
            Toast.makeText(mContext, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show()
        }
    }

    private fun process() {

        getTopicContentWiseAnswerListsAPICalling( )

    }

    private fun getTopicContentWiseAnswerListsAPICalling(
    ) {

        if (content_idListSize>0){
            try {
                alltopicsWiseContentView.progressWheel.spin()
                val repository = LMSRepoProvider.getTopicList()
                DashboardActivity.compositeDisposable.add(
                    repository.getTopicContentWiseAnswerLists(Pref.user_id!!,
                        topic_id, content_idL.get(content_idListSize-1))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({ result ->
                            val response = result as TopicContentWiseAnswerListsFetchResponse
                            if (response.status == NetworkConstant.SUCCESS) {
                                alltopicsWiseContentView.progressWheel.stopSpinning()

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
                                alltopicsWiseContentView.progressWheel.stopSpinning()
                                Toast.makeText(mContext, response.message, Toast.LENGTH_SHORT).show()

                            }
                        }, { error ->
                            alltopicsWiseContentView.progressWheel.stopSpinning()
                            Toast.makeText(mContext, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show()
                        })
                )
            } catch (ex: Exception) {
                ex.printStackTrace()
                alltopicsWiseContentView.progressWheel.stopSpinning()
                Toast.makeText(mContext, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show()
            }
        }
        else{
            val sortedList = contentL.sortedBy { it.content_play_sequence.toInt() }.toCollection(ArrayList())
            setLearningData(sortedList , topic_name, contentWiseAnswerL)
        }

    }

    private fun setLearningData(
        finalDatal: ArrayList<ContentL>,
        topic_name: String,
        contentWiseAnswerL: ArrayList<ContentWiseAnswerL>
    ) {
            alltopicsWiseContentView.rvMylearningProgress.layoutManager = LinearLayoutManager(mContext)
            val adapter = MyLearningProgressAdapter(
                mContext,
                finalDatal,
                topic_name,
                this,
                this.contentWiseAnswerL
            )
            alltopicsWiseContentView.rvMylearningProgress.adapter = adapter
            alltopicsWiseContentView.rvMylearningProgress.visibility =View.VISIBLE

    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            alltopicsWiseContentView.includeBottomTabLms.llLmsMylearning.id -> {
                (mContext as DashboardActivity).loadFrag(SearchLmsFrag(),SearchLmsFrag::class.java.name,
                    true
                )
            }
            alltopicsWiseContentView.includeBottomTabLms.llLmsKnowledgehub.id -> {
                (mContext as DashboardActivity).loadFrag(SearchLmsKnowledgeFrag(),SearchLmsKnowledgeFrag::class.java.name,true)
            }
            alltopicsWiseContentView.includeBottomTabLms.llLmsPerformance.id -> {
                (mContext as DashboardActivity).loadFrag(PerformanceInsightPage(), PerformanceInsightPage::class.java.name , true)
            }

            alltopicsWiseContentView.llFragSearchRoot.id -> {
                AppUtils.hideSoftKeyboard(mContext as DashboardActivity)
                alltopicsWiseContentView.progressWheel.spin()

                GlobalScope.launch(Dispatchers.IO) {
                    var tempSearchL = contentL.filter { it.content_title.contains(alltopicsWiseContentView.etFragLearningSearch.text.toString().trim(), ignoreCase = true) || it.content_description.contains(alltopicsWiseContentView.etFragLearningSearch.text.toString().trim(), ignoreCase = true)  } as ArrayList<ContentL>
                    withContext(Dispatchers.Main) {
                        alltopicsWiseContentView.progressWheel.stopSpinning()
                        if(tempSearchL.size>0){
                            alltopicsWiseContentView.rvMylearningProgress.visibility =View.VISIBLE
                            alltopicsWiseContentView.llContinueLearning.visibility =View.VISIBLE
                            alltopicsWiseContentView.llNoData.visibility =View.GONE
                            setLearningData(tempSearchL, topic_name, contentWiseAnswerL)
                        }else{
                            alltopicsWiseContentView.rvMylearningProgress.visibility =View.GONE
                            alltopicsWiseContentView.llContinueLearning.visibility =View.GONE
                            alltopicsWiseContentView.llNoData.visibility =View.VISIBLE
                        }
                    }
                }
            }


            alltopicsWiseContentView.ivFragSpk.id ->{
                suffixText = alltopicsWiseContentView.etFragLearningSearch.text.toString().trim()
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
                    alltopicsWiseContentView.etFragLearningSearch.setText(suffixText+t)
                    alltopicsWiseContentView.etFragLearningSearch.setSelection(setFullText.length);
                }else{
                    var SuffixPostText = t+alltopicsWiseContentView.etFragLearningSearch.text.toString()
                    alltopicsWiseContentView.etFragLearningSearch.setText(SuffixPostText)
                    alltopicsWiseContentView.etFragLearningSearch.setSelection(SuffixPostText.length);
                }
            }
            catch (ex:Exception) {
                ex.printStackTrace()
            }
        }
    }

    override fun onItemClick(item: ContentL , position: Int) {
        val store_topic_id = topic_id
        val store_topic_name = topic_name
        val store_content_id = item.content_id
        VideoPlayLMS.loadedFrom = "AllTopicsWiseContents"
        CustomStatic.VideoPosition = position
        Pref.videoCompleteCount = "0"
        (mContext as DashboardActivity).loadFrag(VideoPlayLMS.getInstance(topic_id +"~"+ topic_name),VideoPlayLMS::class.java.name, true)
    }



    override fun onRetryClick(item: ContentL, position: Int) {
        val store_topic_id = topic_id
        val store_topic_name = topic_name
        val store_content_id = item.content_id
        VideoPlayLMS.loadedFrom = "AllTopicsWiseContents"
        CustomStatic.VideoPosition = position

        CustomStatic.RetryTopicId = topic_id.toInt()
        CustomStatic. RetryTopicName= topic_name
        CustomStatic.RetryContentId = store_content_id.toInt()
        CustomStatic.RetryContentName = item.content_title
        CustomStatic.RetryContentURL = item.content_url
        (mContext as DashboardActivity).loadFrag(RetryIncorrectQuizFrag.getInstance(topic_id +"~"+ store_content_id +"~"+ topic_name +"~"+item.content_url +"~"+item.content_title), RetryIncorrectQuizFrag::class.java.name,true )
    }

    override fun onResume() {
        super.onResume()
        (mContext as DashboardActivity).toolbarTitle.text = "All Topics"
    }
}
package com.breezemobilearndemo

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
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.breezemobilearndemo.api.LMSRepoProvider
import com.breezemobilearndemo.databinding.FragmentAllTopicsWiseContentsBinding
import com.breezemobilearndemo.databinding.FragmentMyLearningBinding
import com.breezemobilearndemo.databinding.FragmentMyLearningTopicListBinding
import com.breezemobilearndemo.databinding.FragmentSearchLmsBinding
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MyLearningTopicList : Fragment(), View.OnClickListener, LmsSearchAdapter.OnItemClickListener {

    private var binding : FragmentSearchLmsBinding? = null
    private val myLearningView get() = binding!!
    lateinit var courseList: List<LmsSearchData>
    lateinit var sortedCourseList: List<LmsSearchData>
    lateinit var lmsSearchAdapter: LmsSearchAdapter
    private lateinit var mContext: Context
    private var  suffixText:String = ""

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    companion object {
        var topic_id: String = ""
        var topic_name: String = ""

        fun getInstance(objects: Any): MyLearningTopicList {
            val myLearningTopicList = MyLearningTopicList()

            if (!TextUtils.isEmpty(objects.toString())) {
                val parts = objects.toString().split("~")
                topic_id = parts[0]
                topic_name = parts[1]
            } else {
                topic_id = ""
                topic_name = ""
            }

            return myLearningTopicList
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchLmsBinding.inflate(inflater, container, false)
        return myLearningView.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        requireActivity().getWindow()
            .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        myLearningView.bottomLayoutLms.ivLmsPerformance.setImageResource(R.drawable.performance_insights)
        myLearningView.bottomLayoutLms.ivLmsMylearning .setImageResource(R.drawable.my_topics_selected)
        myLearningView.bottomLayoutLms.ivLmsKnowledgehub .setImageResource(R.drawable.set_of_books_lms)

        myLearningView.bottomLayoutLms.tvLmsPerformance .setTextColor(getResources().getColor(R.color.toolbar_lms))
        myLearningView.bottomLayoutLms.tvLmsMylearning .setTextColor(getResources().getColor(R.color.black))
        myLearningView.bottomLayoutLms.tvLmsKnowledgehub .setTextColor(getResources().getColor(R.color.black))

        myLearningView.llFragSearch.setOnClickListener(this)
        myLearningView.ivFragSearchVoice.setOnClickListener(this)

        if (AppUtils.isOnline(mContext)) {
            myLearningView.llMyLearningTopicList.visibility = View.VISIBLE
            getTopicL()
        }else{
            Toast.makeText(mContext, R.string.no_internet, Toast.LENGTH_SHORT).show()
            myLearningView.llMyLearningTopicList.visibility = View.INVISIBLE

        }
        myLearningView.etFragSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (!myLearningView.etFragSearch.text.toString().trim().equals("")) {
                    myLearningView.progressWheel.spin()
                    GlobalScope.launch(Dispatchers.IO) {
                        myLearningView.progressWheel.stopSpinning()
                        if (courseList.size>0) {
                            myLearningView.progressWheel.stopSpinning()
                            var tempSearchL = courseList.filter {
                                it.courseName.contains(
                                    myLearningView.etFragSearch.text.toString().trim(), ignoreCase = true
                                )
                            }
                            withContext(Dispatchers.Main) {
                                myLearningView.progressWheel.stopSpinning()
                                if (tempSearchL.size > 0) {
                                    myLearningView.gvSearch.visibility = View.VISIBLE
                                    setTopicAdapter(tempSearchL)
                                } else {
                                    myLearningView.gvSearch.visibility = View.GONE
                                }
                            }
                        }
                    }
                }
                else{

                    GlobalScope.launch(Dispatchers.IO) {
                        var tempSearchL = courseList
                        withContext(Dispatchers.Main) {
                            myLearningView.progressWheel.stopSpinning()
                            setTopicAdapter(tempSearchL)
                        }
                    }
                }
            }

            override fun afterTextChanged(s: Editable) {

            }
        })
        myLearningView.bottomLayoutLms.llLmsPerformance .setOnClickListener(this)
        myLearningView.bottomLayoutLms.llLmsMylearning .setOnClickListener(this)
        myLearningView.bottomLayoutLms.llLmsKnowledgehub .setOnClickListener(this)

    }
    override fun onItemClick(item: LmsSearchData) {
        /*if (lmsSearchAdapter.getSelectedPosition() == RecyclerView.NO_POSITION) {
            Toast.makeText(mContext, "Please select one topic", Toast.LENGTH_SHORT).show()
        } else {*/
            val selectedItem = item
            VideoPlayLMS.previousFrag = SearchLmsFrag.toString()
            VideoPlayLMS.loadedFrom = "SearchLmsFrag"
            Pref.videoCompleteCount = "0"

            (mContext as DashboardActivity).loadFrag(SearchLmsLearningFrag.getInstance(selectedItem.searchid+"~"+selectedItem.courseName), SearchLmsLearningFrag::class.java.name ,true )
       // }
    }

    fun getTopicL() {
        myLearningView.progressWheel.spin()
        try {
            val repository = LMSRepoProvider.getTopicList()
            DashboardActivity.compositeDisposable.add(
                repository.getTopics(Pref.user_id!!)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ result ->
                        val response = result as TopicListResponse
                        if (response.status == NetworkConstant.SUCCESS) {
                            sortedCourseList  = ArrayList<LmsSearchData>()
                            courseList = ArrayList<LmsSearchData>()
                            for (i in 0..response.topic_list.size - 1) {
                                if (response.topic_list.get(i).video_count!= 0) {
                                    sortedCourseList = sortedCourseList + LmsSearchData(
                                        response.topic_list.get(i).topic_id.toString(),
                                        response.topic_list.get(i).topic_name,
                                        response.topic_list.get(i).video_count,
                                        response.topic_list.get(i).topic_parcentage,
                                        response.topic_list.get(i).topic_sequence,
                                    )
                                    courseList = sortedCourseList.sortedBy { it.topic_sequence }
                                }
                            }
                            (mContext as DashboardActivity).toolbarTitle.text = "My Learning"
                            myLearningView.progressWheel.stopSpinning()
                            setTopicAdapter(courseList)
                        }else{
                            myLearningView.progressWheel.stopSpinning()
                            Toast.makeText(mContext, getString(R.string.no_data_found), Toast.LENGTH_SHORT).show()

                        }
                    }, { error ->
                        myLearningView.progressWheel.stopSpinning()
                        Toast.makeText(mContext, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show()
                    })
            )
        }
        catch (ex: Exception) {
            ex.printStackTrace()
            myLearningView.progressWheel.stopSpinning()
            Toast.makeText(mContext, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {

            myLearningView.bottomLayoutLms .llLmsMylearning .id -> {
                (mContext as DashboardActivity).loadFrag(SearchLmsFrag(),SearchLmsFrag::class.java.name)
            }

            myLearningView.bottomLayoutLms.llLmsKnowledgehub.id -> {
                (mContext as DashboardActivity).loadFrag(SearchLmsKnowledgeFrag(),SearchLmsKnowledgeFrag::class.java.name)
            }
            myLearningView.bottomLayoutLms.llLmsPerformance .id -> {
                (mContext as DashboardActivity).loadFrag(PerformanceInsightPage(),PerformanceInsightPage::class.java.name)
            }

            myLearningView.llFragSearch .id -> {
                AppUtils.hideSoftKeyboard(mContext as DashboardActivity)
                if (!myLearningView.etFragSearch.text.toString().trim().equals("")) {
                    GlobalScope.launch(Dispatchers.IO) {
                        myLearningView.progressWheel.spin()
                        if (courseList.size>0) {
                            var tempSearchL = courseList.filter {
                                it.courseName.contains(
                                    myLearningView.etFragSearch .text.toString().trim(), ignoreCase = true
                                )
                            }
                            withContext(Dispatchers.Main) {
                                myLearningView.progressWheel.stopSpinning()

                                if (tempSearchL.size > 0) {
                                    myLearningView.gvSearch.visibility = View.VISIBLE
                                    myLearningView.progressWheel.stopSpinning()

                                    setTopicAdapter(tempSearchL)
                                } else {
                                    myLearningView.gvSearch.visibility = View.GONE
                                    myLearningView.progressWheel.stopSpinning()

                                }
                            }
                        }
                    }

                }
            }

            myLearningView.ivFragSearchVoice .id ->{
                suffixText = myLearningView.etFragSearch.text.toString().trim()
                startVoiceInput()
            }
        }
    }

    fun setTopicAdapter(list:List<LmsSearchData>) {
        myLearningView.gvSearch.visibility =View.VISIBLE

        lmsSearchAdapter = LmsSearchAdapter(mContext, list, this.javaClass.simpleName,this)
        myLearningView.gvSearch.adapter = lmsSearchAdapter

        myLearningView.tvNextAfterSearchLms .setOnClickListener {
        }
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
                    myLearningView.etFragSearch .setText(suffixText+t)
                    myLearningView.etFragSearch.setSelection(setFullText.length);
                }else{
                    var SuffixPostText = t+myLearningView.etFragSearch.text.toString()
                    myLearningView.etFragSearch.setText(SuffixPostText)
                    myLearningView.etFragSearch.setSelection(SuffixPostText.length);
                }
            }
            catch (ex:Exception) {
                ex.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    override fun onResume() {
        super.onResume()
        (mContext as DashboardActivity).toolbarTitle.text = "My Learning"
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

}
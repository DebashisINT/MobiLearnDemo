package com.breezemobilearndemo

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.breezemobilearndemo.api.LMSRepoProvider
import com.breezemobilearndemo.databinding.FragmentMyLearningBinding
import com.breezemobilearndemo.databinding.FragmentSearchLmsKnowledgeBinding
import com.pnikosis.materialishprogress.ProgressWheel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchLmsKnowledgeFrag : Fragment(), OnClickListener , LmsSearchAdapter.OnItemClickListener{
    private var binding : FragmentSearchLmsKnowledgeBinding? = null
    private val searchLmsKnowledgeFragView get() = binding!!

    private lateinit var mContext: Context
    lateinit var courseList: List<LmsSearchData>
    lateinit var sortedCourseList: List<LmsSearchData>
    lateinit var lmsSearchAdapter: LmsSearchAdapter
    private var  suffixText:String = ""

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext=context
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SearchLmsKnowledgeFrag().apply {

            }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSearchLmsKnowledgeBinding.inflate(inflater,container,false)
        return searchLmsKnowledgeFragView.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        searchLmsKnowledgeFragView.includeBottomTabLms.ivLmsPerformance.setImageResource(R.drawable.performance_insights)
        searchLmsKnowledgeFragView.includeBottomTabLms.ivLmsMylearning.setImageResource(R.drawable.open_book_lms_)
        searchLmsKnowledgeFragView.includeBottomTabLms.ivLmsKnowledgehub.setImageResource(R.drawable.all_topics_selected)

        searchLmsKnowledgeFragView.includeBottomTabLms.tvLmsPerformance.setTextColor(getResources().getColor(R.color.black))
        searchLmsKnowledgeFragView.includeBottomTabLms.tvLmsMylearning.setTextColor(getResources().getColor(R.color.black))
        searchLmsKnowledgeFragView.includeBottomTabLms.tvLmsKnowledgehub.setTextColor(getResources().getColor(R.color.toolbar_lms))


        if (AppUtils.isOnline(mContext)) {
            searchLmsKnowledgeFragView.llMyLearningTopicList.visibility =View.VISIBLE
            getTopicL()
        }else{
            Toast.makeText(mContext, getString(R.string.no_internet), Toast.LENGTH_SHORT).show()
            searchLmsKnowledgeFragView.llMyLearningTopicList.visibility =View.INVISIBLE

        }
        searchLmsKnowledgeFragView.etFragContactsSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (!searchLmsKnowledgeFragView.etFragContactsSearch.text.toString().trim().equals("")) {
                    searchLmsKnowledgeFragView.progressWheel.spin()
                    GlobalScope.launch(Dispatchers.IO) {
                        if (courseList.size>0) {
                            var tempSearchL = courseList.filter {
                                it.courseName.contains(
                                    searchLmsKnowledgeFragView.etFragContactsSearch .text.toString().trim(), ignoreCase = true
                                )
                            }
                            withContext(Dispatchers.Main) {
                                if (tempSearchL.size > 0) {
                                    searchLmsKnowledgeFragView.gvSearch.visibility = View.VISIBLE
                                    setTopicAdapter(tempSearchL)
                                } else {
                                    searchLmsKnowledgeFragView.gvSearch.visibility = View.GONE
                                }
                            }
                        }
                    }

                }else{
                    GlobalScope.launch(Dispatchers.IO) {
                        if (courseList.size > 0) {
                            var tempSearchL = courseList
                            withContext(Dispatchers.Main) {
                                searchLmsKnowledgeFragView.progressWheel.stopSpinning()
                                setTopicAdapter(tempSearchL)

                            }
                        }
                    }
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })

        searchLmsKnowledgeFragView.includeBottomTabLms.llLmsPerformance.setOnClickListener(this)
        searchLmsKnowledgeFragView.includeBottomTabLms.llLmsMylearning.setOnClickListener(this)
        searchLmsKnowledgeFragView.includeBottomTabLms.llLmsKnowledgehub.setOnClickListener(this)
        searchLmsKnowledgeFragView.llSearch.setOnClickListener(this)
        searchLmsKnowledgeFragView.ivMicSearch.setOnClickListener(this)


    }

    fun getTopicL() {
        searchLmsKnowledgeFragView.progressWheel.spin()
        try {
            val repository = LMSRepoProvider.getTopicList()
            DashboardActivity.compositeDisposable.add(
                repository.getTopics("0")
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ result ->
                        val response = result as TopicListResponse
                        if (response.status == NetworkConstant.SUCCESS) {
                            courseList = ArrayList<LmsSearchData>()
                            sortedCourseList = ArrayList<LmsSearchData>()
                            for (i in 0..response.topic_list.size - 1) {
                                if (response.topic_list.get(i).video_count!= 0) {
                                    sortedCourseList = sortedCourseList + LmsSearchData(
                                        response.topic_list.get(i).topic_id.toString(),
                                        response.topic_list.get(i).topic_name,
                                        response.topic_list.get(i).video_count,
                                        response.topic_list.get(i).topic_parcentage,
                                        response.topic_list.get(i).topic_sequence
                                    )
                                    courseList = sortedCourseList.sortedBy { it.topic_sequence }
                                }
                            }
                            searchLmsKnowledgeFragView.progressWheel.stopSpinning()
                            setTopicAdapter(courseList)

                        } else {
                            searchLmsKnowledgeFragView.progressWheel.stopSpinning()
                            Toast.makeText(mContext, getString(R.string.no_data_found), Toast.LENGTH_SHORT).show()
                        }
                    }, { error ->
                        searchLmsKnowledgeFragView.progressWheel.stopSpinning()
                        Toast.makeText(mContext, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show()
                    })
            )
        }
        catch (ex: Exception) {
            ex.printStackTrace()
            searchLmsKnowledgeFragView.progressWheel.stopSpinning()
            Toast.makeText(mContext, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show()
        }
    }

    fun setTopicAdapter(list:List<LmsSearchData>) {
        searchLmsKnowledgeFragView.progressWheel.stopSpinning()
        searchLmsKnowledgeFragView.gvSearch.visibility =View.VISIBLE
        lmsSearchAdapter = LmsSearchAdapter(mContext, list, SearchLmsKnowledgeFrag::class.java.name, this)
        searchLmsKnowledgeFragView.gvSearch.adapter = lmsSearchAdapter

        searchLmsKnowledgeFragView.tvNextAfterSearchLms.setOnClickListener {

        }
    }


    override fun onItemClick(item: LmsSearchData) {
        if (lmsSearchAdapter.getSelectedPosition() == RecyclerView.NO_POSITION) {
            Toast.makeText(mContext, "Please select one topic", Toast.LENGTH_SHORT).show()
        } else {
            val selectedItem = item
            VideoPlayLMS.previousFrag = SearchLmsFrag.toString()
            VideoPlayLMS.loadedFrom = "SearchLmsKnowledgeFrag"
            Pref.videoCompleteCount = "0"
            (mContext as DashboardActivity).loadFrag(AllTopicsWiseContents.getInstance(selectedItem.searchid+"~"+selectedItem.courseName), AllTopicsWiseContents::class.java.name, true, )
        }
    }

    override fun onClick(p0: View?) {

        when (p0?.id) {
            searchLmsKnowledgeFragView.includeBottomTabLms.llLmsMylearning.id -> {
                (mContext as DashboardActivity).loadFrag(SearchLmsFrag(),SearchLmsFrag::class.java.name,
                    true
                )
            }
            searchLmsKnowledgeFragView.includeBottomTabLms.llLmsKnowledgehub.id -> {
                (mContext as DashboardActivity).loadFrag(SearchLmsKnowledgeFrag(),SearchLmsKnowledgeFrag::class.java.name,true)
            }
            searchLmsKnowledgeFragView.includeBottomTabLms.llLmsPerformance.id -> {
                (mContext as DashboardActivity).loadFrag(PerformanceInsightPage(), PerformanceInsightPage::class.java.name , true)
            }

            searchLmsKnowledgeFragView.llSearch.id -> {
                AppUtils.hideSoftKeyboard(mContext as DashboardActivity)
                if (!searchLmsKnowledgeFragView.etFragContactsSearch.text.toString().trim().equals("")) {
                    searchLmsKnowledgeFragView.progressWheel.spin()
                    GlobalScope.launch(Dispatchers.IO) {
                        searchLmsKnowledgeFragView.progressWheel.stopSpinning()
                        if (courseList.size > 0) {
                            var tempSearchL = courseList.filter {
                                it.courseName.contains(
                                    searchLmsKnowledgeFragView.etFragContactsSearch.text.toString().trim(), ignoreCase = true
                                )
                            }
                            withContext(Dispatchers.Main) {
                                searchLmsKnowledgeFragView.progressWheel.stopSpinning()
                                if (tempSearchL.size > 0) {
                                    searchLmsKnowledgeFragView.gvSearch.visibility = View.VISIBLE
                                    setTopicAdapter(tempSearchL)
                                } else {
                                    searchLmsKnowledgeFragView.gvSearch.visibility = View.GONE
                                }
                            }
                        }
                    }
                }
            }

            searchLmsKnowledgeFragView.ivMicSearch.id ->{
                suffixText = searchLmsKnowledgeFragView.etFragContactsSearch.text.toString().trim()
                startVoiceInput()
            }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 7009) {
            try {
                val result = data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                var t = result!![0]
                if (suffixText.length > 0 && !suffixText.equals("")) {
                    var setFullText = suffixText + t
                    searchLmsKnowledgeFragView.etFragContactsSearch.setText(suffixText + t)
                    searchLmsKnowledgeFragView.etFragContactsSearch.setSelection(setFullText.length);
                } else {
                    var SuffixPostText = t + searchLmsKnowledgeFragView.etFragContactsSearch.text.toString()
                    searchLmsKnowledgeFragView.etFragContactsSearch.setText(SuffixPostText)
                    searchLmsKnowledgeFragView.etFragContactsSearch.setSelection(SuffixPostText.length);
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        (mContext as DashboardActivity).toolbarTitle.text = "All Topics"
    }

}
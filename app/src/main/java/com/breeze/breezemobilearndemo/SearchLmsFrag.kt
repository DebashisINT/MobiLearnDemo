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
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.breezemobilearndemo.api.LMSRepoProvider
import com.breezemobilearndemo.databinding.FragmentSearchLmsBinding
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchLmsFrag : Fragment() , View.OnClickListener, LmsSearchAdapter.OnItemClickListener{

    private var binding : FragmentSearchLmsBinding? = null
    private val searchLmsFragView get() = binding!!
    lateinit var courseList: List<LmsSearchData>
    lateinit var sortedCourseList: List<LmsSearchData>
    lateinit var lmsSearchAdapter: LmsSearchAdapter
    private lateinit var mContext: Context
    private var  suffixText:String = ""

    companion object {
        fun getInstance(objects: Any): SearchLmsFrag {
            val fragment = SearchLmsFrag()
            return fragment
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
        binding = FragmentSearchLmsBinding.inflate(inflater,container,false)
        return searchLmsFragView.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        initView()
    }

    private fun initView() {

        searchLmsFragView.bottomLayoutLms.ivLmsPerformance.setImageResource(R.drawable.performance_insights)
        searchLmsFragView.bottomLayoutLms.ivLmsMylearning .setImageResource(R.drawable.my_topics_selected)
        searchLmsFragView.bottomLayoutLms.ivLmsKnowledgehub .setImageResource(R.drawable.set_of_books_lms)

        searchLmsFragView.bottomLayoutLms.tvLmsPerformance .setTextColor(getResources().getColor(R.color.black))
        searchLmsFragView.bottomLayoutLms.tvLmsMylearning .setTextColor(getResources().getColor(R.color.toolbar_lms))
        searchLmsFragView.bottomLayoutLms.tvLmsKnowledgehub .setTextColor(getResources().getColor(R.color.black))

        searchLmsFragView.llFragSearch.setOnClickListener(this)
        searchLmsFragView.ivFragSearchVoice.setOnClickListener(this)

        if (AppUtils.isOnline(mContext)) {
            searchLmsFragView.llMyLearningTopicList.visibility = View.VISIBLE
            getTopicL()
        }else{
            Toast.makeText(mContext, R.string.no_internet, Toast.LENGTH_SHORT).show()
            searchLmsFragView.llMyLearningTopicList.visibility = View.INVISIBLE

        }
        searchLmsFragView.etFragSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (!searchLmsFragView.etFragSearch.text.toString().trim().equals("")) {
                    searchLmsFragView.progressWheel.spin()
                    GlobalScope.launch(Dispatchers.IO) {
                        searchLmsFragView.progressWheel.stopSpinning()
                        if (courseList.size>0) {
                            searchLmsFragView.progressWheel.stopSpinning()
                            var tempSearchL = courseList.filter {
                                it.courseName.contains(
                                    searchLmsFragView.etFragSearch.text.toString().trim(), ignoreCase = true
                                )
                            }
                            withContext(Dispatchers.Main) {
                                searchLmsFragView.progressWheel.stopSpinning()

                                if (tempSearchL.size > 0) {
                                    searchLmsFragView.gvSearch.visibility = View.VISIBLE
                                    setTopicAdapter(tempSearchL)
                                } else {
                                    searchLmsFragView.gvSearch.visibility = View.GONE
                                }
                            }
                        }
                    }
                }
                else{

                    GlobalScope.launch(Dispatchers.IO) {
                        var tempSearchL = courseList
                        withContext(Dispatchers.Main) {
                            searchLmsFragView.progressWheel.stopSpinning()
                            setTopicAdapter(tempSearchL)
                        }
                    }
                }
            }

            override fun afterTextChanged(s: Editable) {

            }
        })
        searchLmsFragView.bottomLayoutLms.llLmsPerformance .setOnClickListener(this)
        searchLmsFragView.bottomLayoutLms.llLmsMylearning .setOnClickListener(this)
        searchLmsFragView.bottomLayoutLms.llLmsKnowledgehub .setOnClickListener(this)

    }

    override fun onItemClick(item: LmsSearchData) {
       /* if (lmsSearchAdapter.getSelectedPosition() == RecyclerView.NO_POSITION) {
            Toast.makeText(mContext, "Please select one topic", Toast.LENGTH_SHORT).show()
        } else {*/
            val selectedItem = item
            VideoPlayLMS.previousFrag = SearchLmsFrag.toString()
            VideoPlayLMS.loadedFrom = "SearchLmsFrag"
            Pref.videoCompleteCount = "0"

            (mContext as DashboardActivity).loadFrag(MyTopicsWiseContents.getInstance(selectedItem.searchid+"~"+selectedItem.courseName), MyTopicsWiseContents::class.java.name ,true )
       // }
    }

    fun getTopicL() {
        searchLmsFragView.progressWheel.spin()

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
                            (mContext as DashboardActivity).toolbarTitle.text = "My Topics"
                            searchLmsFragView.progressWheel.stopSpinning()

                            setTopicAdapter(courseList)
                        }else{
                            searchLmsFragView.progressWheel.stopSpinning()

                            Toast.makeText(mContext, getString(R.string.no_data_found), Toast.LENGTH_SHORT).show()

                        }
                    }, { error ->
                        searchLmsFragView.progressWheel.stopSpinning()

                        Toast.makeText(mContext, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show()
                    })
            )
        }
        catch (ex: Exception) {
            ex.printStackTrace()
            searchLmsFragView.progressWheel.stopSpinning()

            Toast.makeText(mContext, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {

            searchLmsFragView.bottomLayoutLms .llLmsMylearning .id -> {
                (mContext as DashboardActivity).loadFrag(SearchLmsFrag(),SearchLmsFrag::class.java.name)
            }

            searchLmsFragView.bottomLayoutLms.llLmsKnowledgehub.id -> {
                (mContext as DashboardActivity).loadFrag(SearchLmsKnowledgeFrag(),SearchLmsKnowledgeFrag::class.java.name)
            }
            searchLmsFragView.bottomLayoutLms.llLmsPerformance .id -> {
                (mContext as DashboardActivity).loadFrag(PerformanceInsightPage(),PerformanceInsightPage::class.java.name)
            }

            searchLmsFragView.llFragSearch .id -> {
                AppUtils.hideSoftKeyboard(mContext as DashboardActivity)
                if (!searchLmsFragView.etFragSearch.text.toString().trim().equals("")) {
                    GlobalScope.launch(Dispatchers.IO) {
                        searchLmsFragView.progressWheel.spin()

                        if (courseList.size>0) {
                            var tempSearchL = courseList.filter {
                                it.courseName.contains(
                                    searchLmsFragView.etFragSearch .text.toString().trim(), ignoreCase = true
                                )
                            }
                            withContext(Dispatchers.Main) {
                                searchLmsFragView.progressWheel.stopSpinning()

                                if (tempSearchL.size > 0) {
                                    searchLmsFragView.gvSearch.visibility = View.VISIBLE
                                    searchLmsFragView.progressWheel.stopSpinning()

                                    setTopicAdapter(tempSearchL)
                                } else {
                                    searchLmsFragView.gvSearch.visibility = View.GONE
                                    searchLmsFragView.progressWheel.stopSpinning()

                                }
                            }
                        }
                    }

                }
            }

            searchLmsFragView.ivFragSearchVoice .id ->{
                suffixText = searchLmsFragView.etFragSearch.text.toString().trim()
                startVoiceInput()
            }
        }
    }

    fun setTopicAdapter(list:List<LmsSearchData>) {
        searchLmsFragView.gvSearch.visibility =View.VISIBLE

        lmsSearchAdapter = LmsSearchAdapter(mContext, list, this.javaClass.simpleName,this)
        searchLmsFragView.gvSearch.adapter = lmsSearchAdapter

        searchLmsFragView.tvNextAfterSearchLms .setOnClickListener {
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
                    searchLmsFragView.etFragSearch .setText(suffixText+t)
                    searchLmsFragView.etFragSearch.setSelection(setFullText.length);
                }else{
                    var SuffixPostText = t+searchLmsFragView.etFragSearch.text.toString()
                    searchLmsFragView.etFragSearch.setText(SuffixPostText)
                    searchLmsFragView.etFragSearch.setSelection(SuffixPostText.length);
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
        (mContext as DashboardActivity).toolbarTitle.text = "My Topics"
    }
    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

}
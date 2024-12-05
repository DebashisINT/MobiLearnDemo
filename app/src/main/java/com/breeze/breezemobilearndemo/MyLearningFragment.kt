package com.breezemobilearndemo

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.breeze.breezemobilearndemo.CustomStatic
import com.breezemobilearndemo.api.LMSRepoProvider
import com.breezemobilearndemo.databinding.ActivityDashboardBinding
import com.breezemobilearndemo.databinding.FragmentMyLearningBinding
import com.breezemobilearndemo.domain.LMSNotiEntity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class MyLearningFragment : Fragment(), OnClickListener {

    private var binding : FragmentMyLearningBinding? = null
    private val homeView get() = binding!!

    private lateinit var mContext: Context
    lateinit var courseList: List<LmsSearchData>
    lateinit var courseListLearning: List<LmsSearchData>

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext=context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentMyLearningBinding.inflate(inflater,container,false)
        return homeView.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentMyLearningBinding.bind(view)
        initView()
    }

    private fun initView(){
        homeView.bottomLayoutLms.ivLmsPerformance.setImageResource(R.drawable.performance_insights)
        homeView.bottomLayoutLms.ivLmsMylearning .setImageResource(R.drawable.open_book_lms_)
        homeView.bottomLayoutLms.ivLmsKnowledgehub .setImageResource(R.drawable.set_of_books_lms)

        homeView.bottomLayoutLms.tvLmsPerformance .setTextColor(getResources().getColor(R.color.black))
        homeView.bottomLayoutLms.tvLmsMylearning .setTextColor(getResources().getColor(R.color.black))
        homeView.bottomLayoutLms.tvLmsKnowledgehub .setTextColor(getResources().getColor(R.color.black))

        homeView.cvLmsLearnerSpace .setOnClickListener {
            setHomeClickFalse()
            (mContext as DashboardActivity).loadFrag(SearchLmsFrag(), SearchLmsFrag::class.java.name )
        }
        homeView.llLmsDashPerformanceIns .setOnClickListener {
            setHomeClickFalse()
            (mContext as DashboardActivity).loadFrag(PerformanceInsightPage(), PerformanceInsightPage::class.java.name)
        }

        homeView.llFragSearchKnowledgeHubRoot .setOnClickListener(this)
        homeView.cvLmsLeaderboard .setOnClickListener(this)
        homeView.llLmsDashPerformanceIns  .setOnClickListener(this)
        homeView.llFragSearchMylearningRoot .setOnClickListener(this)
        homeView.cvFragSearchMylearningRoot .setOnClickListener(this)
        homeView.cvLmsBookmaark .setOnClickListener(this)
        homeView.bottomLayoutLms.llLmsMylearning .setOnClickListener(this)
        homeView.bottomLayoutLms.llLmsKnowledgehub .setOnClickListener(this)
        homeView.bottomLayoutLms.llLmsPerformance .setOnClickListener(this)

        if (AppUtils.isOnline(mContext)) {
            getTopicLAssigened()
            getTopicLearningAssigened()
            getTopicL()
        }else{
            Toast.makeText(mContext, ""+getString(R.string.no_internet), Toast.LENGTH_SHORT).show()
        }
        Handler().postDelayed(Runnable {
            if(Pref.FirstLogiForTheDayTag){
                Pref.FirstLogiForTheDayTag = false

                if(!Pref.LastVideoPlay_TopicName.equals("")){
                    VideoPlayLMS.loadedFrom = "LMSDASHBOARD"
                    CustomStatic.VideoPosition = Pref.LastVideoPlay_VidPosition.toInt()
                    Pref.videoCompleteCount = "0"
                    Handler().postDelayed(Runnable {
                        setHomeClickFalse()
                        (mContext as DashboardActivity).loadFrag(VideoPlayLMS.getInstance(Pref.LastVideoPlay_TopicID +"~"+ Pref.LastVideoPlay_TopicName),VideoPlayLMS::class.java.name, true)
                    }, 1000)
                }else{
                    Handler().postDelayed(Runnable {
                        VideoPlayLMS.loadedFrom = "LMSDASHBOARD"
                        gotoVideoPage()
                    }, 1000)

                }
            }
        }, 600)

        if (Pref.like_count!=0 || Pref.comment_count!=0 || Pref.share_count!=0 || Pref.correct_answer_count!=0 || Pref.wrong_answer_count!=0){
            contentCountSaveAPICalling()
        }
        homeView.tvSaveContent.setText(Pref.CurrentBookmarkCount.toString())
    }


    override fun onResume() {
        super.onResume()
        (mContext as DashboardActivity).toolbarTitle.text = "Home"
        try {
            var votVIwedL = AppDatabase.getDBInstance()!!.lmsNotiDao().getNotViwed(false) as ArrayList<LMSNotiEntity>
            if(votVIwedL.size !=0){
                (mContext as ActivityDashboardBinding).dashToolbar.tvNotiCount.visibility = View.VISIBLE
                (mContext as ActivityDashboardBinding).dashToolbar.tvNotiCount.text = votVIwedL.size.toString()
            }else{
                (mContext as ActivityDashboardBinding).dashToolbar.tvNotiCount.visibility = View.GONE
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            if (AppUtils.isOnline(mContext)) {
                (mContext as DashboardActivity).updateBookmarkCnt()
            }else{
                Toast.makeText(mContext, ""+getString(R.string.no_internet), Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Pref.CurrentBookmarkCount = 0
        }
    }

    private fun contentCountSaveAPICalling() {
        try {
            var obj = ContentCountSave_Data()
            obj.user_id = Pref.user_id.toString()
            obj.save_date = AppUtils.getCurrentDateyymmdd()
            obj.like_count = Pref.like_count
            obj.comment_count = Pref.comment_count
            obj.share_count = Pref.share_count
            obj.correct_answer_count = Pref.correct_answer_count
            obj.wrong_answer_count = Pref.wrong_answer_count
            obj.content_watch_count = Pref.content_watch_count

            val repository = LMSRepoProvider.getTopicList()
            DashboardActivity.compositeDisposable.add(
                repository.saveContentCount(obj)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ result ->
                        val response = result as BaseResponse
                        if (response.status == NetworkConstant.SUCCESS) {
                            try {
                                Pref.like_count = 0
                                Pref.comment_count = 0
                                Pref.share_count = 0
                                Pref.correct_answer_count = 0
                                Pref.wrong_answer_count = 0
                                Pref.content_watch_count = 0
                            }catch (ex:Exception){
                                ex.printStackTrace()
                            }
                        }else{
                            Toast.makeText(mContext, getString(R.string.no_data_found), Toast.LENGTH_SHORT).show()
                        }
                    }, { error ->
                        Toast.makeText(mContext, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show()
                    })
            )
        }
        catch (ex: Exception) {
            ex.printStackTrace()
            Toast.makeText(mContext, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show()
        }

    }

    fun getTopicLAssigened() {
        try {
            val repository = LMSRepoProvider.getTopicList()
            DashboardActivity.compositeDisposable.add(
                repository.getTopics(Pref.user_id!!)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ result ->
                        val response = result as TopicListResponse
                        if (response.status == NetworkConstant.SUCCESS) {
                            courseList = ArrayList<LmsSearchData>()
                            courseListLearning = ArrayList<LmsSearchData>()
                            for (i in 0..response.topic_list.size - 1) {
                                if (response.topic_list.get(i).video_count!= 0) {
                                    courseList = courseList + LmsSearchData(
                                        response.topic_list.get(i).topic_id.toString(),
                                        response.topic_list.get(i).topic_name
                                    )
                                }
                            }
                           homeView.tvContent.setText(courseList.size.toString())
                        }else{

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

    fun getTopicLearningAssigened() {
        try {
            val repository = LMSRepoProvider.getTopicList()
            DashboardActivity.compositeDisposable.add(
                repository.getTopics(Pref.user_id!!)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ result ->
                        val response = result as TopicListResponse
                        if (response.status == NetworkConstant.SUCCESS) {
                            courseList = ArrayList<LmsSearchData>()
                            courseListLearning = ArrayList<LmsSearchData>()
                            for (i in 0..response.topic_list.size - 1) {
                                if (response.topic_list.get(i).video_count!= 0) {
                                    if (response.topic_list.get(i).topic_parcentage!= 0) {
                                        courseListLearning = courseListLearning + LmsSearchData(
                                            response.topic_list.get(i).topic_id.toString(),
                                            response.topic_list.get(i).topic_name
                                        )
                                    }
                                }
                            }
                            homeView.tvContentLearning.setText(courseListLearning.size.toString()+" Topics")
                            CustomStatic.MyLearningTopicCount = courseListLearning.size

                        }else{

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

    fun getTopicL() {
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
                            for (i in 0..response.topic_list.size - 1) {
                                if (response.topic_list.get(i).video_count!= 0) {
                                    courseList = courseList + LmsSearchData(
                                        response.topic_list.get(i).topic_id.toString(),
                                        response.topic_list.get(i).topic_name
                                    )
                                }
                            }
                            homeView.tvContentKnowledge.setText(courseList.size.toString())
                        }else{
                            Toast.makeText(mContext, getString(R.string.no_data_found), Toast.LENGTH_SHORT).show()
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

    override fun onClick(v: View?) {
        when(v?.id){

            homeView.llFragSearchMylearningRoot.id -> {
                setHomeClickFalse()
                (mContext as DashboardActivity).loadFrag(SearchLmsFrag(), SearchLmsFrag::class.java.name)
            }

            homeView.llFragSearchKnowledgeHubRoot.id -> {
                setHomeClickFalse()
                (mContext as DashboardActivity).loadFrag( SearchLmsKnowledgeFrag(), SearchLmsKnowledgeFrag::class.java.name)
            }
            homeView.llLmsDashPerformanceIns.id -> {
                setHomeClickFalse()
                (mContext as DashboardActivity).loadFrag( PerformanceInsightPage(),PerformanceInsightPage::class.java.name)

            }
           homeView.cvFragSearchMylearningRoot .id -> {
                setHomeClickFalse()
                CustomStatic.LMSMyPerformanceFromMenu = false
                (mContext as DashboardActivity).loadFrag( MyPerformanceFrag(),MyPerformanceFrag::class.java.name )
            }
            homeView.cvLmsBookmaark.id -> {
                setHomeClickFalse()
                (mContext as DashboardActivity).loadFrag(  BookmarkFrag(),BookmarkFrag::class.java.name)
            }
            homeView.bottomLayoutLms.llLmsMylearning.id -> {
                setHomeClickFalse()
                (mContext as DashboardActivity).loadFrag(SearchLmsFrag(),SearchLmsFrag::class.java.name, true)
            }
            homeView.bottomLayoutLms.llLmsKnowledgehub.id -> {
                setHomeClickFalse()
                (mContext as DashboardActivity).loadFrag(SearchLmsKnowledgeFrag(),SearchLmsKnowledgeFrag::class.java.name, true)
            }
            homeView.bottomLayoutLms.llLmsPerformance.id -> {
                setHomeClickFalse()
                (mContext as DashboardActivity).loadFrag(PerformanceInsightPage(),PerformanceInsightPage::class.java.name, true)
            }
        }
    }

    fun gotoVideoPage() {
        try {
            val repository = LMSRepoProvider.getTopicList()
            DashboardActivity.compositeDisposable.add(
                repository.getTopics(Pref.user_id!!)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ result ->
                        val response = result as TopicListResponse
                        if (response.status == NetworkConstant.SUCCESS) {
                            if(response.topic_list != null){
                                try {
                                    var filterL =response.topic_list.filter { it.topic_parcentage != 100 }
                                    getVideoTopicWise(filterL.get(0))

                                }catch (e:Exception){
                                    e.printStackTrace()
                                }
                            }
                        }else{

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

    private fun getVideoTopicWise(topicList: TopicList) {

        try {
            val repository = LMSRepoProvider.getTopicList()
            DashboardActivity.compositeDisposable.add(
                repository.getTopicsWiseVideo(Pref.user_id!! , topicList.topic_id.toString())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ result ->
                        val response = result as VideoTopicWiseResponse
                        if (response.status == NetworkConstant.SUCCESS) {
                            try {
                                if (response.content_list != null && response.content_list.size > 0) {
                                    for (i in 0 .. response.content_list.size-1){
                                        CustomStatic.VideoPosition =  CustomStatic.VideoPosition+1
                                        if (response.content_list.get(i).Watch_Percentage != "100"){
                                            break
                                        }
                                    }
                                    VideoPlayLMS.previousFrag = SearchLmsFrag::class.java.simpleName.toString()
                                    VideoPlayLMS.loadedFrom = "LMSDASHBOARD"
                                    Pref.videoCompleteCount = "0"
                                    Handler().postDelayed(Runnable {
                                        setHomeClickFalse()
                                        (mContext as DashboardActivity).loadFrag(VideoPlayLMS.getInstance(topicList.topic_id.toString()+"~"+topicList.topic_name),VideoPlayLMS::class.java.name, true )
                                    }, 500)
                                } else {
                                    Toast.makeText(mContext, "No video found", Toast.LENGTH_SHORT)
                                        .show()

                                }
                            } catch (ex: Exception) {
                                ex.printStackTrace()
                            }
                        } else {

                            Toast.makeText(mContext, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show()

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


    fun setHomeClickFalse(){
        CustomStatic.IsHomeClick = false
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    fun callLastPlayedVideo() {

        try {
            if(!Pref.LastVideoPlay_TopicName.equals("")){
                homeView.cvLastVidRoot .visibility = View.VISIBLE

                homeView.tvFragMyLearningLastContentName .text = Pref.LastVideoPlay_ContentName
                homeView.tvFragMyLearningLastContentDesc .text = Pref.LastVideoPlay_ContentDesc

                if (!Pref.LastVideoPlay_BitmapURL.equals("")) {
                    Glide.with(mContext)
                        .load(Pref.LastVideoPlay_BitmapURL)
                        .apply(RequestOptions.placeholderOf(R.drawable.ic_image).error(R.drawable.ic_image))
                        .into(homeView.ivFragMyLearningLastTopicImg)
                }
                else{
                    homeView.ivFragMyLearningLastTopicImg.setImageResource(R.drawable.ic_image)
                }
            }else{
                homeView.cvLastVidRoot .visibility = View.GONE
            }

            homeView.cvLastVidRoot.setOnClickListener {
                setHomeClickFalse()
                VideoPlayLMS.loadedFrom = "LMSDASHBOARD"
                CustomStatic.VideoPosition = Pref.LastVideoPlay_VidPosition.toInt()
                Pref.videoCompleteCount = "0"
                (mContext as DashboardActivity).loadFrag(VideoPlayLMS.getInstance(Pref.LastVideoPlay_TopicID +"~"+ Pref.LastVideoPlay_TopicName),VideoPlayLMS::class.java.name, true)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


}
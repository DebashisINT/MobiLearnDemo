package com.breezemobilearndemo

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.breeze.breezemobilearndemo.CustomStatic
import com.breezemobilearndemo.api.LMSRepoProvider
import com.breezemobilearndemo.databinding.FragmentMyPerformanceBinding
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.time.Duration
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class MyPerformanceFrag : Fragment(), View.OnClickListener {
    private var binding : FragmentMyPerformanceBinding? = null
    private val myPerformanceView get() = binding!!

    private lateinit var mContext: Context

    private lateinit var totalTimeString: String
    private lateinit var averageTimeString: String
    private lateinit var totalTimeString1: String
    private lateinit var averageTimeString1: String
    lateinit var courseList: List<LmsSearchData>
    var str_filtertopicID: String=""
    var str_filtertopicname: String=""
    var str_filtertopicParcentage: Int=0

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext=context
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentMyPerformanceBinding.inflate(inflater,container,false)

        return myPerformanceView.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (mContext as Activity).requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        if (AppUtils.isOnline(mContext)) {
            getTopicL()
            getMyLarningInfoAPI("0", "All")
            myPerformanceView.tvFilterTopicName.text = "All"
        }
        else{
            Toast.makeText(mContext, getString(R.string.no_internet), Toast.LENGTH_SHORT).show()
        }
        initView()
    }

    fun getTopicL() {
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
                            for (i in 0..response.topic_list.size - 1) {
                                if (response.topic_list.get(i).video_count!= 0) {
                                    courseList = courseList + LmsSearchData(
                                        response.topic_list.get(i).topic_id.toString(),
                                        response.topic_list.get(i).topic_name,
                                        response.topic_list.get(i).video_count,
                                        response.topic_list.get(i).topic_parcentage
                                    )
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

    private fun getMyLarningInfoAPI(str_filtertopicID: String, str_filtertopicname: String) {
        try {
            val repository = LMSRepoProvider.getTopicList()
            DashboardActivity.compositeDisposable.add(
                repository.getMyLraningInfo(
                    Pref.user_id!!
                )
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ result ->
                        val response = result as MyLarningListResponse
                        if (response.status == NetworkConstant.SUCCESS) {
                            try {
                                if (response.learning_content_info_list != null && response.learning_content_info_list.size > 0) {

                                    if (!str_filtertopicID.equals("0") && !str_filtertopicname.equals("All")) {

                                        var topic_name_wise_filter =
                                            response.learning_content_info_list.filter { it.topic_name.equals(str_filtertopicname) && it.topic_id.equals(str_filtertopicID.toInt()) }

                                        val totalDuration1 = topic_name_wise_filter
                                            .map { stringToDuration(it.WatchedDuration) }
                                            .reduce { acc, duration -> acc.plus(duration) }

                                        totalTimeString1 = String.format(
                                            "%02d:%02d:%02d",
                                            totalDuration1.toHours(),
                                            totalDuration1.toMinutes() % 60,
                                            totalDuration1.seconds % 60
                                        )

                                        val averageSeconds1 =
                                            totalDuration1.seconds / topic_name_wise_filter.size
                                        val averageDuration1 = Duration.ofSeconds(averageSeconds1)

                                        averageTimeString1 = String.format(
                                            "%02d:%02d:%02d",
                                            averageDuration1.toHours(),
                                            averageDuration1.toMinutes() % 60,
                                            averageDuration1.seconds % 60
                                        )

                                        myPerformanceView.avgHrOfLrng.text = averageTimeString1
                                        myPerformanceView.tvHourOfLearning.text = totalTimeString1
                                    }
                                    else{
                                        val totalDuration = response.learning_content_info_list
                                            .map { stringToDuration(it.WatchedDuration) }
                                            .reduce { acc, duration -> acc.plus(duration) }

                                        totalTimeString = String.format(
                                            "%02d:%02d:%02d",
                                            totalDuration.toHours(),
                                            totalDuration.toMinutes() % 60,
                                            totalDuration.seconds % 60
                                        )

                                        val averageSeconds = totalDuration.seconds / response.learning_content_info_list.size
                                        val averageDuration = Duration.ofSeconds(averageSeconds)
                                        averageTimeString = String.format(
                                            "%02d:%02d:%02d",
                                            averageDuration.toHours(),
                                            averageDuration.toMinutes() % 60,
                                            averageDuration.seconds % 60
                                        )

                                        myPerformanceView.avgHrOfLrng .text = averageTimeString
                                        myPerformanceView.tvHourOfLearning.text = totalTimeString
                                    }

                                } else {

                                }
                            } catch (ex: Exception) {
                                ex.printStackTrace()
                            }
                        } else {
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

    fun stringToDuration(timeString: String): Duration {
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
        val localTime = LocalTime.parse(timeString, timeFormatter)
        return Duration.ofHours(localTime.hour.toLong())
            .plusMinutes(localTime.minute.toLong())
            .plusSeconds(localTime.second.toLong())
    }

    private fun initView() {

        myPerformanceView.includeBottomTabLms.ivLmsPerformance.setImageResource(R.drawable.performance_insights_checked)
        myPerformanceView.includeBottomTabLms.ivLmsMylearning.setImageResource(R.drawable.open_book_lms_)
        myPerformanceView.includeBottomTabLms.ivLmsKnowledgehub.setImageResource(R.drawable.set_of_books_lms)

        myPerformanceView.includeBottomTabLms.tvLmsPerformance.setTextColor(getResources().getColor(R.color.toolbar_lms))
        myPerformanceView.includeBottomTabLms.tvLmsMylearning.setTextColor(getResources().getColor(R.color.black))
        myPerformanceView.includeBottomTabLms.tvLmsKnowledgehub.setTextColor(getResources().getColor(R.color.black))

        overAllAPI()

        myPerformanceView.includeBottomTabLms.llLmsPerformance.setOnClickListener(this)
        myPerformanceView.includeBottomTabLms.llLmsMylearning.setOnClickListener(this)
        myPerformanceView.includeBottomTabLms.llLmsKnowledgehub.setOnClickListener(this)
        myPerformanceView.cvLmsLeaderboard.setOnClickListener(this)
        myPerformanceView.llFilter.setOnClickListener(this)

        if (!myPerformanceView.tvLeaderRank.text.toString().equals("")) {
            val fullText = myPerformanceView.tvLeaderRank.text.toString()
            val parts = fullText.split("/")

            val largeText = parts[0]
            val smallText = parts[1]
            val spannableString = SpannableString(fullText)
            spannableString.setSpan(
                RelativeSizeSpan(1.3f),
                0,
                largeText.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannableString.setSpan(
                StyleSpan(Typeface.BOLD),
                0,
                largeText.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannableString.setSpan(
                RelativeSizeSpan(1.0f),
                largeText.length,
                smallText.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            myPerformanceView.tvLeaderRank.text = spannableString
        }

        if (AppUtils.isOnline(mContext)) {
            myPerformanceView.llMyPerformance.visibility =View.VISIBLE

        }else{
            myPerformanceView.llMyPerformance.visibility =View.INVISIBLE
        }

    }

    override fun onResume() {
        super.onResume()
        (mContext as DashboardActivity).toolbarTitle.text = "My Performance"
    }

    private fun overAllAPI() {
        val repository = LMSRepoProvider.getTopicList()
        DashboardActivity.compositeDisposable.add(
            repository.overAllAPI(Pref.user_id!!,"","M")
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ result ->
                    if(result.status == NetworkConstant.SUCCESS){
                        if (result.user_list.size!=null || result.user_list.size > 0) {
                            val ownObj =
                                result.user_list.filter { it.user_id == Pref.user_id!!.toInt() }
                                    .first()
                            myPerformanceView.tvLeaderRank.text =
                                ownObj.position.toString() + "/" + result.user_list.size
                        }
                    }else{
                        Toast.makeText(mContext, result.message.toString(), Toast.LENGTH_SHORT).show()
                    }
                }, { error ->
                    error.printStackTrace()
                    Toast.makeText(mContext, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show()
                })
        )
    }

    companion object {

        fun getInstance(objects: Any): MyPerformanceFrag {
            val fragment = MyPerformanceFrag()
            return fragment
        }
    }


    override fun onClick(p0: View?) {
        when(p0?.id) {
            myPerformanceView.includeBottomTabLms.llLmsMylearning.id -> {
                (mContext as DashboardActivity).loadFrag(
                    SearchLmsFrag(),SearchLmsFrag::class.java.name, true)
            }
            myPerformanceView.includeBottomTabLms.llLmsKnowledgehub.id -> {
                (mContext as DashboardActivity).loadFrag(
                    SearchLmsKnowledgeFrag(),SearchLmsKnowledgeFrag::class.java.name, true)
            }
            myPerformanceView.includeBottomTabLms.llLmsPerformance.id -> {
                (mContext as DashboardActivity).loadFrag(PerformanceInsightPage(),PerformanceInsightPage::class.java.name, true)

            }
            myPerformanceView.cvLmsLeaderboard.id -> {
                LeaderboardLmsFrag.loadedFrom = "MyPerformanceFrag"
                CustomStatic.LMSLeaderboardFromMenu = false
                (mContext as DashboardActivity).loadFrag(LeaderboardLmsFrag(),LeaderboardLmsFrag::class.java.name, true)
            }
            myPerformanceView.llFilter.id -> {
                genericDialogOfTopicList()
            }
        }
    }

    private fun genericDialogOfTopicList() {

        if(courseList.size>0){
            val allOption = LmsSearchData( "0","All")
            val updatedCourseList = courseList.toMutableList()
            updatedCourseList.add(0, allOption)


            var genericL : ArrayList<CustomDataLms> = ArrayList()
            for(i in 0..updatedCourseList.size-1){
                genericL.add(CustomDataLms(updatedCourseList.get(i).searchid.toString(),updatedCourseList.get(i).courseName.toString(),updatedCourseList.get(i).topic_parcentage))
            }
            GenericDialogLMS.newInstance("Topic",genericL as ArrayList<CustomDataLms>){
                str_filtertopicID = it.id
                str_filtertopicname = it.name
                str_filtertopicParcentage = it.topic_parcentage

                myPerformanceView.tvFilterTopicName.setText(it.name)
                if (str_filtertopicname.toString().equals("All")){
                    myPerformanceView.avgHrOfLrng.text = averageTimeString
                    myPerformanceView.tvHourOfLearning.text = totalTimeString
                }else{
                    if (str_filtertopicParcentage!=0) {
                        getMyLarningInfoAPI(str_filtertopicID, str_filtertopicname)
                    }
                    else{
                        myPerformanceView.tvHourOfLearning.text = "00:00:00"
                        myPerformanceView.avgHrOfLrng.text = "00:00:00"
                    }
                }

            }.show((mContext as DashboardActivity).supportFragmentManager, "")
        }else{
            Toast.makeText(mContext, "No Duration Found", Toast.LENGTH_SHORT).show()
        }

    }


}
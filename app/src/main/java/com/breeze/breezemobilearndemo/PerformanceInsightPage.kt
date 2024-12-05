package com.breezemobilearndemo

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import com.breeze.breezemobilearndemo.CustomStatic
import com.breezemobilearndemo.databinding.FragmentPerformanceInsightPageBinding

class PerformanceInsightPage : Fragment(), OnClickListener {
    private var binding : FragmentPerformanceInsightPageBinding? = null
    private val performanceInsightPageView get() = binding!!

    private lateinit var mContext: Context

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PerformanceInsightPage().apply {

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
        binding = FragmentPerformanceInsightPageBinding.inflate(inflater,container,false)
        return performanceInsightPageView.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {

        performanceInsightPageView.includeBottomTablayoutlms.ivLmsPerformance.setImageResource(R.drawable.performance_insights_checked)
        performanceInsightPageView.includeBottomTablayoutlms.ivLmsMylearning.setImageResource(R.drawable.open_book_lms_)
        performanceInsightPageView.includeBottomTablayoutlms.ivLmsKnowledgehub.setImageResource(R.drawable.all_topics_selected)

        performanceInsightPageView.includeBottomTablayoutlms.tvLmsPerformance.setTextColor(getResources().getColor(R.color.toolbar_lms))
        performanceInsightPageView.includeBottomTablayoutlms.tvLmsMylearning.setTextColor(getResources().getColor(R.color.black))
        performanceInsightPageView.includeBottomTablayoutlms.tvLmsKnowledgehub.setTextColor(getResources().getColor(R.color.black))

        performanceInsightPageView.includeBottomTablayoutlms.llLmsPerformance.setOnClickListener(this)
        performanceInsightPageView.includeBottomTablayoutlms.llLmsMylearning.setOnClickListener(this)
        performanceInsightPageView.includeBottomTablayoutlms.llLmsKnowledgehub.setOnClickListener(this)
        performanceInsightPageView.llFragSearchMylearningRoot.setOnClickListener(this)
        performanceInsightPageView.cvLmsLeaderboard.setOnClickListener(this)
        performanceInsightPageView.cvFragSearchMyperformRoot.setOnClickListener(this)

        performanceInsightPageView.tvFragPerfMyLearningCnt.text = CustomStatic.MyLearningTopicCount.toString()

    }

    override fun onClick(v: View?) {
        when(v?.id){
            performanceInsightPageView.cvLmsLeaderboard.id -> {
                setHomeClickFalse()
                LeaderboardLmsFrag.loadedFrom = "PerformanceInsightPage"
                CustomStatic.LMSLeaderboardFromMenu = false
                (mContext as DashboardActivity).loadFrag(LeaderboardLmsFrag(),LeaderboardLmsFrag::class.java.name, true)
            }

            performanceInsightPageView.includeBottomTablayoutlms.llLmsMylearning.id -> {
                (mContext as DashboardActivity).loadFrag(SearchLmsFrag(),SearchLmsFrag::class.java.name, true)
            }
            performanceInsightPageView.includeBottomTablayoutlms.llLmsKnowledgehub.id -> {
                (mContext as DashboardActivity).loadFrag(SearchLmsKnowledgeFrag(),SearchLmsKnowledgeFrag::class.java.name,true)
            }
            performanceInsightPageView.includeBottomTablayoutlms.llLmsPerformance.id -> {
                (mContext as DashboardActivity).loadFrag(PerformanceInsightPage(), PerformanceInsightPage::class.java.name , true)
            }
            performanceInsightPageView.cvFragSearchMyperformRoot.id -> {
                setHomeClickFalse()
                CustomStatic.LMSMyPerformanceFromMenu = false
                (mContext as DashboardActivity).loadFrag(MyPerformanceFrag(),MyPerformanceFrag::class.java.name, true)
            }
            performanceInsightPageView.llFragSearchMylearningRoot.id -> {
                setHomeClickFalse()
                (mContext as DashboardActivity).loadFrag(MyLearningTopicList(), MyLearningTopicList::class.java.name,true)
            }
        }

    }

    fun setHomeClickFalse(){
        CustomStatic.IsHomeClick = false
    }

    override fun onResume() {
        super.onResume()
        (mContext as DashboardActivity).toolbarTitle.text = "Performance Insights"
    }

}
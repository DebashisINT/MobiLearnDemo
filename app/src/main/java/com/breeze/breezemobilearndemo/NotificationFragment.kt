package com.breezemobilearndemo

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.breezemobilearndemo.databinding.FragmentNotificationBinding
import com.breezemobilearndemo.domain.LMSNotiEntity

class NotificationFragment : Fragment(), View.OnClickListener {
    private var binding : FragmentNotificationBinding? = null
    private val notificationView get() = binding!!
    private lateinit var mContext: Context
    private lateinit var recyclerView: RecyclerView
    private lateinit var lmsNotiFilterData: ArrayList<LMSNotiFilterData>



    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext=context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentNotificationBinding.inflate(inflater,container,false)
        return notificationView.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        initView(view)
    }

    private fun initView(view: View) {
        AppDatabase.getDBInstance()!!.lmsNotiDao().updateISViwed(true)
        notificationView.includeBottomTabLms.ivLmsPerformance.setImageResource(R.drawable.performance_insights)
        notificationView.includeBottomTabLms.ivLmsMylearning.setImageResource(R.drawable.open_book_lms_)
        notificationView.includeBottomTabLms.ivLmsKnowledgehub.setImageResource(R.drawable.set_of_books_lms)
        notificationView.includeBottomTabLms.llLmsPerformance.setOnClickListener(this)
        notificationView.includeBottomTabLms.llLmsMylearning.setOnClickListener(this)
        notificationView.includeBottomTabLms.llLmsKnowledgehub.setOnClickListener(this)

        recyclerView = notificationView.rvNotification

        try {
            lmsNotiFilterData = ArrayList()
            var dateL =
                AppDatabase.getDBInstance()!!.lmsNotiDao().getDistinctDate() as ArrayList<String>
            for (i in 0..dateL.size - 1) {
                var obj: LMSNotiFilterData = LMSNotiFilterData()
                obj.noti_date = dateL.get(i)
                obj.notiL = AppDatabase.getDBInstance()!!.lmsNotiDao()
                    .getNotiByDate(dateL.get(i)) as ArrayList<LMSNotiEntity>
                lmsNotiFilterData.add(obj)
            }

            val headerAdapter = HeaderAdapterLMSNoti(lmsNotiFilterData)
            recyclerView.layoutManager = LinearLayoutManager(mContext)
            recyclerView.adapter = headerAdapter
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            notificationView.includeBottomTabLms.llLmsMylearning.id -> {
                (mContext as DashboardActivity).loadFrag(
                    SearchLmsFrag(),SearchLmsFrag::class.java.name, true)
            }

            notificationView.includeBottomTabLms.llLmsKnowledgehub.id -> {
                (mContext as DashboardActivity).loadFrag(
                    SearchLmsKnowledgeFrag(),SearchLmsKnowledgeFrag::class.java.name, true)
            }

            notificationView.includeBottomTabLms.llLmsPerformance.id -> {
                (mContext as DashboardActivity).loadFrag(PerformanceInsightPage(),PerformanceInsightPage::class.java.name, true)
            }

        }
    }

    override fun onResume() {
        super.onResume()
        (mContext as DashboardActivity).toolbarTitle.text = "Notifications"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
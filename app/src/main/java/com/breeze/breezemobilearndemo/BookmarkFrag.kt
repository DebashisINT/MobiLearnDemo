package com.breezemobilearndemo

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.breezemobilearndemo.api.LMSRepoProvider
import com.breezemobilearndemo.databinding.FragmentBookmarkBinding
import com.breezemobilearndemo.databinding.FragmentMyLearningBinding
import com.breezemobilearndemo.databinding.FragmentVideoPlayLMSBinding
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class BookmarkFrag : Fragment() {

    private var binding : FragmentBookmarkBinding? = null
    private val bookmarkView get() = binding!!
    private lateinit var mContext: Context
    private lateinit var response: BookmarkFetchResponse
    private lateinit var adapterBookmarked:AdapterBookmarkedprivate

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext=context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentBookmarkBinding.inflate(inflater,container,false)
        return bookmarkView.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        initView()
    }

    private fun initView() {

        bookmarkView.progressWheelBookmark.stopSpinning()
        getBookmarked()
    }

    private fun getBookmarked(){
        try {
            bookmarkView.progressWheelBookmark.spin()
            val repository = LMSRepoProvider.getTopicList()
            DashboardActivity.compositeDisposable.add(
                repository.getBookmarkedApiCall(Pref.user_id.toString())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ result ->
                        response = result as BookmarkFetchResponse
                        if (response.status == NetworkConstant.SUCCESS) {
                            bookmarkView.rvFragBookmark.visibility = View.VISIBLE
                            bookmarkView.ivNoData.visibility = View.GONE
                            bookmarkView.progressWheelBookmark.stopSpinning()
                            (mContext as DashboardActivity).toolbarTitle.text = "Saved Contents : (${response.bookmark_list.size})"
                            showData(response.bookmark_list)

                            try {
                                (mContext as DashboardActivity).updateBookmarkCnt()
                            } catch (e: Exception) {
                                Pref.CurrentBookmarkCount = 0
                            }
                        } else {
                            bookmarkView.rvFragBookmark.visibility = View.GONE
                            bookmarkView.ivNoData.visibility = View.VISIBLE
                            bookmarkView.progressWheelBookmark.stopSpinning()
                            try {
                                (mContext as DashboardActivity).updateBookmarkCnt()
                            } catch (e: Exception) {
                                Pref.CurrentBookmarkCount = 0
                            }
                            (mContext as DashboardActivity).toolbarTitle.text = "Saved Contents"
                        }
                    }, { error ->
                        error.printStackTrace()
                        bookmarkView.rvFragBookmark.visibility = View.GONE
                        bookmarkView.ivNoData.visibility = View.GONE
                        bookmarkView.progressWheelBookmark.stopSpinning()
                    })
            )
        } catch (ex: Exception) {
            ex.printStackTrace()
            bookmarkView.rvFragBookmark.visibility = View.GONE
            bookmarkView.ivNoData.visibility = View.GONE
            bookmarkView.progressWheelBookmark.stopSpinning()
        }
    }

    fun showData(bookmark_list:ArrayList<VidBookmark>){
        var filterL = bookmark_list.distinctBy { it.content_id.toString() }
        adapterBookmarked = AdapterBookmarkedprivate(mContext,filterL as ArrayList<VidBookmark>,object :AdapterBookmarkedprivate.OnClick{
            override fun onClick(obj: VidBookmark) {
                BookmarkPlayFrag.play_url = obj.content_url
                (mContext as DashboardActivity).loadFrag(BookmarkPlayFrag(),BookmarkPlayFrag::class.java.name, true)
            }

            override fun onDelClick(obj: VidBookmark) {
                obj.isBookmarked = "0"
                bookmarkDelApi(obj)
            }
        })
        bookmarkView.rvFragBookmark.adapter = adapterBookmarked
    }

    private fun bookmarkDelApi(obj:VidBookmark){
        var apiObj : BookmarkResponse = BookmarkResponse()
        apiObj.user_id = Pref.user_id.toString()
        apiObj.topic_id = obj.topic_id
        apiObj.topic_name = obj.topic_name
        apiObj.content_id = obj.content_id
        apiObj.content_name = obj.content_name
        apiObj.content_desc = obj.content_desc
        apiObj.content_bitmap = obj.content_bitmap
        apiObj.content_url = obj.content_url
        apiObj.addBookmark = obj.isBookmarked

        try {
            val repository = LMSRepoProvider.getTopicList()
            DashboardActivity.compositeDisposable.add(
                repository.bookmarkApiCall(apiObj)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ result ->
                        var response = result as BaseResponse
                        if (response.status == NetworkConstant.SUCCESS) {
                            getBookmarked()
                        } else {

                        }
                    }, { error ->
                        error.printStackTrace()
                    })
            )
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun updateToolbar() {
        super.onResume()
        try {
            (mContext as DashboardActivity).toolbarTitle.text = "Saved Contents : (${response.bookmark_list.size})"
        }catch (ex:Exception){
            ex.printStackTrace()
            (mContext as DashboardActivity).toolbarTitle.text = "Saved Contents"
        }

        try {
            (mContext as DashboardActivity).updateBookmarkCnt()
        } catch (e: Exception) {
            Pref.CurrentBookmarkCount = 0
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}
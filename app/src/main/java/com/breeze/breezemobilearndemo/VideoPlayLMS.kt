package com.breezemobilearndemo

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.viewpager2.widget.ViewPager2
import com.airbnb.lottie.LottieAnimationView
import com.breeze.breezemobilearndemo.CustomStatic
import com.breezemobilearndemo.api.LMSRepoProvider
import com.breezemobilearndemo.databinding.FragmentVideoPlayLMSBinding
import com.breezemobilearndemo.features.mylearning.VideoAdapter
import com.breezemobilearndemo.features.mylearning.VideoAdapter1
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers


class VideoPlayLMS : Fragment() {
    private var binding : FragmentVideoPlayLMSBinding? = null
    private val videoPlayLMSView get() = binding!!
    private lateinit var mContext: Context
    private lateinit var adapter: VideoAdapter
    private lateinit var adapter1: VideoAdapter1
    private val exoPlayerItems = ArrayList<ExoPlayerItem>()
    var contentL: ArrayList<ContentL> = ArrayList()
    private var content_comment_point: Int = 0
    private var content_share_point: Int = 0
    private var content_like_point: Int = 0
    private var content_watch_point: Int = 0
    var current_lms_video_obj: LMS_CONTENT_INFO = LMS_CONTENT_INFO()

    var onActivityStateChanged: VideoAdapter.OnActivityStateChanged? = null
    var onActivityStateChanged1: VideoAdapter1.OnActivityStateChanged? = null
    private lateinit var screenOffReceiver: BroadcastReceiver
    lateinit var currentVideoObj : ContentL
    var like_flag = false
    var Obj_LMS_CONTENT_INFO: LMS_CONTENT_INFO = LMS_CONTENT_INFO()
    var commentL: ArrayList<CommentL> = ArrayList()
    private lateinit var popupWindow: PopupWindow
    private lateinit var question_ans_setL :ArrayList<QuestionL>
    private lateinit var cmtAdapter: AdapterComment
    private lateinit var response: VideoTopicWiseResponse
    private var isReceiverRegistered = false


    companion object {

        var sequenceQuestionL :ArrayList<SequenceQuestion> = ArrayList()

        var loadedFrom:String = ""
        var topic_id: String = ""
        var topic_name: String = ""
        var previousFrag: String = ""
        var content_position: Int = 0
        var lastvideo: Boolean = false
        var lastvideo_: Boolean = false
        fun getInstance(objects: Any): VideoPlayLMS {
            val videoPlayLMS = VideoPlayLMS()

            if (!TextUtils.isEmpty(objects.toString())) {
                val parts = objects.toString().split("~")
                topic_id = parts[0]
                topic_name = parts[1]
            } else {
                topic_id = ""
                topic_name = ""

            }
            println("tag_topic_id" + topic_id)

            val bundle = Bundle()
            bundle.putBoolean("LAST_VIDEO", lastvideo)
            videoPlayLMS.arguments = bundle
            return videoPlayLMS
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
        binding = FragmentVideoPlayLMSBinding.inflate(inflater,container,false)
        return videoPlayLMSView.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        initView()
    }

    private fun initView() {

        videoPlayLMSView.exoFullscreen.setBackgroundResource(R.drawable.switch_to_full_screen_button);

        videoPlayLMSView.llVideoNotFound.visibility = View.GONE
        videoPlayLMSView.llFragVideoPlayComments .visibility = View.GONE
        videoPlayLMSView.llFragVidBookmark.visibility = View.VISIBLE
        videoPlayLMSView.lottieBookmark.visibility = View.GONE

        getPointsAPICalling()

        contentL = ArrayList()

        videoPlayLMSView.ivFragVideoCommentHide.setOnClickListener {
            videoPlayLMSView.llFragVideoPlayComments.visibility = View.GONE
        }

        if (topic_id != "") {
            getVideoTopicWise()
            return
        }

        videoPlayLMSView.llVdoPlyShare.setOnClickListener {

        }

    }


    fun getVideoTopicWise() {
        try {
            videoPlayLMSView.progressWheel.spin()
            val repository = LMSRepoProvider.getTopicList()
            DashboardActivity.compositeDisposable.add(
                repository.getTopicsWiseVideo(Pref.user_id!!,topic_id)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ result ->
                        videoPlayLMSView.progressWheel.stopSpinning()
                        response = result as VideoTopicWiseResponse
                        if (response.status == NetworkConstant.SUCCESS) {
                            try {
                                if (response.content_list != null && response.content_list.size > 0) {
                                    videoPlayLMSView.llVideoNotFound .visibility = View.GONE

                                    var temp  = response.content_list.distinctBy { it.content_play_sequence.toString() }
                                    contentL = temp as ArrayList<ContentL>
                                    val sortedList = contentL.sortedBy { it.content_play_sequence.toInt() }.toCollection(ArrayList())
                                    sequenceQuestionL = ArrayList()
                                    try {
                                        for (i in 0.. sortedList.size-1){
                                            var rootObj : SequenceQuestion = SequenceQuestion()
                                            rootObj.index = i+1
                                            rootObj.completionStatus = sortedList.get(i).CompletionStatus
                                            rootObj.question_list = sortedList.get(i).question_list
                                            sequenceQuestionL.add(rootObj)
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        sequenceQuestionL = ArrayList()
                                    }
                                    var kk = sequenceQuestionL
                                    setVideoAdapter(
                                        sortedList, topic_id, topic_name, content_position,
                                        videoPlayLMSView.llVdoPlyLike,
                                        videoPlayLMSView.llFragVideoPlayComments,
                                        videoPlayLMSView.llVdoPlyShare,
                                        videoPlayLMSView.ivVdoPlyLike,
                                        content_watch_point,
                                        videoPlayLMSView.exoFullscreen,
                                    )

                                } else {
                                    Toast.makeText(mContext, "No video found", Toast.LENGTH_SHORT)
                                        .show()
                                    videoPlayLMSView.llVideoNotFound .visibility = View.VISIBLE
                                }
                            } catch (ex: Exception) {
                                ex.printStackTrace()
                            }
                        } else {
                            videoPlayLMSView.llVideoNotFound .visibility = View.VISIBLE
                            Toast.makeText(mContext, getString(R.string.no_data_found), Toast.LENGTH_SHORT).show()

                        }
                    }, { error ->
                        videoPlayLMSView.llVideoNotFound .visibility = View.GONE
                        Toast.makeText(mContext, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show()
                    })
            )
        } catch (ex: Exception) {
            ex.printStackTrace()
            videoPlayLMSView.progressWheel.stopSpinning()
            videoPlayLMSView.llVideoNotFound .visibility = View.GONE
            Toast.makeText(mContext, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show()
        }
    }

    fun setVideoAdapter(
        contentL: ArrayList<ContentL>,
        topic_id: String,
        topic_name: String,
        content_position: Int,
        ll_vdo_ply_like: LinearLayout,
        ll_vdo_ply_cmmnt: LinearLayout,
        ll_vdo_ply_share: LinearLayout,
        iv_vdo_ply_like: ImageView,
        content_watch_point: Int,
        exo_fullscreen: ImageView
    ) {

        if (Pref.IsVideoAutoPlayInLMS) {
            adapter = VideoAdapter(
                videoPlayLMSView.viewPager2,
                mContext,
                contentL,
                topic_id,
                topic_name,
                Companion.content_position,
                ll_vdo_ply_like,
                ll_vdo_ply_cmmnt,
                ll_vdo_ply_share,
                iv_vdo_ply_like,
                videoPlayLMSView.ivVdoPlyBookmark,
                exo_fullscreen,
                object : VideoAdapter.OnVideoPreparedListener {

                    override fun onLikeClick(isLike: Boolean) {

                        onLikeClickFun(isLike)

                    }

                    override fun onBookmarkClick() {

                        onBookmarkClickFun()

                    }

                    override fun onEndofVidForCountUpdate() {
                        contentCountSaveAPICalling()
                    }

                    override fun onVideoPrepared(exoPlayerItem: ExoPlayerItem) {
                        exoPlayerItems.add(exoPlayerItem)
                    }

                    override fun onNonVideo() {

                    }

                    override fun onContentInfoAPICalling(obj: LMS_CONTENT_INFO) {

                        onContentInfoAPICallingFun(obj)

                    }

                    override fun onCommentCLick(obj: ContentL) {

                    }

                    override fun onShareClick(obj: ContentL) {

                    }

                    @SuppressLint("SuspiciousIndentation")
                    override fun onQuestionAnswerSetPageLoad(
                        obj: ArrayList<QuestionL>,
                        position: Int
                    ) {

                        onQuestionAnswerSetPageLoadFun(position)

                    }
                },
                object : VideoAdapter.OnLastVideoCompleteListener {
                    override fun onLastVideoComplete() {

                        onLastVideoCompleteFun()

                    }
                },
                content_watch_point
            )

            videoPlayLMSView.viewPager2.adapter = adapter

            onActivityStateChanged = adapter.registerActivityState()

            if (!isReceiverRegistered) {

                screenOffReceiver = object : BroadcastReceiver() {
                    override fun onReceive(context: Context?, intent: Intent?) {
                        if (intent?.action == Intent.ACTION_SCREEN_OFF) {
                            adapter.pauseCurrentVideo()
                        }
                    }
                }

                val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)
                requireActivity().registerReceiver(screenOffReceiver, filter)

                isReceiverRegistered = true
            }


            videoPlayLMSView.viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    adapter.updateCurrentPosition(position)
                    Pref.videoCompleteCount = (position + 1).toString()
                    try {
                        Pref.LastVideoPlay_TopicID = topic_id
                        Pref.LastVideoPlay_TopicName = topic_name
                        Pref.LastVideoPlay_VidPosition = position.toString()
                        Pref.LastVideoPlay_BitmapURL =
                            contentL.get(position).content_thumbnail.toString()
                        Pref.LastVideoPlay_ContentID = contentL.get(position).content_id
                        Pref.LastVideoPlay_ContentName = contentL.get(position).content_title
                        Pref.LastVideoPlay_ContentDesc =
                            contentL.get(position).content_description
                        Pref.LastVideoPlay_ContentParcent =
                            contentL.get(position).Watch_Percentage

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    currentVideoObj = contentL.get(position)

                    try {
                        if (currentVideoObj.isBookmarked == null) {
                            currentVideoObj.isBookmarked = "0"
                        }
                    } catch (e: Exception) {
                        currentVideoObj.isBookmarked = "0"
                    }

                    try {
                        if (currentVideoObj.isBookmarked.equals("1")) {
                            videoPlayLMSView.llBook.visibility = View.VISIBLE
                            videoPlayLMSView.llBook.setBackground(
                                mContext.getResources()
                                    .getDrawable(R.drawable.back_round_corner_lms_round_white)
                            );
                            videoPlayLMSView.ivVdoPlyBookmark.setImageResource(R.drawable.bookmark_green)
                            videoPlayLMSView.tvFragVidBookmarkText.text = "Saved"
                        } else {
                            videoPlayLMSView.llBook.visibility = View.VISIBLE
                            videoPlayLMSView.llBook.setBackground(
                                mContext.getResources()
                                    .getDrawable(R.drawable.back_round_corner_lms_round)
                            );
                            videoPlayLMSView.ivVdoPlyBookmark.setImageResource(R.drawable.save_instagram)
                            videoPlayLMSView.tvFragVidBookmarkText.text = "Save"
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    if (contentL.get(position).isAllowLike) {
                        ll_vdo_ply_like.visibility = View.VISIBLE
                    } else {
                        ll_vdo_ply_like.visibility = View.GONE
                    }
                    if (contentL.get(position).isAllowComment) {
                        //videoPlayLMSView.llComment.visibility = View.VISIBLE
                        ll_vdo_ply_cmmnt.visibility = View.VISIBLE
                    } else {
                        //videoPlayLMSView.llComment.visibility = View.INVISIBLE
                        ll_vdo_ply_cmmnt.visibility = View.GONE
                    }

                    ll_vdo_ply_like.setOnClickListener {

                        ll_vdo_ply_likeFun(position)
                    }
                    try {
                        if (contentL.get(position).like_flag == true) {
                            like_flag = true

                            videoPlayLMSView.llLike.visibility = View.VISIBLE
                            videoPlayLMSView.llLike.setBackground(
                                mContext.getResources()
                                    .getDrawable(R.drawable.back_round_corner_lms_round_white)
                            )
                            iv_vdo_ply_like.setImageResource(R.drawable.heart_red)
                        } else {
                            videoPlayLMSView.llLike.visibility = View.VISIBLE
                            like_flag = false
                            videoPlayLMSView.llLike.setBackground(
                                mContext.getResources()
                                    .getDrawable(R.drawable.back_round_corner_lms_round)
                            )
                            iv_vdo_ply_like.setImageResource(R.drawable.like_white)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    ll_vdo_ply_cmmnt.setOnClickListener {
                        onCommentClick(contentL.get(position).content_id)
                    }

                    ll_vdo_ply_share.setOnClickListener {
                        val intent = Intent(Intent.ACTION_SEND)
                        intent.putExtra(Intent.EXTRA_TEXT, currentVideoObj.content_url)
                        intent.type = "text/plain"
                        context!!.startActivity(Intent.createChooser(intent, "Share Via"))
                    }

                    val comment_list: ArrayList<CommentL> = ArrayList()

                    current_lms_video_obj.user_id = Pref.user_id.toString()
                    current_lms_video_obj.topic_id = Companion.topic_id.toInt()
                    current_lms_video_obj.topic_name = Companion.topic_name
                    current_lms_video_obj.content_id = contentL.get(position).content_id.toInt()
                    current_lms_video_obj.like_flag = false
                    current_lms_video_obj.share_count = 0
                    current_lms_video_obj.no_of_comment = 0
                    current_lms_video_obj.content_length = "00:00:00"
                    current_lms_video_obj.content_watch_length = "00:00:00"
                    current_lms_video_obj.content_watch_start_date =
                        AppUtils.getCurrentDateyymmdd()
                    current_lms_video_obj.content_watch_end_date =
                        AppUtils.getCurrentDateyymmdd()
                    current_lms_video_obj.content_watch_completed = false
                    current_lms_video_obj.content_last_view_date_time =
                        AppUtils.getCurrentDateTimeNew()
                    current_lms_video_obj.WatchStartTime = "00:00:00"
                    current_lms_video_obj.WatchEndTime = "00:00:00"
                    current_lms_video_obj.WatchedDuration = "00:00:00"
                    current_lms_video_obj.Timestamp = AppUtils.getCurrentDateTimeNew()
                    current_lms_video_obj.DeviceType = "Mobile"
                    current_lms_video_obj.Operating_System = "Android"
                    current_lms_video_obj.Location = "0.0"
                    current_lms_video_obj.PlaybackSpeed = "0.0"
                    current_lms_video_obj.Watch_Percentage = 0
                    current_lms_video_obj.QuizAttemptsNo = 0
                    current_lms_video_obj.QuizScores = 0
                    current_lms_video_obj.CompletionStatus = false
                    current_lms_video_obj.comment_list = comment_list
                    videoPlayLMSView.llFragVideoPlayComments.visibility = View.GONE
                    AppUtils.hideSoftKeyboard(mContext as DashboardActivity)
                    val previousIndex = exoPlayerItems.indexOfFirst { it.exoPlayer.isPlaying }
                    if (previousIndex != -1) {
                        val player = exoPlayerItems[previousIndex].exoPlayer
                        player.pause()
                        player.playWhenReady = false
                    }
                    val newIndex = exoPlayerItems.indexOfFirst { it.position == position }
                    if (newIndex != -1) {
                        val player = exoPlayerItems[newIndex].exoPlayer
                        player.playWhenReady = true
                        player.play()
                    }
                }
            })

            try {
                if (CustomStatic.VideoPosition != -1)
                    videoPlayLMSView.viewPager2.setCurrentItem(CustomStatic.VideoPosition, false)
                CustomStatic.VideoPosition = -1
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            adapter1 = VideoAdapter1(
                videoPlayLMSView.viewPager2,
                mContext,
                contentL,
                topic_id,
                topic_name,
                Companion.content_position,
                ll_vdo_ply_like,
                ll_vdo_ply_cmmnt,
                ll_vdo_ply_share,
                iv_vdo_ply_like,
                videoPlayLMSView.ivVdoPlyBookmark,
                exo_fullscreen,
                object : VideoAdapter1.OnVideoPreparedListener {

                    override fun onLikeClick(isLike: Boolean) {

                        onLikeClickFun(isLike)

                    }

                    override fun onBookmarkClick() {

                        onBookmarkClickFun()

                    }

                    override fun onEndofVidForCountUpdate() {
                        contentCountSaveAPICalling()
                    }

                    override fun onVideoPrepared(exoPlayerItem: ExoPlayerItem) {
                        exoPlayerItems.add(exoPlayerItem)
                    }

                    override fun onNonVideo() {

                    }

                    override fun onContentInfoAPICalling(obj: LMS_CONTENT_INFO) {

                        onContentInfoAPICallingFun(obj)

                    }

                    override fun onCommentCLick(obj: ContentL) {

                    }

                    override fun onShareClick(obj: ContentL) {

                    }

                    @SuppressLint("SuspiciousIndentation")
                    override fun onQuestionAnswerSetPageLoad(
                        obj: ArrayList<QuestionL>,
                        position: Int
                    ) {

                        onQuestionAnswerSetPageLoadFun(position)

                    }
                },
                object : VideoAdapter1.OnLastVideoCompleteListener {
                    override fun onLastVideoComplete() {

                        onLastVideoCompleteFun()

                    }
                },
                content_watch_point
            )

            videoPlayLMSView.viewPager2.adapter = adapter1

            onActivityStateChanged1 = adapter1.registerActivityState()


            if (!isReceiverRegistered) {
                screenOffReceiver = object : BroadcastReceiver() {
                    override fun onReceive(context: Context?, intent: Intent?) {
                        if (intent?.action == Intent.ACTION_SCREEN_OFF) {
                            adapter1.pauseCurrentVideo()
                        }
                    }
                }

                val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)
                requireActivity().registerReceiver(screenOffReceiver, filter)
                isReceiverRegistered = true
            }

            videoPlayLMSView.viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)

                    Pref.videoCompleteCount = (position + 1).toString()
                    try {
                        Pref.LastVideoPlay_TopicID = topic_id
                        Pref.LastVideoPlay_TopicName = topic_name
                        Pref.LastVideoPlay_VidPosition = position.toString()
                        Pref.LastVideoPlay_BitmapURL =
                            contentL.get(position).content_thumbnail.toString()
                        Pref.LastVideoPlay_ContentID = contentL.get(position).content_id
                        Pref.LastVideoPlay_ContentName = contentL.get(position).content_title
                        Pref.LastVideoPlay_ContentDesc =
                            contentL.get(position).content_description
                        Pref.LastVideoPlay_ContentParcent =
                            contentL.get(position).Watch_Percentage

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    currentVideoObj = contentL.get(position)

                    try {
                        if (currentVideoObj.isBookmarked == null) {
                            currentVideoObj.isBookmarked = "0"
                        }
                    } catch (e: Exception) {
                        currentVideoObj.isBookmarked = "0"
                    }

                    try {
                        if (currentVideoObj.isBookmarked.equals("1")) {
                            videoPlayLMSView.llBook.visibility = View.VISIBLE
                            videoPlayLMSView.llBook.setBackground(
                                mContext.getResources()
                                    .getDrawable(R.drawable.back_round_corner_lms_round_white)
                            );
                            videoPlayLMSView.ivVdoPlyBookmark.setImageResource(R.drawable.bookmark_green)
                            videoPlayLMSView.tvFragVidBookmarkText.text = "Saved"
                        } else {
                            videoPlayLMSView.llBook.visibility = View.VISIBLE
                            videoPlayLMSView.llBook.setBackground(
                                mContext.getResources()
                                    .getDrawable(R.drawable.back_round_corner_lms_round)
                            );
                            videoPlayLMSView.ivVdoPlyBookmark.setImageResource(R.drawable.save_instagram)
                            videoPlayLMSView.tvFragVidBookmarkText.text = "Save"
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    if (contentL.get(position).isAllowLike) {
                        ll_vdo_ply_like.visibility = View.VISIBLE
                    } else {
                        ll_vdo_ply_like.visibility = View.GONE
                    }
                    if (contentL.get(position).isAllowComment) {
                        //videoPlayLMSView.llComment.visibility = View.VISIBLE
                        ll_vdo_ply_cmmnt.visibility = View.VISIBLE
                    } else {
                        //videoPlayLMSView.llComment.visibility = View.INVISIBLE
                        ll_vdo_ply_cmmnt.visibility = View.GONE
                    }

                    ll_vdo_ply_like.setOnClickListener {

                        ll_vdo_ply_likeFun(position)
                    }
                    try {
                        if (contentL.get(position).like_flag == true) {
                            like_flag = true

                            videoPlayLMSView.llLike.visibility = View.VISIBLE
                            videoPlayLMSView.llLike.setBackground(
                                mContext.getResources()
                                    .getDrawable(R.drawable.back_round_corner_lms_round_white)
                            )
                            iv_vdo_ply_like.setImageResource(R.drawable.heart_red)
                        } else {
                            videoPlayLMSView.llLike.visibility = View.VISIBLE
                            like_flag = false
                            videoPlayLMSView.llLike.setBackground(
                                mContext.getResources()
                                    .getDrawable(R.drawable.back_round_corner_lms_round)
                            )
                            iv_vdo_ply_like.setImageResource(R.drawable.like_white)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    ll_vdo_ply_cmmnt.setOnClickListener {
                        onCommentClick(contentL.get(position).content_id)
                    }

                    ll_vdo_ply_share.setOnClickListener {
                        val intent = Intent(Intent.ACTION_SEND)
                        intent.putExtra(Intent.EXTRA_TEXT, currentVideoObj.content_url)
                        intent.type = "text/plain"
                        context!!.startActivity(Intent.createChooser(intent, "Share Via"))
                    }

                    val comment_list: ArrayList<CommentL> = ArrayList()

                    current_lms_video_obj.user_id = Pref.user_id.toString()
                    current_lms_video_obj.topic_id = Companion.topic_id.toInt()
                    current_lms_video_obj.topic_name = Companion.topic_name
                    current_lms_video_obj.content_id = contentL.get(position).content_id.toInt()
                    current_lms_video_obj.like_flag = false
                    current_lms_video_obj.share_count = 0
                    current_lms_video_obj.no_of_comment = 0
                    current_lms_video_obj.content_length = "00:00:00"
                    current_lms_video_obj.content_watch_length = "00:00:00"
                    current_lms_video_obj.content_watch_start_date =
                        AppUtils.getCurrentDateyymmdd()
                    current_lms_video_obj.content_watch_end_date =
                        AppUtils.getCurrentDateyymmdd()
                    current_lms_video_obj.content_watch_completed = false
                    current_lms_video_obj.content_last_view_date_time =
                        AppUtils.getCurrentDateTimeNew()
                    current_lms_video_obj.WatchStartTime = "00:00:00"
                    current_lms_video_obj.WatchEndTime = "00:00:00"
                    current_lms_video_obj.WatchedDuration = "00:00:00"
                    current_lms_video_obj.Timestamp = AppUtils.getCurrentDateTimeNew()
                    current_lms_video_obj.DeviceType = "Mobile"
                    current_lms_video_obj.Operating_System = "Android"
                    current_lms_video_obj.Location = "0.0"
                    current_lms_video_obj.PlaybackSpeed = "0.0"
                    current_lms_video_obj.Watch_Percentage = 0
                    current_lms_video_obj.QuizAttemptsNo = 0
                    current_lms_video_obj.QuizScores = 0
                    current_lms_video_obj.CompletionStatus = false
                    current_lms_video_obj.comment_list = comment_list
                    videoPlayLMSView.llFragVideoPlayComments.visibility = View.GONE
                    AppUtils.hideSoftKeyboard(mContext as DashboardActivity)
                    val previousIndex = exoPlayerItems.indexOfFirst { it.exoPlayer.isPlaying }
                    if (previousIndex != -1) {
                        val player = exoPlayerItems[previousIndex].exoPlayer
                        player.pause()
                        player.playWhenReady = false
                    }
                    val newIndex = exoPlayerItems.indexOfFirst { it.position == position }
                    if (newIndex != -1) {
                        val player = exoPlayerItems[newIndex].exoPlayer
                        player.playWhenReady = true
                        player.play()
                    }
                }
            })

            try {
                if (CustomStatic.VideoPosition != -1)
                    videoPlayLMSView.viewPager2.setCurrentItem(CustomStatic.VideoPosition, false)
                CustomStatic.VideoPosition = -1
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private fun ll_vdo_ply_likeFun(position: Int) {

        if (like_flag) {
            videoPlayLMSView.llLike.visibility = View.VISIBLE
            videoPlayLMSView.llLike.setBackground(
                mContext.getResources()
                    .getDrawable(R.drawable.back_round_corner_lms_round)
            )
            videoPlayLMSView.ivVdoPlyLike.setImageResource(R.drawable.like_white)
            like_flag = false
            contentL.get(position).isLiked = false
            contentL.filter { it.content_id.equals(currentVideoObj.content_id) }
                .first().like_flag = false
        } else {
            videoPlayLMSView.llLike.visibility = View.VISIBLE
            like_flag = true
            videoPlayLMSView.llLike.setBackground(
                mContext.getResources()
                    .getDrawable(R.drawable.back_round_corner_lms_round_white)
            )
            videoPlayLMSView.ivVdoPlyLike.setImageResource(R.drawable.heart_red)
            contentL.get(position).isLiked = true
            contentL.filter { it.content_id.equals(currentVideoObj.content_id) }
                .first(). like_flag = true
            showLikePointPopup(content_like_point)
            Pref.like_count = (Pref.like_count + 1)
            contentCountSaveAPICalling()
        }
        contentL.filter {
            it.content_id.toString().equals(Pref.LastVideoPlay_ContentID.toString())
        }.first().like_flag = like_flag

        var obj: LMS_CONTENT_INFO = LMS_CONTENT_INFO()
        obj.user_id = Pref.user_id.toString()
        obj.topic_id = Pref.LastVideoPlay_TopicID.toInt()
        obj.topic_name = Pref.LastVideoPlay_TopicName
        obj.content_id = Pref.LastVideoPlay_ContentID.toInt()
        obj.like_flag = like_flag
        obj.share_count = 0
        obj.no_of_comment = 0
        obj.content_length = Obj_LMS_CONTENT_INFO.content_length
        obj.content_watch_length =
            Obj_LMS_CONTENT_INFO.content_watch_length
        obj.content_watch_start_date = AppUtils.getCurrentDateyymmdd()
        obj.content_watch_end_date = AppUtils.getCurrentDateyymmdd()
        obj.content_watch_completed =
            Obj_LMS_CONTENT_INFO.content_watch_completed
        obj.content_last_view_date_time = AppUtils.getCurrentDateTimeNew()
        obj.WatchStartTime = Obj_LMS_CONTENT_INFO.WatchStartTime
        obj.WatchEndTime = Obj_LMS_CONTENT_INFO.WatchEndTime
        obj.WatchedDuration = Obj_LMS_CONTENT_INFO.WatchedDuration
        obj.Timestamp = AppUtils.getCurrentDateTimeNew()
        obj.DeviceType = "Mobile"
        obj.Operating_System = "Android"
        obj.Location = "0.0"
        obj.PlaybackSpeed = "0.0"
        obj.Watch_Percentage = Obj_LMS_CONTENT_INFO.Watch_Percentage
        obj.QuizAttemptsNo = 0
        obj.QuizScores = 0
        obj.CompletionStatus = false
        val comment_listL: ArrayList<CommentL> = ArrayList()
        obj.comment_list = comment_listL

        saveContentWiseInfo(obj)
    }

    private fun onLastVideoCompleteFun() {

        lastvideo = true

        lastvideo_ = true

        if (lastvideo_ == true) {
            (context as DashboardActivity).onBackPressed()
        }

    }

    private fun onQuestionAnswerSetPageLoadFun(position: Int) {

        try {

            if (contentL.size - 1 == position)
                lastvideo = true
            else
                lastvideo = false

            LmsQuestionAnswerSet.lastVideo = lastvideo

            if (Pref.videoCompleteCount.toInt() % Pref.QuestionAfterNoOfContentForLMS.toInt() == 0) {
                CustomStatic.IsQuestionPageOpen = true
                question_ans_setL = ArrayList()
                for (i in Pref.videoCompleteCount.toInt() - 1 downTo (Pref.videoCompleteCount.toInt() - Pref.QuestionAfterNoOfContentForLMS.toInt())) {
                    if (sequenceQuestionL.get(i).completionStatus == false) {
                        var questionRootObj = sequenceQuestionL.get(i).question_list
                        question_ans_setL.addAll(questionRootObj)
                    }
                }
                if (question_ans_setL.size > 0) {
                    LmsQuestionAnswerSet.topic_name = topic_name
                    //val videoPlayLMS = (context as DashboardActivity).supportFragmentManager.findFragmentByTag("VideoPlayLMS")
                    stopVideoPlayback()
                    CustomStatic.IsQuestionPageOpen = false

                    (context as DashboardActivity).loadFrag(LmsQuestionAnswerSet.getInstance(question_ans_setL),LmsQuestionAnswerSet::class.java.simpleName, true)

                } else {
                    CustomStatic.IsQuestionPageOpen = false

                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun stopVideoPlayback() {
        val index = exoPlayerItems.indexOfFirst { it.position == videoPlayLMSView.viewPager2.currentItem }
        if (index != -1) {
            val player = exoPlayerItems[index].exoPlayer
            player.playWhenReady = false
            player.pause()
        }
        if (Pref.IsVideoAutoPlayInLMS) {
            adapter.pauseCurrentVideo()
        } else {
            adapter1.pauseCurrentVideo()
        }
        if (exoPlayerItems.isNotEmpty()) {
            for (item in exoPlayerItems) {
                val player = item.exoPlayer
                player.stop()
            }
        }
        if (isReceiverRegistered) {
            requireActivity().unregisterReceiver(screenOffReceiver)
            isReceiverRegistered = false
        }

    }

    private fun onContentInfoAPICallingFun(obj: LMS_CONTENT_INFO) {

        obj.like_flag = contentL.filter {
            it.content_id.toString().equals(obj.content_id.toString())
        }.first().like_flag
        if (contentL.filter {
                it.content_id.toString().equals(obj.content_id.toString())
            }.first().content_watch_completed) {
            obj.content_watch_completed = true
            obj.Watch_Percentage = 100
        }
        obj.CompletionStatus = false
        obj.QuizScores = 0
        obj.comment_list = commentL.filter {
            it.content_id.toString().equals(obj.content_id.toString())
        } as ArrayList<CommentL>
        Obj_LMS_CONTENT_INFO = obj

        obj.comment_list = ArrayList()
        saveContentWiseInfo(obj)
    }

    private fun onBookmarkClickFun() {

        var obj = VidBookmark()
        obj.topic_id = topic_id
        obj.topic_name = topic_name
        obj.content_id = currentVideoObj.content_id
        obj.content_name = currentVideoObj.content_title
        obj.content_desc = currentVideoObj.content_description
        obj.content_bitmap = currentVideoObj.content_thumbnail
        obj.content_url = currentVideoObj.content_url
        try {
            if (currentVideoObj.isBookmarked == null) {
                currentVideoObj.isBookmarked = "0"
            }
        } catch (e: Exception) {
            currentVideoObj.isBookmarked = "0"
        }
        if (currentVideoObj.isBookmarked.equals("1")) {
            obj.isBookmarked = "0"
            contentL.filter {
                it.content_id.toString()
                    .equals(currentVideoObj.content_id.toString())
            }.first().isBookmarked = "0"
        } else {
            obj.isBookmarked = "1"
            contentL.filter {
                it.content_id.toString()
                    .equals(currentVideoObj.content_id.toString())
            }.first().isBookmarked = "1"
        }
        if (obj.isBookmarked.equals("1")) {
            videoPlayLMSView.llBook.visibility = View.VISIBLE
            videoPlayLMSView.llBook.setBackground(
                mContext.getResources()
                    .getDrawable(R.drawable.back_round_corner_lms_round_white)
            );
            videoPlayLMSView.ivVdoPlyBookmark.setImageResource(R.drawable.bookmark_green)
        } else {
            videoPlayLMSView.llBook.visibility = View.VISIBLE
            videoPlayLMSView.llBook.setBackground(
                mContext.getResources()
                    .getDrawable(R.drawable.back_round_corner_lms_round)
            );
            videoPlayLMSView.ivVdoPlyBookmark.setImageResource(R.drawable.save_instagram)
        }
        bookmarkApi(obj)
        if (obj.isBookmarked.equals("1")) {
            videoPlayLMSView.lottieBookmark.visibility = View.VISIBLE

            val animator = ValueAnimator.ofFloat(0f, .5f)
            animator.addUpdateListener { animation: ValueAnimator ->
                videoPlayLMSView.lottieBookmark.setProgress(animation.animatedValue as Float)
            }
            animator.start()
            videoPlayLMSView.tvFragVidBookmarkText.text = "Saved"
        } else {
            videoPlayLMSView.tvFragVidBookmarkText.text = "Save"
        }
        Handler().postDelayed(Runnable {
            videoPlayLMSView.lottieBookmark.visibility = View.GONE
        }, 1000)
    }

    private fun onLikeClickFun(isLike: Boolean) {

        contentL.filter { it.content_id.equals(currentVideoObj.content_id) }
            .first(). like_flag = isLike
        for (i in 0..contentL.size - 1) {
        }
    }


    @SuppressLint("MissingInflatedId")
    private fun showLikePointPopup(content_like_point: Int) {
        val inflater: LayoutInflater =
            mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView: View = inflater.inflate(R.layout.popup_layout_like, null)
        popupWindow = PopupWindow(
            popupView,
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT,
            true
        )
        val popup_image: LottieAnimationView = popupView.findViewById(R.id.popup_image)
        val popup_message: TextView = popupView.findViewById(R.id.popup_message)
        var typeFace: Typeface? =
            ResourcesCompat.getFont(mContext, R.font.remachinescript_personal_use)
        popup_message.setText("+$content_like_point")

        val a: Animation = AnimationUtils.loadAnimation(mContext, R.anim.scale)
        a.reset()
        popup_message.clearAnimation()
        popup_message.startAnimation(a)

        popup_image.visibility = View.VISIBLE
        popupWindow.setBackgroundDrawable(ColorDrawable())
        popupWindow.isOutsideTouchable = false
        popupWindow.isFocusable = false
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)

        Handler().postDelayed(Runnable {
            popupWindow.dismiss()
        }, 1700)

    }


    private fun saveContentWiseInfo(obj: LMS_CONTENT_INFO) {
        try {
            videoPlayLMSView.progressWheel.spin()
            val repository = LMSRepoProvider.getTopicList()
            DashboardActivity.compositeDisposable.add(
                repository.saveContentInfoApi(obj)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ result ->
                        videoPlayLMSView.progressWheel.stopSpinning()
                        val response = result as BaseResponse
                        if (response.status == NetworkConstant.SUCCESS) {
                        } else {
                            Toast.makeText(mContext, getString(R.string.no_data_found), Toast.LENGTH_SHORT).show()

                        }
                    }, { error ->
                        Toast.makeText(mContext, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show()
                    })
            )
        } catch (ex: Exception) {
            ex.printStackTrace()
            videoPlayLMSView.progressWheel.stopSpinning()
            Toast.makeText(mContext, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPause() {
        super.onPause()

        try {
            val index = exoPlayerItems.indexOfFirst { it.position == videoPlayLMSView.viewPager2.currentItem }
            if (index != -1) {
                val player = exoPlayerItems[index].exoPlayer

                player.playWhenReady = false
                player.pause()
                player.release()
            }
            if (Pref.IsVideoAutoPlayInLMS) {
                adapter.pauseCurrentVideo()
            } else {
                adapter1.pauseCurrentVideo()
            }
        } catch (e: Exception) {
           e.printStackTrace()
        }

    }

    override fun onStop() {
        super.onStop()
        val index = exoPlayerItems.indexOfFirst { it.position == videoPlayLMSView.viewPager2.currentItem }
        if (index != -1) {
            val player = exoPlayerItems[index].exoPlayer

            player.playWhenReady = false
            player.stop()
            requireActivity().requestedOrientation =
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    override fun onResume() {
        super.onResume()

        val index = exoPlayerItems.indexOfFirst { it.position == videoPlayLMSView.viewPager2.currentItem }
        if (index != -1) {
            val player = exoPlayerItems[index].exoPlayer
            player.playWhenReady = true
            player.play()
        }
    }



    override fun onDestroy() {
        super.onDestroy()
        videoPlayLMSView.progressWheel.stopSpinning()
        if (exoPlayerItems.isNotEmpty()) {
            for (item in exoPlayerItems) {
                val player = item.exoPlayer
                player.stop()
                player.clearMediaItems()
            }
        }
         requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        if (isReceiverRegistered) {
            requireActivity().unregisterReceiver(screenOffReceiver)
            isReceiverRegistered = false
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (isReceiverRegistered) {
            requireActivity().unregisterReceiver(screenOffReceiver)
            isReceiverRegistered = false
        }
    }

    fun callDestroy() {
        try {
            super.onDestroy()
            if (exoPlayerItems.isNotEmpty()) {
                for (item in exoPlayerItems) {
                    val player = item.exoPlayer
                    player.stop()
                    player.clearMediaItems()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun onCommentClick(content_id: String) {

        commentAPICalling(content_id)

        videoPlayLMSView.ivFragVideoCommentSave.setOnClickListener {

            if (!videoPlayLMSView.etFragVideoComment.text.toString().equals("")) {

                AppUtils.hideSoftKeyboard(mContext as DashboardActivity)
                var obj: CommentL = CommentL()
                obj.topic_id = topic_id
                obj.content_id = content_id
                obj.commented_user_id = Pref.user_id.toString()
                obj.commented_user_name = Pref.user_name.toString()
                obj.comment_id = Pref.user_id + "_" + AppUtils.getCurrentDateTime()
                obj.comment_description = videoPlayLMSView.etFragVideoComment.text.toString()
                obj.comment_date_time = AppUtils.getCurrentDateTime()
                commentL.add(obj)

                videoPlayLMSView.etFragVideoComment.setText("")

                current_lms_video_obj.comment_list = ArrayList()
                current_lms_video_obj.comment_list.add(obj)

                saveContentWiseInfoFOrComment(current_lms_video_obj)
            } else {
                Toast.makeText(mContext, "Please write any comment", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun commentAPICalling(content_id: String) {
        try {
            val repository = LMSRepoProvider.getTopicList()
            DashboardActivity.compositeDisposable.add(
                repository.getCommentInfo(topic_id,content_id )
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ result ->
                        val response = result as MyCommentListResponse
                        try {
                            if (response.status == NetworkConstant.SUCCESS) {
                                Pref.comment_count = (Pref.comment_count + 1)
                                commentL = ArrayList<CommentL>()
                                videoPlayLMSView.llFragVideoPlayComments.visibility = View.VISIBLE
                                loadCommentData(response.comment_list.filter { it.content_id.toString().equals(content_id.toString()) } as ArrayList<CommentL>)

                            }else{
                                var blankL : ArrayList<CommentL> = ArrayList()
                                loadCommentData(blankL)
                            }
                        } catch (e: Exception) {
                            var blankL : ArrayList<CommentL> = ArrayList()
                            loadCommentData(blankL)
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

    fun loadCommentData(comL: ArrayList<CommentL>) {
        videoPlayLMSView.llFragVideoPlayComments.visibility = View.VISIBLE
        try {
            if (comL.size > 0) {
                cmtAdapter = AdapterComment(mContext, comL)
                videoPlayLMSView.rvFragVideoPlayComment.adapter = cmtAdapter
                videoPlayLMSView.rvFragVideoPlayComment.smoothScrollToPosition(comL.size - 1)
            } else {
                if (cmtAdapter != null) {
                    cmtAdapter.clear()
                    cmtAdapter.notifyDataSetChanged()
                } else {
                    cmtAdapter = AdapterComment(mContext, comL)
                    videoPlayLMSView.rvFragVideoPlayComment.adapter = cmtAdapter
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        contentCountSaveAPICalling()
    }

    private fun saveContentWiseInfoFOrComment(obj: LMS_CONTENT_INFO) {
        try {
            videoPlayLMSView.progressWheel.spin()
            val repository = LMSRepoProvider.getTopicList()
            DashboardActivity.compositeDisposable.add(
                repository.saveContentInfoApi(obj)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ result ->
                        videoPlayLMSView.progressWheel.stopSpinning()
                        val response = result as BaseResponse
                        if (response.status == NetworkConstant.SUCCESS) {

                            onCommentClick(obj.content_id.toString())
                            showCommentPointPopup(content_comment_point)
                        } else {
                            Toast.makeText(mContext, getString(R.string.no_data_found), Toast.LENGTH_SHORT).show()

                        }
                    }, { error ->
                        Toast.makeText(mContext, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show()
                    })
            )
        } catch (ex: Exception) {
            ex.printStackTrace()
            videoPlayLMSView.progressWheel.stopSpinning()
            Toast.makeText(mContext, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("MissingInflatedId")
    private fun showCommentPointPopup(content_comment_point: Int) {

        val inflater: LayoutInflater =
            mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView: View = inflater.inflate(R.layout.popup_layout_congratulation_, null)
        popupWindow = PopupWindow(
            popupView,
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT,
            true
        )
        val popup_image: LottieAnimationView = popupView.findViewById(R.id.popup_image)
        val popup_message: TextView = popupView.findViewById(R.id.popup_message)
        var typeFace: Typeface? =
            ResourcesCompat.getFont(mContext, R.font.remachinescript_personal_use)
        popup_message.setText("+$content_comment_point")

        val a: Animation = AnimationUtils.loadAnimation(mContext, R.anim.scale)
        a.reset()
        popup_message.clearAnimation()
        popup_message.startAnimation(a)

        popup_image.visibility = View.VISIBLE
        popupWindow.setBackgroundDrawable(ColorDrawable())
        popupWindow.isOutsideTouchable = false
        popupWindow.isFocusable = false
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)

        Handler().postDelayed(Runnable {
            popupWindow.dismiss()
        }, 1500)

    }

    private fun getPointsAPICalling() {
        try {
            val repository = LMSRepoProvider.getTopicList()
            DashboardActivity.compositeDisposable.add(
                repository.overAllDatalist(Pref.session_token!!)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ result ->
                        val response = result as SectionsPointsList
                        if (response.status == NetworkConstant.SUCCESS) {
                            content_watch_point = response.content_watch_point
                            content_like_point = response.content_like_point
                            content_share_point = response.content_share_point
                            content_comment_point = response.content_comment_point

                        }else{

                        }
                    }, { error ->

                    })
            )
        }
        catch (ex: Exception) {
            ex.printStackTrace()

        }
    }

    private fun bookmarkApi(obj:VidBookmark){
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
                            try {
                                (mContext as DashboardActivity).updateBookmarkCnt()
                            } catch (e: Exception) {
                                Pref.CurrentBookmarkCount = 0
                            }
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
            println("tag_count_api call for $obj")
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

}
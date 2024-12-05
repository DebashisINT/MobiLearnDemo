package com.breezemobilearndemo.features.mylearning
import android.animation.Animator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Handler
import android.text.format.DateFormat
import android.util.Log
import android.view.Gravity
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
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.airbnb.lottie.LottieAnimationView
import com.breezemobilearndemo.AppUtils
import com.breezemobilearndemo.CommentL
import com.breezemobilearndemo.ContentL
import com.breeze.breezemobilearndemo.CustomStatic
import com.breezemobilearndemo.DashboardActivity
import com.breezemobilearndemo.ExoPlayerItem
import com.breezemobilearndemo.LMS_CONTENT_INFO
import com.breezemobilearndemo.Pref
import com.breezemobilearndemo.QuestionL
import com.breezemobilearndemo.R
import com.breezemobilearndemo.SavedContentIds
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSource
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.pnikosis.materialishprogress.ProgressWheel
import org.apache.commons.lang3.time.DurationFormatUtils
import java.text.SimpleDateFormat
import java.util.Locale


class VideoAdapter1(var viewPager2: ViewPager2,
                    var context: Context,
                    var videos: ArrayList<ContentL>,
                    var topic_id:String,
                    var topic_name:String,
                    var content_position:Int,
                    var ll_vdo_ply_like: LinearLayout,
                    var ll_vdo_ply_cmmnt: LinearLayout,
                    var ll_vdo_ply_share: LinearLayout,
                    var iv_vdo_ply_like: ImageView,
                    var iv_vdo_ply_bookmark: ImageView,
                    var exo_fullscreen: ImageView,
                    /*var like_flag: Boolean,*/
                    var videoPreparedListener: VideoAdapter1.OnVideoPreparedListener,
                    var lastVideoCompleteListener: OnLastVideoCompleteListener,
                    var content_watch_point: Int) : RecyclerView.Adapter<VideoAdapter1.VideoViewHolder>()
{

    companion object {
        private var is_portraitTouched: Boolean = false
    }


    private var position_: Long = 0
    private var duration: Long = 0
    private var percentageWatched: Long = 0
    private var starttime: Long = 0
    private var endTime: Long = 0

    private var currentPlayingViewHolder: VideoViewHolder? = null
    private lateinit var popupWindow: PopupWindow

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_video, parent, false)
        return VideoViewHolder(view)
    }

    override fun getItemCount(): Int {
        return videos.size
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.bindItems(holder,context,videos,videoPreparedListener)
    }

    interface OnVideoPlaybackStateChangedListener {
        fun onVideoPlaybackStateChanged(position: Long, duration: Long)
    }

    private var onVideoPlaybackStateChangedListener: OnVideoPlaybackStateChangedListener? = null

    inner class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        lateinit var exoPlayer: ExoPlayer
        lateinit var mediaSource: MediaSource
        var video_watch_completed = false
        var like_flag = false
        var seek_dragging = false
        private val savedContentIds = mutableSetOf<Int>()

        fun bindItems(holder: VideoViewHolder, context: Context, videos_: ArrayList<ContentL>, listner: OnVideoPreparedListener) {

            var stylplayerView = itemView.findViewById<StyledPlayerView>(R.id.stylplayerView)
            var tvTitle = itemView.findViewById<AppCompatTextView>(R.id.tvTitle)
            var tvDescrip = itemView.findViewById<AppCompatTextView>(R.id.tvDescrip)
            var progress_wheel = itemView.findViewById<ProgressWheel>(R.id.progress_wheel)


            if (videos_.get(absoluteAdapterPosition).content_url.contains(
                    ".mp4",
                    ignoreCase = true
                )
            ) {
                setVideoPath(
                    holder,
                    videos_.get(absoluteAdapterPosition).content_url,
                    absoluteAdapterPosition,
                    listner,
                    stylplayerView,
                    progress_wheel
                )
                if (videos_.get(absoluteAdapterPosition).content_url.contains("http")) {
                    setVideoPath(
                        holder,
                        videos_.get(absoluteAdapterPosition).content_url,
                        absoluteAdapterPosition,
                        listner,
                        stylplayerView,
                        progress_wheel
                    )
                } else {
                    setVideoPath(
                        holder,
                        "http://3.7.30.86:8073" + videos_.get(absoluteAdapterPosition).content_url,
                        absoluteAdapterPosition,
                        listner,
                        stylplayerView,
                        progress_wheel
                    )
                }
            }

            val model = videos[absoluteAdapterPosition]
            tvTitle.text = model.content_title
            tvDescrip.text = model.content_description

            if (model.content_watch_length != "" && model.content_watch_completed == false) {

                val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
                val time = LocalTime.parse(model.content_watch_length, formatter)
                val milliseconds = time.toSecondOfDay() * 1000L
                exoPlayer.seekTo(milliseconds)
            } else {
                exoPlayer.seekTo(0)

            }

            ll_vdo_ply_share.setOnClickListener {

            }
            iv_vdo_ply_bookmark.setOnClickListener {
                videoPreparedListener.onBookmarkClick()
            }

            exo_fullscreen.setOnClickListener {
                try {
                    if (is_portraitTouched == false) {
                        is_portraitTouched = true
                        exo_fullscreen.setBackgroundResource(R.drawable.full_screenfff);
                        (context as Activity).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
                        (context as DashboardActivity).hideToolbar()
                        context.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    } else {
                        exo_fullscreen.setBackgroundResource(R.drawable.switch_to_full_screen_button);
                        (context as Activity).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                        is_portraitTouched = false
                        (context as DashboardActivity).showToolbar()
                        context.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)


                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        private fun setVideoPath(
            holder: VideoViewHolder,
            contentUrl: String,
            position: Int,
            listner: OnVideoPreparedListener,
            stylplayerView: StyledPlayerView,
            progress_wheel: ProgressWheel
        ) {

            try {
                exoPlayer.release()
                stylplayerView.player?.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            progress_wheel.stopSpinning()
            val trackSelector = DefaultTrackSelector(context)
            exoPlayer = ExoPlayer.Builder(context).setTrackSelector(trackSelector)
                .setRenderersFactory(
                    DefaultRenderersFactory(context).setEnableDecoderFallback(
                        true
                    )
                ).setSeekForwardIncrementMs(10000L)
                .setSeekBackIncrementMs(10000L).build()

            val parametersBuilder = trackSelector.parameters.buildUpon()

            parametersBuilder.setMaxVideoBitrate(500_000)

            trackSelector.setParameters(parametersBuilder)


            exoPlayer.addListener(object : Player.Listener {

                override fun onPlayerError(error: PlaybackException) {
                    super.onPlayerError(error)
                    Toast.makeText(context, "Can't play this video", Toast.LENGTH_SHORT).show()
                }

                @SuppressLint("SuspiciousIndentation")
                override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {

                    position_ = exoPlayer.currentPosition
                    duration = exoPlayer.duration

                    onVideoPlaybackStateChangedListener?.onVideoPlaybackStateChanged(exoPlayer.currentPosition, exoPlayer.duration)

                    percentageWatched = (100 * position_ / duration)
                    if (percentageWatched.toInt() == 100) {
                        video_watch_completed = true
                    } else {
                        video_watch_completed = false
                    }

                    if (percentageWatched.toInt() > 100){
                        percentageWatched =100
                    }

                    if (playWhenReady && playbackState == Player.STATE_READY) {

                        currentPlayingViewHolder = holder

                        starttime = System.currentTimeMillis()
                        convertDate(starttime.toString(), "hh:mm:ss a")
                        convertTo24HourFormat(convertDate(starttime.toString(), "hh:mm:ss a"))
                    }

                    if (duration >=0 && position_ >=0 ){

                        val comment_list: ArrayList<CommentL> = ArrayList()

                        val data_end_LMS_CONTENT_INFO = LMS_CONTENT_INFO(
                            Pref.user_id!!,
                            topic_id.toInt(),
                            topic_name,
                            videos.get(position).content_id.toInt(),
                            like_flag,
                            0,
                            0,
                            DurationFormatUtils.formatDuration(duration, "HH:mm:ss"),
                            DurationFormatUtils.formatDuration(position_, "HH:mm:ss"),
                            AppUtils.getCurrentDateTimeNew(),
                            AppUtils.getCurrentDateTimeNew(),
                            video_watch_completed,
                            AppUtils.getCurrentDateTimeNew(),
                            convertTo24HourFormat(convertDate(starttime.toString(), "hh:mm:ss a")),
                            convertTo24HourFormat(convertDate(endTime.toString(),"hh:mm:ss a")),
                            DurationFormatUtils.formatDuration(position_, "HH:mm:ss"),
                            AppUtils.getCurrentDateTimeNew(),
                            "Mobile",
                            "Android",
                            "",
                            exoPlayer.playbackParameters.speed.toString(),
                            percentageWatched.toInt(),
                            0,
                            0,
                            false,
                            comment_list
                        )

                        listner.onContentInfoAPICalling(data_end_LMS_CONTENT_INFO)

                    }

                    when (playbackState) {

                        Player.STATE_ENDED -> {

                            Pref.content_watch_count = Pref.content_watch_count+1

                            listner.onEndofVidForCountUpdate()

                            endTime = System.currentTimeMillis()
                            convertDate(endTime.toString(),"hh:mm:ss a")
                            convertTo24HourFormat(convertDate(endTime.toString(),"hh:mm:ss a"))
                            val comment_list: ArrayList<CommentL> = ArrayList()

                            val data_end_LMS_CONTENT_INFO = LMS_CONTENT_INFO(
                                Pref.user_id!!,
                                topic_id.toInt(),
                                topic_name,
                                videos.get(position).content_id.toInt(),
                                like_flag,
                                0,
                                0,
                                DurationFormatUtils.formatDuration(duration, "HH:mm:ss"),
                                DurationFormatUtils.formatDuration(position_, "HH:mm:ss"),
                                AppUtils.getCurrentDateTimeNew(),
                                AppUtils.getCurrentDateTimeNew(),
                                video_watch_completed,
                                AppUtils.getCurrentDateTimeNew(),
                                convertTo24HourFormat(convertDate(starttime.toString(), "hh:mm:ss a")),
                                convertTo24HourFormat(convertDate(endTime.toString(),"hh:mm:ss a")),
                                DurationFormatUtils.formatDuration(position_, "HH:mm:ss"),
                                AppUtils.getCurrentDateTimeNew(),
                                "Mobile",
                                "Android",
                                "",
                                exoPlayer.playbackParameters.speed.toString(),
                                percentageWatched.toInt(),
                                0,
                                0,
                                false,
                                comment_list
                            )

                            listner.onContentInfoAPICalling(data_end_LMS_CONTENT_INFO)

                        }

                        Player.STATE_READY -> {
                            val starttime = System.currentTimeMillis()
                            convertDate(starttime.toString(),"yyyy-MM-dd hh:mm:ss");
                        }
                        Player.STATE_BUFFERING -> {
                        }
                        Player.STATE_IDLE -> {
                        }
                    }

                    if (playbackState == Player.STATE_BUFFERING) {
                        progress_wheel.spin()
                    }
                    else if (playbackState == ExoPlayer.STATE_READY ){
                        progress_wheel.stopSpinning()
                    }
                    else if ( playbackState == Player.STATE_ENDED ) {

                        if(CustomStatic.IsHomeClick == true){
                            CustomStatic.IsHomeClick = false
                        }else{
                            if (Pref.LastVideoPlay_VidPosition.toInt() == absoluteAdapterPosition && CustomStatic.IsQuestionPageOpen == false) {
                                showWatchPointPopup(content_watch_point)
                            }
                        }

                        try {
                            val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                            val contentIdsString = sharedPreferences.getString("saved_content_ids", "")
                            val savedContentIds = SavedContentIds()
                            savedContentIds.content_id = contentIdsString!!.split(",").filter { it.isNotEmpty() }.map { it.toInt() }.let { it.toCollection(LinkedHashSet(it)) }

                            if (exoPlayer.playbackParameters.speed != 2.0.toFloat() && !seek_dragging ) {
                                //if (true) { // test code
                                if (videos.get(position).question_list!=null && videos.get(position).CompletionStatus==false) {
                                    //if (true) {// test code
                                    popupWindow.setOnDismissListener {

                                        try {
                                            listner.onQuestionAnswerSetPageLoad(videos.get(position).question_list.clone() as ArrayList<QuestionL>, absoluteAdapterPosition)
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }

                                    }
                                }
                            }

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                override fun onPositionDiscontinuity(
                    oldPosition: Player.PositionInfo,
                    newPosition: Player.PositionInfo,
                    reason: Int
                ) {
                    super.onPositionDiscontinuity(oldPosition, newPosition, reason)

                    if(((newPosition.positionMs / 1000).toInt() - (oldPosition.positionMs / 1000).toInt()) >= 10 ){
                        seek_dragging = true
                    }else{
                        seek_dragging = false
                    }
                }
            })

            stylplayerView.visibility = View.VISIBLE

            exoPlayer.seekTo(0)
            exoPlayer.repeatMode = Player.REPEAT_MODE_OFF

            val dataSourceFactory = DefaultDataSource.Factory(context)

            mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(Uri.parse(contentUrl)))
            exoPlayer.setMediaSource(mediaSource)
            exoPlayer.prepare()

            progress_wheel.visibility = View.GONE
            if (absoluteAdapterPosition == 0) {
                exoPlayer.playWhenReady = true
                exoPlayer.play()
                progress_wheel.stopSpinning()
            }

            exoPlayer.playWhenReady = false
            videoPreparedListener.onVideoPrepared(ExoPlayerItem(exoPlayer, absoluteAdapterPosition))
            stylplayerView.player = exoPlayer

        }

        fun convertDate(dateInMilliseconds: String, dateFormat: String?): String {
            return DateFormat.format(dateFormat, dateInMilliseconds.toLong()).toString()
        }

        private fun convertTo24HourFormat(time12Hour: String): String {
            val inputFormat = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
            val outputFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val date = inputFormat.parse(time12Hour)
            return outputFormat.format(date)
        }
    }
    private fun showWatchPointPopup(content_watch_point: Int) {

        val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView: View = inflater.inflate(R.layout.popup_layout_congratulation_, null)
        popupWindow = PopupWindow(
            popupView,
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT,
            true
        )
        val popup_image: LottieAnimationView = popupView.findViewById(R.id.popup_image)
        val popup_message: TextView = popupView.findViewById(R.id.popup_message)
        popup_message.setText("+$content_watch_point")

        val a: Animation = AnimationUtils.loadAnimation(context, R.anim.scale)
        a.reset()
        popup_message.clearAnimation()
        popup_message.startAnimation(a)

        popup_image.visibility =View.VISIBLE
        popupWindow.setBackgroundDrawable(ColorDrawable())
        popupWindow.isOutsideTouchable = false
        popupWindow.isFocusable = false
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)


        popup_image.addAnimatorListener(object : Animator.AnimatorListener {

            override fun onAnimationStart(animation: Animator) {
                Log.e("AnimationVideo:","start");
            }

            override fun onAnimationEnd(animation: Animator) {

                Handler().postDelayed(Runnable {

                    popup_image.visibility = View.GONE
                    popup_message.visibility = View.VISIBLE
                    popupWindow.dismiss()
                    viewPager2.setCurrentItem(viewPager2.currentItem + 1, true)
                }, 1)


            }

            override fun onAnimationCancel(animation: Animator) {
                Log.e("AnimationVideo:","cancel");
            }

            override fun onAnimationRepeat(animation: Animator) {
                Log.e("AnimationVideo:","Repeat");
            }
        })

    }

    fun registerActivityState()  = object : OnActivityStateChanged{
        override fun onResumed() {
            Log.d("SimpleTextListAdapter", "onResumed: ")
        }

        override fun onPaused() {
            println("tag_check_lf onPaused")

        }
    }

    fun pauseCurrentVideo() {
        currentPlayingViewHolder?.exoPlayer?.pause()
    }

    interface OnVideoPreparedListener {
        fun onVideoPrepared(exoPlayerItem: ExoPlayerItem)
        fun onNonVideo()
        fun onContentInfoAPICalling(obj: LMS_CONTENT_INFO)
        fun onCommentCLick(obj: ContentL)
        fun onShareClick(obj: ContentL)
        fun onQuestionAnswerSetPageLoad(obj: ArrayList<QuestionL>,position:Int)
        fun onLikeClick(isLike:Boolean)
        fun onBookmarkClick()
        fun onEndofVidForCountUpdate()
    }
    interface OnLastVideoCompleteListener {
        fun onLastVideoComplete()
    }

    interface OnActivityStateChanged{
        fun onResumed()
        fun onPaused()
    }

}
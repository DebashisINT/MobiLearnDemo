package com.breezemobilearndemo

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.breezemobilearndemo.databinding.FragmentRetryPlayBinding
import com.breezemobilearndemo.databinding.FragmentSearchLmsBinding
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource

class RetryPlayFrag : Fragment() {

    private var binding : FragmentRetryPlayBinding? = null
    private val retryPlayView get() = binding!!
    private lateinit var mContext: Context
    lateinit var exoPlayer: ExoPlayer
    lateinit var mediaSource: MediaSource


    companion object{
        var play_url:String=""
        var question_id:Int=0
        var topic_id:Int=0
        var content_id:Int=0
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext=context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentRetryPlayBinding.inflate(inflater,container,false)
        return retryPlayView.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE or WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        initView()
    }

    private fun initView() {

        playRetryVid()

    }

    fun playRetryVid(){
        exoPlayer = ExoPlayer.Builder(mContext)
            .setRenderersFactory(
                DefaultRenderersFactory(mContext).setEnableDecoderFallback(
                    true
                )
            ).setSeekForwardIncrementMs(10000L)
            .setSeekBackIncrementMs(10000L).build()

        exoPlayer.seekTo(0)
        exoPlayer.repeatMode = Player.REPEAT_MODE_OFF

        val dataSourceFactory = DefaultDataSource.Factory(mContext)

        mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(Uri.parse(play_url)))
        exoPlayer.setMediaSource(mediaSource)
        exoPlayer.prepare()

        exoPlayer.playWhenReady = true
        exoPlayer.play()

        retryPlayView.stylePlayerRetry.visibility = View.VISIBLE
        retryPlayView.stylePlayerRetry.player = exoPlayer

        exoPlayer.addListener(object : Player.Listener{
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                super.onPlayerStateChanged(playWhenReady, playbackState)
                when(playbackState){
                    Player.STATE_READY -> {
                        retryPlayView.progressWheel.stopSpinning()
                    }
                    Player.STATE_BUFFERING -> {
                        retryPlayView.progressWheel.spin()
                    }
                    Player.STATE_ENDED -> {
                        retryPlayView.progressWheel.stopSpinning()
                        RetryQuestionFrag.topic_id = topic_id
                        RetryQuestionFrag.content_id = content_id
                        RetryQuestionFrag.question_id = question_id
                        (mContext as DashboardActivity).loadFrag(RetryQuestionFrag(), RetryQuestionFrag::class.java.name, true)
                    }
                }
            }
        })
    }

    override fun onPause() {
        super.onPause()
        try {
            exoPlayer.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

}
package com.breezemobilearndemo

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.breezemobilearndemo.databinding.FragmentBookmarkBinding
import com.breezemobilearndemo.databinding.FragmentBookmarkPlayBinding
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource


class BookmarkPlayFrag : Fragment() {

    private var binding : FragmentBookmarkPlayBinding? = null
    private val bookmarkPlayView get() = binding!!
    private lateinit var mContext: Context
    lateinit var exoPlayer: ExoPlayer
    lateinit var mediaSource: MediaSource

    companion object{
        var play_url:String=""
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext=context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentBookmarkPlayBinding.inflate(inflater,container,false)
        return bookmarkPlayView.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        initView()
    }

    private fun initView() {
        bookmarkPlayView.progressWheelBookmarkPlay.stopSpinning()
        playVid()
    }

    fun playVid(){
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

        bookmarkPlayView.stylePlayerBookmark.visibility = View.VISIBLE
        bookmarkPlayView.stylePlayerBookmark.player = exoPlayer

        exoPlayer.addListener(object : Player.Listener{
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                super.onPlayerStateChanged(playWhenReady, playbackState)
                when(playbackState){
                    Player.STATE_READY -> {
                        bookmarkPlayView.progressWheelBookmarkPlay.stopSpinning()
                    }
                    Player.STATE_BUFFERING -> {
                        bookmarkPlayView.progressWheelBookmarkPlay.spin()
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
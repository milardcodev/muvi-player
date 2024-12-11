package com.muviplayer.android

import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.extractor.ExtractorsFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.muviplayer.android.WatchActivity.Companion.videoListWA

class VideoPlayerActivity : AppCompatActivity() {
    private var screenRatio: ImageView? = null
    private var simpleExoPlayer: SimpleExoPlayer? = null
    private var playWhenReady = true
    private var playerView: PlayerView? = null
    private var playbackPosition: Long = 0
    private var fileName: TextView? = null
    private var position = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player)

        initViews()
        initializeViews()
        position = intent.getIntExtra("position", -1)
        if (position != -1 && position < videoListWA.size) {
            val videoFileName: String = videoListWA[position].name
            fileName!!.text = videoFileName
        }
    }

    private fun initViews() {
        screenRatio?.setOnClickListener {
            if (playerView?.resizeMode == AspectRatioFrameLayout.RESIZE_MODE_FIT) {
                playerView?.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                screenRatio?.setImageResource(R.drawable.ic_fit_screen)
            } else {
                playerView?.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                screenRatio?.setImageResource(R.drawable.ic_aspect_ratio)
            }
        }
    }


    private fun initializePlayer() {
        val path: String = videoListWA[position].path
        if (path != null) {
            val uri = Uri.parse(path)
            simpleExoPlayer = SimpleExoPlayer.Builder(this).build()
            val factory: DataSource.Factory = DefaultDataSourceFactory(
                this,
                com.google.android.exoplayer2.util.Util.getUserAgent(this, "My App Name")
            )
            val extractorsFactory: ExtractorsFactory = DefaultExtractorsFactory()
            val mediaSource: MediaSource =
                ProgressiveMediaSource.Factory(factory, extractorsFactory).createMediaSource(
                    MediaItem.fromUri(uri)
                )
            playerView!!.player = simpleExoPlayer
            playerView!!.keepScreenOn = true
            simpleExoPlayer!!.prepare(mediaSource)
            if (playbackPosition > 0) {
                simpleExoPlayer!!.seekTo(playbackPosition)
            }
            simpleExoPlayer!!.playWhenReady = playWhenReady
        }
    }

    private fun initializeViews() {
        playerView = findViewById<PlayerView>(R.id.exoplayer_movie)
        fileName = findViewById(R.id.video_file_name)
    }

    private fun releasePlayer() {
        if (simpleExoPlayer != null) {
            playbackPosition = simpleExoPlayer!!.currentPosition
            playWhenReady = simpleExoPlayer!!.playWhenReady
            simpleExoPlayer!!.release()
            simpleExoPlayer = null
        }
    }

    override fun onStart() {
        super.onStart()
        if (SDK_INT >= 24) {
            initializePlayer()
        }
    }

    override fun onResume() {
        super.onResume()
        if (SDK_INT < 24 || simpleExoPlayer == null) {
            initializePlayer()
        }
    }

    override fun onPause() {
        if (SDK_INT < 24) {
            releasePlayer()
        }
        super.onPause()
    }

    override fun onStop() {
        if (SDK_INT >= 24) {
            releasePlayer()
        }
        super.onStop()
    }
}
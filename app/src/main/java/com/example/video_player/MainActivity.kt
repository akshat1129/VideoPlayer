package com.example.video_player

import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import android.view.View

class MainActivity : AppCompatActivity() {

    private var player: ExoPlayer? = null
    private lateinit var playerView: PlayerView

    private lateinit var volumeSeekBar: SeekBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        playerView = findViewById(R.id.playerView)
        volumeSeekBar = findViewById(R.id.volumeSeekBar)
        setupFullscreenButton()
    }

    override fun onStart() {
        super.onStart()
        initializePlayer()
    }

    override fun onStop() {
        super.onStop()
        releasePlayer()
    }

    private fun initializePlayer() {
        // 1. Create the ExoPlayer instance
        player = ExoPlayer.Builder(this).build()

        // 2. Bind the player to the UI view
        playerView.player = player

        // Listen for when the standard controls appear or disappear
        // Listen for when the standard controls appear or disappear
        playerView.setControllerVisibilityListener(PlayerView.ControllerVisibilityListener { visibility ->
            // Update the volume slider's visibility to match the controls
            volumeSeekBar.visibility = visibility
        })

        // 3. Build a MediaItem from a sample video URL
        val sampleVideoUrl = "https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4"
        val mediaItem = MediaItem.fromUri(Uri.parse(sampleVideoUrl))

        // 4. Set the media item, prepare the player, and start playback
        player?.setMediaItem(mediaItem)
        player?.prepare()
        player?.playWhenReady = true // Auto-plays when ready

        // Setup Volume Control Logic
        volumeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Convert the 0-100 progress into a 0.0f - 1.0f float for ExoPlayer
                val volumeLevel = progress / 100f
                player?.volume = volumeLevel
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Optional: You could show a volume icon here when the user starts touching the slider
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Optional: Hide the volume icon when the user stops touching the slider
            }
        })

    }

    private fun releasePlayer() {
        player?.release()
        player = null
    }

    private fun setupFullscreenButton() {
        // Listen for clicks on the ExoPlayer's built-in fullscreen button
        playerView.setFullscreenButtonClickListener { isFullScreen ->
            if (isFullScreen) {
                enterFullscreen()
            } else {
                exitFullscreen()
            }
        }
    }

    private fun enterFullscreen() {
        // 1. Force screen to landscape
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

        // 2. Hide the system status bar and navigation bar
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, playerView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            // Allow the user to swipe from the edges to temporarily reveal the system bars
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        // Handle the notch
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

    }

    private fun exitFullscreen() {
        // 1. Force screen back to portrait
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // 2. Show the system status bar and navigation bar again
        WindowCompat.setDecorFitsSystemWindows(window, true)
        WindowInsetsControllerCompat(window, playerView).show(WindowInsetsCompat.Type.systemBars())
    }

}
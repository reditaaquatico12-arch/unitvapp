// language: Kotlin, file: PlayerActivity.kt, target: Android 21+
// Tela fullscreen de reprodução — suporta controle remoto TV Box
package com.unitvclone.ui.player

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import com.unitvclone.R
import com.unitvclone.data.local.AppDatabase
import com.unitvclone.data.local.WatchHistoryEntry
import com.unitvclone.player.ExoPlayerManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@UnstableApi
class PlayerActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_URL   = "stream_url"
        const val EXTRA_TITLE = "content_title"
        const val EXTRA_ID    = "content_id"
        const val EXTRA_COVER = "cover_url"
        const val EXTRA_RESUME_MS = "resume_position_ms"
    }

    private lateinit var playerManager: ExoPlayerManager
    private lateinit var playerView: PlayerView
    private lateinit var progressBar: ProgressBar
    private lateinit var titleText: TextView
    private lateinit var btnBack: ImageButton

    private var contentId    = ""
    private var contentTitle = ""
    private var coverUrl     = ""
    private var streamUrl    = ""
    private var resumeMs     = 0L

    private val db by lazy { AppDatabase.get(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Fullscreen — sem status bar
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        )
        setContentView(R.layout.activity_player)

        contentId    = intent.getStringExtra(EXTRA_ID)    ?: ""
        contentTitle = intent.getStringExtra(EXTRA_TITLE) ?: ""
        coverUrl     = intent.getStringExtra(EXTRA_COVER) ?: ""
        streamUrl    = intent.getStringExtra(EXTRA_URL)   ?: return
        resumeMs     = intent.getLongExtra(EXTRA_RESUME_MS, 0L)

        playerView  = findViewById(R.id.playerView)
        progressBar = findViewById(R.id.progressBar)
        titleText   = findViewById(R.id.tvTitle)
        btnBack     = findViewById(R.id.btnBack)

        titleText.text = contentTitle

        playerManager = ExoPlayerManager(this)
        playerView.player = playerManager.player

        playerManager.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                progressBar.visibility = if (state == Player.STATE_BUFFERING)
                    View.VISIBLE else View.GONE
            }
        })

        playerManager.play(streamUrl, resumeMs)
        scheduleHistorySave()

        btnBack.setOnClickListener { finish() }
    }

    // Salva histórico e posição a cada 30 segundos
    private fun scheduleHistorySave() {
        if (contentId.isEmpty()) return
        lifecycleScope.launch {
            while (true) {
                delay(30_000)
                if (!playerManager.isPlaying) continue
                db.watchHistoryDao().upsert(
                    WatchHistoryEntry(
                        contentId    = contentId,
                        contentName  = contentTitle,
                        coverUrl     = coverUrl,
                        streamUrl    = streamUrl,
                        positionMs   = playerManager.currentPositionMs
                    )
                )
            }
        }
    }

    // Controle remoto TV Box — botões DPAD
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                if (playerManager.isPlaying) playerManager.pause()
                else playerManager.resume()
                true
            }
            KeyEvent.KEYCODE_MEDIA_FAST_FORWARD, KeyEvent.KEYCODE_DPAD_RIGHT -> {
                playerManager.seekTo(playerManager.currentPositionMs + 10_000)
                true
            }
            KeyEvent.KEYCODE_MEDIA_REWIND, KeyEvent.KEYCODE_DPAD_LEFT -> {
                playerManager.seekTo(maxOf(0, playerManager.currentPositionMs - 10_000))
                true
            }
            KeyEvent.KEYCODE_BACK -> { finish(); true }
            else -> super.onKeyDown(keyCode, event)
        }
    }

    override fun onPause() {
        super.onPause()
        playerManager.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Salva posição final
        if (contentId.isNotEmpty()) {
            lifecycleScope.launch {
                db.watchHistoryDao().upsert(
                    WatchHistoryEntry(
                        contentId   = contentId,
                        contentName = contentTitle,
                        coverUrl    = coverUrl,
                        streamUrl   = streamUrl,
                        positionMs  = playerManager.currentPositionMs
                    )
                )
            }
        }
        playerManager.release()
    }
}

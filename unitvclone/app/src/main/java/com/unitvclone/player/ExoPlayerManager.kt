// language: Kotlin, file: ExoPlayerManager.kt, target: Android 21+
// Media3 ExoPlayer — HLS · RTMP · MP4 · MKV · DASH
// suporta retomada de posição, buffer configurável, troca de qualidade
package com.unitvclone.player

import android.content.Context
import android.net.Uri
import androidx.media3.common.*
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.rtsp.RtspMediaSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector

@UnstableApi
class ExoPlayerManager(
    private val context: Context,
    bufferSec: Int = 10,
    hwDecode: Boolean = true
) {
    val player: ExoPlayer

    private val trackSelector = DefaultTrackSelector(context).apply {
        setParameters(buildUponParameters().setPreferredAudioLanguage("pt"))
    }

    init {
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                /* minBufferMs    */ bufferSec * 1000,
                /* maxBufferMs    */ bufferSec * 4000,
                /* bufferForPlaybackMs */ bufferSec * 500,
                /* bufferForPlaybackAfterRebufferMs */ bufferSec * 1000
            )
            .build()

        val renderersFactory = if (hwDecode) {
            // Hardware decode — mais rápido, usa GPU
            androidx.media3.exoplayer.DefaultRenderersFactory(context)
                .setExtensionRendererMode(androidx.media3.exoplayer.DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)
        } else {
            androidx.media3.exoplayer.DefaultRenderersFactory(context)
                .setExtensionRendererMode(androidx.media3.exoplayer.DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF)
        }

        player = ExoPlayer.Builder(context, renderersFactory)
            .setTrackSelector(trackSelector)
            .setLoadControl(loadControl)
            .build()
    }

    fun play(url: String, startPositionMs: Long = 0L) {
        val mediaItem = MediaItem.fromUri(Uri.parse(url))
        val source = buildMediaSource(url, mediaItem)
        player.setMediaSource(source)
        if (startPositionMs > 0) player.seekTo(startPositionMs)
        player.prepare()
        player.playWhenReady = true
    }

    fun pause()  { player.pause() }
    fun resume() { player.play() }
    fun stop()   { player.stop(); player.clearMediaItems() }

    fun seekTo(positionMs: Long) { player.seekTo(positionMs) }

    val currentPositionMs: Long get() = player.currentPosition
    val durationMs: Long        get() = player.duration
    val isPlaying: Boolean      get() = player.isPlaying

    // Adiciona listener externo para estado do player
    fun addListener(listener: Player.Listener) = player.addListener(listener)
    fun removeListener(listener: Player.Listener) = player.removeListener(listener)

    fun release() { player.release() }

    private fun buildMediaSource(url: String, item: MediaItem): MediaSource {
        val uri = Uri.parse(url)
        return when {
            url.contains(".m3u8") || url.contains("/live/") ->
                HlsMediaSource.Factory(
                    androidx.media3.datasource.DefaultHttpDataSource.Factory()
                        .setUserAgent("UniTV/1.0")
                        .setAllowCrossProtocolRedirects(true)
                ).createMediaSource(item)

            url.startsWith("rtsp") ->
                RtspMediaSource.Factory().createMediaSource(item)

            else ->
                DefaultMediaSourceFactory(context).createMediaSource(item)
        }
    }
}

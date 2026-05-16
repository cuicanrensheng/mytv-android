package top.yogiczy.mytv.player

import android.content.Context
import android.view.MotionEvent
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import top.yogiczy.mytv.data.SettingManager
import top.yogiczy.mytv.data.SourceManager
import top.yogiczy.mytv.m3u.M3uParser
import kotlin.math.abs

class PlayerManager(
    private val context: Context,
    private val m3uParser: M3uParser
) {
    private var exoPlayer: ExoPlayer? = null
    private var touchStartX = 0f
    private var touchStartY = 0f

    init {
        initPlayer()
    }

    private fun initPlayer() {
        exoPlayer = ExoPlayer.Builder(context)
            .build()
        exoPlayer?.playWhenReady = true
        exoPlayer?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                super.onPlaybackStateChanged(state)
                if (state == Player.STATE_IDLE || state == Player.STATE_ENDED) {
                    if (m3uParser.autoSwitchLine()) {
                        playCurrentChannel()
                    }
                } else if (state == Player.STATE_READY) {
                    val url = m3uParser.getCurrentChannel()
                        ?.lineUrls?.get(m3uParser.getCurrentChannel()?.currentLine ?: 0) ?: return
                    val domain = url.split("/")[2]
                    val domains = SourceManager.validDomain.toMutableSet()
                    domains.add(domain)
                    SourceManager.validDomain = domains
                }
            }
        })
    }

    fun playCurrentChannel() {
        val channel = m3uParser.getCurrentChannel() ?: return
        val url = channel.lineUrls[channel.currentLine]
        val mediaItem = MediaItem.fromUri(url)
        exoPlayer?.setMediaItem(mediaItem)
        exoPlayer?.prepare()
    }

    fun prevChannel() {
        m3uParser.prevChannel()
        playCurrentChannel()
    }

    fun nextChannel() {
        m3uParser.nextChannel()
        playCurrentChannel()
    }

    fun switchLinePrev() {
        m3uParser.switchLine(-1)
        playCurrentChannel()
    }

    fun switchLineNext() {
        m3uParser.switchLine(1)
        playCurrentChannel()
    }

    fun jumpChannel(index: Int) {
        if (index >= 0 && index < m3uParser.channelList.size) {
            m3uParser.currentIndex = index
            playCurrentChannel()
        }
    }

    fun toggleCollect() {
        m3uParser.toggleCollect()
    }

    fun handleTouch(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchStartX = event.x
                touchStartY = event.y
            }
            MotionEvent.ACTION_UP -> {
                val dx = event.x - touchStartX
                val dy = event.y - touchStartY
                if (abs(dx) > abs(dy)) {
                    if (dx > 30) switchLineNext()
                    else if (dx < -30) switchLinePrev()
                } else {
                    if (dy > 30) nextChannel()
                    else if (dy < -30) prevChannel()
                }
            }
            MotionEvent.ACTION_LONG_PRESS -> {
                toggleCollect()
            }
        }
        return true
    }

    fun release() {
        exoPlayer?.release()
    }
}

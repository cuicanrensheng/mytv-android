package top.yogiczy.mytv.activities

import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import top.yogiczy.mytv.data.SettingManager
import top.yogiczy.mytv.data.SourceManager
import top.yogiczy.mytv.m3u.M3uParser
import top.yogiczy.mytv.player.PlayerManager
import top.yogiczy.mytv.web.WebServer
import java.util.Timer
import java.util.TimerTask

class MainActivity : AppCompatActivity() {
    private lateinit var player: PlayerManager
    private lateinit var m3uParser: M3uParser
    private lateinit var webServer: WebServer
    private var doubleTapTime = 0L
    private var numberBuffer = ""
    private var numberTimer: Timer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        SettingManager.init(this)
        SourceManager.init(this)

        webServer = WebServer(this)
        webServer.start()

        m3uParser = M3uParser()
        m3uParser.initDefaultSource()
        player = PlayerManager(this, m3uParser)
        player.playCurrentChannel()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> player.prevChannel()
            KeyEvent.KEYCODE_DPAD_DOWN -> player.nextChannel()
            KeyEvent.KEYCODE_DPAD_LEFT -> player.switchLinePrev()
            KeyEvent.KEYCODE_DPAD_RIGHT -> player.switchLineNext()

            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                if (event.isLongPress) player.toggleCollect()
            }

            KeyEvent.KEYCODE_MENU, KeyEvent.KEYCODE_HELP -> {
                showEpg()
            }

            in KeyEvent.KEYCODE_0..KeyEvent.KEYCODE_9 -> {
                numberBuffer += (keyCode - KeyEvent.KEYCODE_0)
                numberTimer?.cancel()
                numberTimer = Timer().apply {
                    schedule(object : TimerTask() {
                        override fun run() {
                            val num = numberBuffer.toIntOrNull()
                            num?.let { player.jumpChannel(it - 1) }
                            numberBuffer = ""
                        }
                    }, 800)
                }
            }

            else -> return super.onKeyDown(keyCode, event)
        }
        return true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_UP -> {
                val now = System.currentTimeMillis()
                if (now - doubleTapTime < 300) {
                    showEpg()
                } else {
                    doubleTapTime = now
                }
            }
            MotionEvent.ACTION_LONG_PRESS -> {
                player.toggleCollect()
            }
        }
        return player.handleTouch(event)
    }

    private fun showEpg() {
        val channel = m3uParser.getCurrentChannel() ?: return
        val epgList = top.yogiczy.mytv.epg.EpgManager.getTodayEpg(channel.name)
        Toast.makeText(this, "今日节目单已打开", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }
}

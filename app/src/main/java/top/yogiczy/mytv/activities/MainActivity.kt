package com.tviptv

import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import com.tviptv.player.PlayerManager
import com.tviptv.web.WebServer

class MainActivity : AppCompatActivity() {

    private lateinit var player: PlayerManager
    private lateinit var webServer: WebServer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 启动10481网页配置服务
        webServer = WebServer()
        webServer.start()

        // 初始化播放器、直播源、多线路、收藏、EPG
        player = PlayerManager(this)
        player.initDefaultSource("https://gitee.com/qf_1111/iptv/raw/master/playlist.m3u")
    }

    // 遥控器按键映射（严格按你的需求）
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> { player.prevChannel(); true }
            KeyEvent.KEYCODE_DPAD_DOWN -> { player.nextChannel(); true }
            KeyEvent.KEYCODE_DPAD_LEFT -> { player.switchLinePrev(); true }
            KeyEvent.KEYCODE_DPAD_RIGHT -> { player.switchLineNext(); true }
            KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_DPAD_CENTER -> {
                if (event?.isLongPress == true) player.collectChannel()
                else player.selectChannel()
                true
            }
            KeyEvent.KEYCODE_MENU, KeyEvent.KEYCODE_HELP -> { player.showEpg(); true }
            else -> super.onKeyDown(keyCode, event)
        }
    }

    // 触屏映射
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return player.handleTouch(event)
    }
}

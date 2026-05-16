package com.tviptv.player

import android.content.Context
import android.view.MotionEvent
import com.tviptv.m3u.M3uParser

class PlayerManager(private val ctx: Context) {
    private var m3uParser = M3uParser()

    fun initDefaultSource(url: String) {
        m3uParser.parse(url)
    }

    // 频道切换（支持反转）
    fun prevChannel() = m3uParser.prev()
    fun nextChannel() = m3uParser.next()

    // 线路切换
    fun switchLinePrev() = m3uParser.switchLine(-1)
    fun switchLineNext() = m3uParser.switchLine(1)

    // 收藏
    fun collectChannel() = m3uParser.toggleCollect()

    // 选台
    fun selectChannel() = m3uParser.play()

    // 节目单
    fun showEpg() = m3uParser.showTodayEpg()

    // 触屏映射
    fun handleTouch(e: MotionEvent): Boolean {
        // 上下滑动换台，左右滑动切线路，单击OK，长按收藏，双击EPG
        return true
    }
}

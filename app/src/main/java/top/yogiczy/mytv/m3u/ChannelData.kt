package com.tviptv.m3u

data class ChannelData(
    val name: String,
    val lineUrls: MutableList<String>, // 多线路播放地址
    var currentLine: Int = 0
)

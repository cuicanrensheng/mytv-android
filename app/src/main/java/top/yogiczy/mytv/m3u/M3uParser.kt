package top.yogiczy.mytv.m3u

import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import top.yogiczy.mytv.data.SettingManager
import top.yogiczy.mytv.data.SourceManager
import java.util.concurrent.TimeUnit

class M3uParser {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
    var channelList: MutableList<ChannelData> = mutableListOf()
    var currentIndex = 0
    var isShowCollect = false

    // ===================== 你的固定直播源 =====================
    // 【已写入】https://gitee.com/qf_1111/iptv/raw/master/playlist.m3u
    fun initDefaultSource() {
        parseSource("https://gitee.com/qf_1111/iptv/raw/master/playlist.m3u")
    }
    // =========================================================

    // 解析订阅源，支持 M3U / TVBox 格式
    fun parseSource(url: String) {
        try {
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""

            channelList.clear()
            when {
                body.contains("#EXTM3U") -> parseM3U(body)
                body.contains("\"urls\"") -> parseTVBox(body)
            }

            // 解析成功加入历史订阅源
            SourceManager.saveCustomSource(url)
        } catch (e: Exception) {
            // 解析失败从历史移除
            SourceManager.removeFailSource(url)
        }
    }

    // 解析标准M3U，合并同名称多线路
    private fun parseM3U(content: String) {
        val lines = content.lines()
        var channelName = ""
        val tempMap = mutableMapOf<String, MutableList<String>>()

        for (line in lines) {
            val trimLine = line.trim()
            if (trimLine.startsWith("#EXTINF")) {
                channelName = trimLine.split(",").lastOrNull() ?: "未知频道"
            } else if (trimLine.isNotEmpty() && !trimLine.startsWith("#")) {
                tempMap.getOrPut(channelName) { mutableListOf() }.add(trimLine)
            }
        }

        // 按可播放域名优先级排序线路
        val validDomains = SourceManager.validDomain
        tempMap.forEach { (name, urls) ->
            val sortedUrls = urls.sortedByDescending { url ->
                validDomains.any { domain -> url.contains(domain) }
            }.toMutableList()
            channelList.add(ChannelData(name, sortedUrls))
        }
    }

    // 解析TVBox格式订阅源
    private fun parseTVBox(content: String) {
        val tvBoxData = gson.fromJson(content, TvBoxSource::class.java)
        tvBoxData.channels.forEach { item ->
            channelList.add(ChannelData(item.name, item.urls.toMutableList()))
        }
    }

    // 上一个频道（支持换台反转）
    fun prevChannel() {
        val reverse = SettingManager.reverseChannel
        if (reverse) nextChannel()
        else {
            currentIndex = if (currentIndex <= 0) channelList.size - 1 else currentIndex - 1
        }
    }

    // 下一个频道（支持换台反转）
    fun nextChannel() {
        val reverse = SettingManager.reverseChannel
        if (reverse) prevChannel()
        else {
            currentIndex = if (currentIndex >= channelList.size - 1) 0 else currentIndex + 1
        }
    }

    // 左右切换线路
    fun switchLine(offset: Int) {
        val channel = getCurrentChannel() ?: return
        channel.currentLine += offset
        if (channel.currentLine < 0) channel.currentLine = channel.lineUrls.size - 1
        if (channel.currentLine >= channel.lineUrls.size) channel.currentLine = 0
    }

    // 自动切换线路（播放失败时调用）
    fun autoSwitchLine(): Boolean {
        val channel = getCurrentChannel() ?: return false
        val oldUrl = channel.lineUrls[channel.currentLine]
        // 移除失效域名
        SourceManager.validDomain = SourceManager.validDomain.apply {
            remove(oldUrl.split("/")[2])
        }
        channel.currentLine++
        return channel.currentLine < channel.lineUrls.size
    }

    // 收藏/取消收藏当前频道
    fun toggleCollect() {
        val collectSet = SourceManager.collectChannel
        if (collectSet.contains(currentIndex)) collectSet.remove(currentIndex)
        else collectSet.add(currentIndex)
        SourceManager.collectChannel = collectSet
    }

    // 切换全部频道/收藏频道列表
    fun toggleCollectList() {
        isShowCollect = !isShowCollect
    }

    fun getCurrentChannel(): ChannelData? {
        return if (channelList.isNotEmpty()) channelList[currentIndex] else null
    }
}

// 频道数据模型
data class ChannelData(
    val name: String,
    val lineUrls: MutableList<String>,
    var currentLine: Int = 0
)

// TVBox 解析实体
data class TvBoxSource(
    val channels: List<TvBoxChannel>
)

data class TvBoxChannel(
    val name: String,
    val urls: List<String>
)

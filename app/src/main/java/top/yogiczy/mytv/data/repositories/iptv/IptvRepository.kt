package top.yogiczy.mytv.data.repositories.iptv

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.Request
import top.yogiczy.mytv.data.entities.IptvGroupList
import top.yogiczy.mytv.data.repositories.FileCacheRepository
import top.yogiczy.mytv.data.repositories.iptv.parser.IptvParser
import top.yogiczy.mytv.utils.Logger

class IptvRepository : FileCacheRepository("iptv.txt") {
    private val log = Logger.create(javaClass.simpleName)
    private val client = OkHttpClient()

    // 把你的新链接设为默认值
    suspend fun getIptvGroupList(
        context: Context,
        sourceUrl: String = "https://gitee.com/qf_1111/iptv/raw/master/playlist.m3u",
        cacheTime: Long,
        simplify: Boolean = false,
    ): IptvGroupList {
        return try {
            log.d("获取远程直播源：$sourceUrl")
            val request = Request.Builder().url(sourceUrl).build()
            val response = client.newCall(request).execute()
            
            if (!response.isSuccessful) throw Exception("获取远程直播源失败：${response.code}")
            val sourceData = response.body?.string() ?: ""

            val parser = IptvParser.instances.first { it.isSupport(sourceUrl, sourceData) }
            val groupList = parser.parse(sourceData)

            log.i("解析直播源完成：${groupList.size}个分组")
            return groupList
        } catch (ex: Exception) {
            log.e("获取远程直播源失败", ex)
            throw Exception("获取远程直播源失败，请检查网络连接", ex)
        }
    }
}

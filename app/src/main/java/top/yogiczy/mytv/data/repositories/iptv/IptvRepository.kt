package top.yogiczy.mytv.data.repositories.iptv

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.Request
import top.yogiczy.mytv.data.entities.IptvGroupList
import top.yogiczy.mytv.data.repositories.FileCacheRepository
import top.yogiczy.mytv.data.repositories.iptv.parser.IptvParser
import top.yogiczy.mytv.utils.Logger
import java.util.concurrent.TimeUnit

class IptvRepository : FileCacheRepository("iptv.txt") {
    private val log = Logger.create(javaClass.simpleName)
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    // 直接用你给的完整链接，包括%20和完整用户名
    suspend fun getIptvGroupList(
        context: Context,
        sourceUrl: String = "https://gitee.com/qf_1111/iptv/raw/master/playlist%20.m3u",
        cacheTime: Long,
        simplify: Boolean = false,
    ): IptvGroupList {
        return try {
            log.d("获取远程直播源：$sourceUrl")

            // 伪装成浏览器，绕过Gitee防盗链
            val request = Request.Builder()
                .url(sourceUrl)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .build()

            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                throw Exception("获取远程直播源失败：HTTP ${response.code}")
            }

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

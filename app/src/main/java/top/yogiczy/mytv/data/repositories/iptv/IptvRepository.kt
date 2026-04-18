package top.yogiczy.mytv.data.repositories.iptv

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.Request
import top.yogiczy.mytv.data.entities.Iptv
import top.yogiczy.mytv.data.entities.IptvGroup
import top.yogiczy.mytv.data.entities.IptvGroupList
import top.yogiczy.mytv.data.repositories.FileCacheRepository
import top.yogiczy.mytv.data.repositories.iptv.parser.IptvParser
import top.yogiczy.mytv.utils.Logger
import java.io.BufferedReader
import java.io.InputStreamReader

class IptvRepository : FileCacheRepository("iptv.txt") {
    private val log = Logger.create(javaClass.simpleName)
    private val client = OkHttpClient()

    // 双源定义
    object Source {
        const val LOCAL = "file:///android_asset/channel.txt"
        const val REMOTE = "https://raw.githubusercontent.com/cuicanrensheng/IPTV/refs/heads/main/666aaa.php"
    }

    private fun simplifyTest(group: IptvGroup, iptv: Iptv): Boolean {
        return iptv.name.lowercase().startsWith("cctv") || iptv.name.endsWith("卫视")
    }

    private fun readLocalFile(context: Context, path: String): String {
        return if (path.startsWith("file:///android_asset/")) {
            val assetPath = path.removePrefix("file:///android_asset/")
            try {
                context.assets.open(assetPath).use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).use { it.readText() }
                }
            } catch (e: Exception) {
                log.e("读取本地文件失败", e)
                ""
            }
        } else ""
    }

    private suspend fun fetchSourceData(context: Context, sourceUrl: String): String {
        return if (sourceUrl.startsWith("file:///android_asset/")) {
            readLocalFile(context, sourceUrl)
        } else {
            log.d("获取远程直播源：$sourceUrl")
            val request = Request.Builder().url(sourceUrl).build()
            return try {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) throw Exception("获取远程直播源失败：${response.code}")
                    response.body?.string() ?: ""
                }
            } catch (ex: Exception) {
                log.e("获取远程直播源失败", ex)
                throw Exception("获取远程直播源失败，请检查网络连接", ex)
            }
        }
    }

    suspend fun getIptvGroupList(
        context: Context,
        sourceUrl: String = Source.LOCAL,
        cacheTime: Long,
        simplify: Boolean = false,
    ): IptvGroupList {
        return try {
            val sourceData = fetchSourceData(context, sourceUrl)
            if (sourceData.isBlank()) throw Exception("直播源文件为空或读取失败")

            val parser = IptvParser.instances.first { it.isSupport(sourceUrl, sourceData) }
            val groupList = parser.parse(sourceData)
            
            // 修正：用flatMap的方式获取频道总数，同时兼容IptvGroup结构
            val channelCount = groupList.sumOf { it.iptvList.size }
            log.i("解析直播源完成：${groupList.size}个分组，${channelCount}个频道")

            if (simplify) {
                return IptvGroupList(groupList.map { group ->
                    IptvGroup(
                        name = group.name,
                        iptvList = group.iptvList.filter { simplifyTest(group, it) }
                    )
                }.filter { it.iptvList.isNotEmpty() })
            }

            return groupList
        } catch (ex: Exception) {
            log.e("获取直播源失败", ex)
            throw Exception(ex)
        }
    }
}

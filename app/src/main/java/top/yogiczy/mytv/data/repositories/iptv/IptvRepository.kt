package top.yogiczy.mytv.data.repositories.iptv

import android.content.Context
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

    // 双源定义
    object Source {
        // 源1：APP内置本地源（默认）
        const val LOCAL = "file:///android_asset/channel.txt"
        // 源2：远程PHP源
        const val REMOTE = "https://raw.githubusercontent.com/cuicanrensheng/IPTV/refs/heads/main/666aaa.php"
    }

    /**
     * 简化规则
     */
    private fun simplifyTest(group: IptvGroup, iptv: Iptv): Boolean {
        return iptv.name.lowercase().startsWith("cctv") || iptv.name.endsWith("卫视")
    }

    /**
     * 读取本地asset文件
     */
    private fun readLocalFile(context: Context, path: String): String {
        return if (path.startsWith("file:///android_asset/")) {
            val assetPath = path.removePrefix("file:///android_asset/")
            try {
                context.assets.open(assetPath).use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
                        reader.readText()
                    }
                }
            } catch (e: Exception) {
                log.e("读取本地文件失败", e)
                ""
            }
        } else {
            ""
        }
    }

    /**
     * 获取直播源（自动识别本地/远程）
     */
    private suspend fun fetchSourceData(context: Context, sourceUrl: String): String {
        return if (sourceUrl.startsWith("file:///android_asset/")) {
            // 读取本地源
            readLocalFile(context, sourceUrl)
        } else {
            // 读取远程源（保留原有逻辑）
            log.d("获取远程直播源：$sourceUrl")
            val client = okhttp3.OkHttpClient()
            val request = okhttp3.Request.Builder().url(sourceUrl).build()
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

    /**
     * 获取直播源分组列表（支持双源切换）
     */
    suspend fun getIptvGroupList(
        context: Context,
        sourceUrl: String = Source.LOCAL, // 默认使用本地源
        cacheTime: Long,
        simplify: Boolean = false,
    ): IptvGroupList {
        return try {
            val sourceData = fetchSourceData(context, sourceUrl)

            if (sourceData.isBlank()) {
                throw Exception("直播源文件为空或读取失败")
            }

            val parser = IptvParser.instances.first { it.isSupport(sourceUrl, sourceData) }
            val groupList = parser.parse(sourceData)
            log.i("解析直播源完成：${groupList.size}个分组，${groupList.flatMap { it.iptvList }.size}个频道")

            if (simplify) {
                return IptvGroupList(groupList.map { group ->
                    IptvGroup(
                        name = group.name,
                        iptvList = group.iptvList.filter { iptv -> simplifyTest(group, iptv) }
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

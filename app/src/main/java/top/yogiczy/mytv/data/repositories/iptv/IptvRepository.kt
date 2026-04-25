package top.yogiczy.mytv.data.repositories.iptv

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import top.yogiczy.mytv.Constants
import top.yogiczy.mytv.data.entities.Iptv
import top.yogiczy.mytv.data.entities.IptvGroup
import top.yogiczy.mytv.data.entities.IptvGroupList
import top.yogiczy.mytv.data.entities.IptvList
import top.yogiczy.mytv.data.repositories.FileCacheRepository
import top.yogiczy.mytv.data.repositories.iptv.parser.IptvParser
import top.yogiczy.mytv.utils.Logger
import java.util.concurrent.TimeUnit

class IptvRepository : FileCacheRepository("iptv.txt") {
    private val log = Logger.create(javaClass.simpleName)

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private suspend fun fetchSource(sourceUrl: String) = withContext(Dispatchers.IO) {
        log.d("加载直播源: $sourceUrl")
        val request = Request.Builder().url(sourceUrl).build()
        val response = okHttpClient.newCall(request).execute()

        if (!response.isSuccessful) {
            throw RuntimeException("链接访问失败：${response.code}")
        }
        response.body?.string() ?: throw RuntimeException("直播源内容为空")
    }

    private fun simplifyTest(group: IptvGroup, iptv: Iptv): Boolean {
        return iptv.name.lowercase().startsWith("cctv") || iptv.name.endsWith("卫视")
    }

    suspend fun getIptvGroupList(
        sourceUrl: String = Constants.DEFAULT_IPTV_URL,
        cacheTime: Long,
        simplify: Boolean = false,
    ): IptvGroupList {
        return try {
            val sourceData = getOrRefresh(cacheTime) { fetchSource(sourceUrl) }
            val parser = IptvParser.instances.first { it.isSupport(sourceUrl, sourceData) }
            val groupList = parser.parse(sourceData)
            log.i("频道解析完成，共${groupList.size}个分组")

            if (simplify) {
                IptvGroupList(groupList.map { g ->
                    IptvGroup(
                        name = g.name,
                        iptvList = IptvList(g.iptvList.filter { simplifyTest(g, it) })
                    )
                }.filter { it.iptvList.isNotEmpty() })
            } else {
                groupList
            }
        } catch (e: Exception) {
            log.e("直播源加载异常", e)
            throw e
        }
    }
}

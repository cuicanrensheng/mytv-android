package top.yogiczy.mytv.data.repositories.iptv

import top.yogiczy.mytv.data.entities.IptvGroup
import top.yogiczy.mytv.data.entities.IptvGroupList
import top.yogiczy.mytv.data.repositories.FileCacheRepository
import top.yogiczy.mytv.data.repositories.iptv.parser.IptvParser
import top.yogiczy.mytv.utils.Logger

class IptvRepository : FileCacheRepository("iptv.txt") {
    private val log = Logger.create(javaClass.simpleName)

    /**
     * 简化规则
     */
    private fun simplifyTest(group: IptvGroup, iptv: Iptv): Boolean {
        return iptv.name.lowercase().startsWith("cctv") || iptv.name.endsWith("卫视")
    }

    /**
     * 获取直播源分组列表（只加载本地源）
     */
    suspend fun getIptvGroupList(
        sourceUrl: String,
        cacheTime: Long,
        simplify: Boolean = false,
    ): IptvGroupList {
        return try {
            // 直接读取本地文件，跳过所有远程请求
            val localSourceUrl = "file:///android_asset/channel.txt"
            val sourceData = readLocalFile(localSourceUrl)

            val parser = IptvParser.instances.first { it.isSupport(localSourceUrl, sourceData) }
            val groupList = parser.parse(sourceData)
            log.i("解析直播源完成：${groupList.size}个分组，${groupList.flatMap { it.iptvList }.size}个频道")

            if (simplify) {
                return IptvGroupList(groupList.map { group ->
                    IptvGroup(
                        name = group.name, iptvList = group.iptvList.filter { iptv ->
                            simplifyTest(group, iptv)
                        }
                    )
                }.filter { it.iptvList.isNotEmpty() })
            }

            return groupList
        } catch (ex: Exception) {
            log.e("获取直播源失败", ex)
            throw Exception(ex)
        }
    }

    /**
     * 读取本地asset文件
     */
    private fun readLocalFile(path: String): String {
        return if (path.startsWith("file:///android_asset/")) {
            val assetPath = path.removePrefix("file:///android_asset/")
            // 这里的代码在编译后会被正确处理，读取assets目录下的文件
            javaClass.classLoader?.getResourceAsStream(assetPath)?.bufferedReader()?.use { it.readText() } ?: ""
        } else {
            ""
        }
    }
}

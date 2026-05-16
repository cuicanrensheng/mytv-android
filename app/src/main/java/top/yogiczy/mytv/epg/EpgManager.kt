package top.yogiczy.mytv.epg

import okhttp3.OkHttpClient
import okhttp3.Request
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import top.yogiczy.mytv.data.SourceManager
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.GZIPInputStream

data class EpgItem(
    val title: String,
    val startTime: String,
    val endTime: String
)

object EpgManager {
    private val client = OkHttpClient()
    private val epgMap = mutableMapOf<String, MutableList<EpgItem>>()
    private val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())

    fun loadEpg(url: String) {
        try {
            val req = Request.Builder().url(url).build()
            val res = client.newCall(req).execute()
            var inputStream: InputStream = res.body?.byteStream() ?: return
            if (url.endsWith(".gz")) inputStream = GZIPInputStream(inputStream)

            val parser = XmlPullParserFactory.newInstance().newPullParser()
            parser.setInput(inputStream, "UTF-8")
            parseXml(parser)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun parseXml(parser: XmlPullParser) {
        var eventType = parser.eventType
        var channelId = ""
        var startTime = ""
        var endTime = ""
        var title = ""

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "channel" -> channelId = parser.getAttributeValue(null, "id")
                        "programme" -> {
                            channelId = parser.getAttributeValue(null, "channel")
                            startTime = parser.getAttributeValue(null, "start")
                            endTime = parser.getAttributeValue(null, "stop")
                        }
                        "title" -> title = parser.nextText()
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name == "programme") {
                        val today = dateFormat.format(Date())
                        if (startTime.startsWith(today)) {
                            epgMap.getOrPut(channelId) { mutableListOf() }
                                .add(EpgItem(title, startTime, endTime))
                        }
                    }
                }
            }
            eventType = parser.next()
        }
    }

    fun getTodayEpg(channelName: String): List<EpgItem> {
        return epgMap[channelName] ?: emptyList()
    }
}

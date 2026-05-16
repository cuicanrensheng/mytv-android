package top.yogiczy.mytv.web

import top.yogiczy.mytv.data.SettingManager
import top.yogiczy.mytv.data.SourceManager
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.request.*
import android.content.Context

class WebServer(private val context: Context) {
    fun start() {
        Thread {
            embeddedServer(Netty, port = 10481) {
                routing {
                    get("/") {
                        val html = context.assets.open("web/index.html")
                            .bufferedReader().use { it.readText() }
                        call.respondText(html, io.ktor.http.ContentType.Text.Html)
                    }
                    post("/api/saveSource") {
                        val body = call.receive<Map<String,String>>()
                        SourceManager.saveCustomSource(body["source"] ?: "")
                        call.respondText("ok")
                    }
                    post("/api/saveEpg") {
                        val body = call.receive<Map<String,String>>()
                        SourceManager.saveCustomEpg(body["epg"] ?: "")
                        call.respondText("ok")
                    }
                    post("/api/saveSetting") {
                        val body = call.receive<Map<String,Any>>()
                        SettingManager.reverseChannel = body["reverse"] as Boolean
                        SettingManager.cacheTime = (body["cache"] as Number).toInt()
                        SettingManager.bootStart = body["boot"] as Boolean
                        call.respondText("ok")
                    }
                }
            }.start(wait = false)
        }.start()
    }
}

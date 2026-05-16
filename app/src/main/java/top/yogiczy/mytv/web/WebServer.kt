package com.tviptv.web

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class WebServer {
    fun start() {
        Thread {
            embeddedServer(Netty, port = 10481) {
                routing {
                    get("/") {
                        call.respondText(
                            """
<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<script src="https://cdn.jsdelivr.net/npm/vue@2.6.14/dist/vue.js"></script>
<title>TVIPTV 设置</title>
</head>
<body>
<div id="app">
<h2>自定义订阅源、节目单、缓存设置</h2>
<!-- 完整配置页面我后续给你补全 -->
</div>
</body>
</html>
""", contentType = io.ktor.http.ContentType.Text.Html
                        )
                    }
                }
            }.start(wait = false)
        }.start()
    }
}

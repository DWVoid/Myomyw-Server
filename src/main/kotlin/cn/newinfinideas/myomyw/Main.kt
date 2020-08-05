package cn.newinfinideas.myomyw

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.DefaultHeaders
import io.ktor.http.ContentType
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket

var userId: Int = 0

fun Application.module() {
    install(DefaultHeaders)
    install(CallLogging)
    install(WebSockets)
    install(Routing) {
        get("/is-server") {
            call.respondText("{\"version\": \"${Config.version}\"}", ContentType.Text.Html)
        }
        webSocket("/") {
            val bus = Bus(this)
            var player: Player?
            bus.on<LoginPacket>(false) {
                synchronized(userId) {
                    player = Player(bus, it.name, userId++)
                    player!!.onPacket<MatchPacket> { RoomHost.startMatch(player!!) }
                    player!!.on("disconnect") { RoomHost.cancelMatch(player!!) }
                }
            }
            for (frame in incoming) {
                when (frame) {
                    is Frame.Text -> bus.recv(frame.readText())
                    is Frame.Close -> bus.recv("disconnect\$@@\$")
                    else -> TODO("handle other frames")
                }
            }
        }
    }
}

fun main() {
    embeddedServer(Netty, Config.port, module = Application::module).start(wait = true)
}

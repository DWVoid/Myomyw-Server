package cn.newinfinideas.myomyw

import io.ktor.http.cio.websocket.*

class Bus(private val session: WebSocketSession) {
    private val onMsg: HashMap<String, ArrayList<suspend (String) -> Unit>> = HashMap()

    fun on(name: String, reset: Boolean, fn: suspend (String) -> Unit) {
        synchronized(onMsg) {
            onMsg.putIfAbsent(name, ArrayList())
            val q = onMsg[name]
            if (q != null) {
                if (reset) q.clear()
                q.add(fn)
            }
        }
    }

    suspend fun emit(name: String, content: String) {
        session.outgoing.send(Frame.Text("$name$@@$$content"))
    }

    suspend fun recv(content: String) {
        val name = content.substringBefore("$@@$")
        val body = content.substringAfter("$@@$")
        var unit: Array<suspend (String) -> Unit>? = null
        synchronized(onMsg) {
            val q = onMsg[name]
            if (q != null) unit = q.toTypedArray()
        }
        if (unit != null) for (v in unit!!) v(body)
    }

    suspend fun disconnect() = session.close(CloseReason(CloseReason.Codes.NORMAL, ""))
}

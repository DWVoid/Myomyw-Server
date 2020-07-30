package cn.newinfinideas.myomyw

class Player(private val socket: Bus, val name: String, private val id: Int) {
    fun on(event: String, func: suspend (String) -> Unit) {
        this.socket.on(event, true, func)
    }

    suspend fun emit(name: String, content: String) = this.socket.emit(name, content)

    suspend fun disconnect() = this.socket.disconnect()

    fun getDescription(): String = "${this.name}(${this.id})"
}
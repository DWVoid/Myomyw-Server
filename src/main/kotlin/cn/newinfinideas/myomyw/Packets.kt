package cn.newinfinideas.myomyw

import kotlin.reflect.KClass

data class StartPacket(val side: Int, val room: Int, val opponentName: String)

data class NextChessmanPacket(val chessman: Chessman)

class EndTurnPacket

data class MovePacket(val col: Int?)

data class EndGamePacket(val reason: EndReason)

object PacketTable {
    private val map = HashMap<KClass<*>, String>()
    private val invMap = HashMap<String, KClass<*>>()

    private fun push(klass: KClass<*>, name: String) {
        map[klass] = name
        invMap[name] = klass
    }

    fun getName(klass: KClass<*>) = map[klass]!!

    fun getClass(name: String) = invMap[name]!!

    init {
        push(StartPacket::class, "start")
        push(NextChessmanPacket::class, "nextChessman")
        push(EndTurnPacket::class, "endTurn")
        push(MovePacket::class, "move")
        push(EndGamePacket::class, "endGame")
    }
}
package cn.newinfinideas.myomyw.game

import cn.newinfinideas.myomyw.Chessman
import cn.newinfinideas.myomyw.EndReason
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

enum class PacketBound { Client, Server, Any }

annotation class PacketMeta(val bound: PacketBound, val name: String)

@PacketMeta(PacketBound.Client, "start")
data class StartPacket(val side: Int, val room: Int, val opponentName: String)

@PacketMeta(PacketBound.Client, "nextChessman")
data class NextChessmanPacket(val chessman: Int) {
    constructor(chessman: Chessman): this(chessman.ordinal)
}

@PacketMeta(PacketBound.Any, "endTurn")
class EndTurnPacket

@PacketMeta(PacketBound.Any, "move")
data class MovePacket(val col: Int?)

@PacketMeta(PacketBound.Client, "endGame")
data class EndGamePacket(val reason: Int) {
   constructor(reason: EndReason): this(reason.ordinal)
}

object PacketTable {
    private val map = HashMap<KClass<*>, String>()
    private val invMap = HashMap<String, KClass<*>>()

    private fun push(klass: KClass<*>, name: String) {
        map[klass] = name
        invMap[name] = klass
    }

    private inline fun <reified T> push() {
        val klass = T::class
        val an = klass.findAnnotation<PacketMeta>()
        if (an != null) push(klass, an.name)
    }

    fun getName(klass: KClass<*>) = map[klass]!!

    fun getClass(name: String) = invMap[name]!!

    init {
        push<StartPacket>()
        push<NextChessmanPacket>()
        push<EndTurnPacket>()
        push<MovePacket>()
        push<EndGamePacket>()
    }
}
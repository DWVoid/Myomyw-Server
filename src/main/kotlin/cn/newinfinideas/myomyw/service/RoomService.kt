package cn.newinfinideas.myomyw.service

import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.isNotEmpty

private val MAX_SERVER_ROOM = 10000

class Room(val id: Int, val isPublic: Boolean) {
    fun userJoin(user: User, asCompetitor: Boolean): Pair<Boolean, Int> {
    }

    var allowWatching: Boolean = false
}

object RoomService {
    private var activeRoomCount = 0
    private var maxId = 0
    private var idRecycler = ArrayDeque<Int>()
    private val roomTable = HashMap<Int, Room>()

    fun createRoom(public: Boolean): Int {
        val id = synchronized(idRecycler) { if (idRecycler.isNotEmpty()) idRecycler.removeFirst() else maxId++ }
        return tryCreateRoomWithId(id, public)
    }

    fun getRoom(id:Int): Room? = synchronized(roomTable) { roomTable[id] }

    private fun tryCreateRoomWithId(id: Int, public: Boolean): Int {
        val success = synchronized(this) {
            if (activeRoomCount < MAX_SERVER_ROOM) {
                activeRoomCount++
                true
            } else false
        }
        return if (success) doCreateRoomWithId(id, public) else -1
    }

    private fun doCreateRoomWithId(id: Int, public: Boolean): Int {
        synchronized(roomTable) { roomTable.put(id, Room(id, public)) }
        //TODO(publish public rooms)
        return id
    }
}
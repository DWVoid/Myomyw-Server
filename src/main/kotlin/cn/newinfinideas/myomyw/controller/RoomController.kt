package cn.newinfinideas.myomyw.controller

import cn.newinfinideas.myomyw.service.RoomService
import cn.newinfinideas.myomyw.service.UserService

data class CreateRoomRequest(
    val token: String,
    val public: Boolean,
    val asCompetitor: Boolean,
    val allowWatching: Boolean
)

data class CreateRoomResponse(val errorCode: Int, val roomId: Int = 0, val sessionId: Int = 0)
data class JoinRoomRequest(val token: String, val room: Int, val asCompetitor: Boolean)
data class JoinRoomResponse(val errorCode: Int, val sessionId: Int = 0)
data class AutoMatchRequest(val token: String, val allowWatching: Boolean)
data class AutoMatchResponse(val errorCode: Int, val roomId: Int, val sessionId: Int)
data class CancelMatchRequest(val token: String)
data class CancelMatchResponse(val errorCode: Int)

const val ERR_SUCCESS = 0
const val ERR_NO_SUCH_ROOM = 1
const val ERR_ROLE_CHANGE = 2
const val ERR_ROOM_FULL = 3
const val ERR_AUTH = 9997
const val ERR_USER_INTERRUPT = 9998
const val ERR_SERVER_FULL = 9999

interface IRoomController {
    fun createRoom(request: CreateRoomRequest): CreateRoomResponse
    fun joinRoom(request: JoinRoomRequest): JoinRoomResponse
    fun autoMatch(request: AutoMatchRequest): AutoMatchResponse
    fun cancelMatch(request: CancelMatchRequest): CancelMatchResponse
}

class RoomController(private val roomSrv: RoomService, private val userSrv: UserService) : IRoomController {
    override fun createRoom(request: CreateRoomRequest): CreateRoomResponse {
        val user = userSrv.getAuth(request.token) ?: return CreateRoomResponse(ERR_AUTH)
        val id = roomSrv.createRoom(request.public)
        if (id == -1) return CreateRoomResponse(ERR_SERVER_FULL)
        val room = roomSrv.getRoom(id)!!
        room.allowWatching = request.allowWatching
        val (_, token) = room.userJoin(user, request.asCompetitor)
        return CreateRoomResponse(ERR_SUCCESS, id, token)
    }

    override fun joinRoom(request: JoinRoomRequest): JoinRoomResponse {
        val user = userSrv.getAuth(request.token) ?: return JoinRoomResponse(ERR_AUTH)
        val room = roomSrv.getRoom(request.room) ?: return JoinRoomResponse(ERR_NO_SUCH_ROOM)
        val (success, token) = room.userJoin(user, request.asCompetitor)
        return JoinRoomResponse(if (success) ERR_SUCCESS else ERR_ROLE_CHANGE, token)
    }

    override fun autoMatch(request: AutoMatchRequest): AutoMatchResponse {

    }

    override fun cancelMatch(request: CancelMatchRequest): CancelMatchResponse {

    }
}
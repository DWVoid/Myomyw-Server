package cn.newinfinideas.myomyw.controller

import cn.newinfinideas.myomyw.service.UserService

data class HandshakeRequest(val version: String)
data class HandshakeResponse(val errorCode: Int)
data class LoginRequest(val name: String)
data class LoginResponse(val token: String)

private val WELL_KNOWN = setOf("BETA0.8")

interface IUserController {
    fun handshake(request: HandshakeRequest): HandshakeResponse
    fun login(request: LoginRequest): LoginResponse
}

class UserController(private val userSrv: UserService) : IUserController {
    override fun handshake(request: HandshakeRequest) =
        HandshakeResponse(if (WELL_KNOWN.contains(request.version)) 1 else 0)

    override fun login(request: LoginRequest) = LoginResponse(userSrv.signIn(request.name))
}
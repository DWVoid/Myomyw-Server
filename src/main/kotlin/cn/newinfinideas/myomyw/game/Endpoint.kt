package cn.newinfinideas.myomyw.game

interface IEndPoint {
    fun <T> on(name: String, fn: suspend (T) -> Unit)
    suspend fun <T> emit(name: String, content: T)
}

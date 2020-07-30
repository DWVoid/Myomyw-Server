package cn.newinfinideas.myomyw

import kotlinx.coroutines.*

class Timeout(private val timeMs: Long, private val fn: suspend ()->Unit) {
    private var cancelled = false

    init { start() }

    fun cancel() = synchronized(this) { cancelled = true }

    private fun start() = runBlocking { startX() }

    private fun CoroutineScope.startX() {
        launch {
            delay(timeMs)
            val test: Boolean
            synchronized(this) { test = cancelled }
            if (!test) fn()
        }
    }
}

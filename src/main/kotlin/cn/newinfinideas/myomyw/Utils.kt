package cn.newinfinideas.myomyw

import kotlinx.coroutines.*
import java.util.ArrayDeque
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.CoroutineContext

class Timeout(private val timeMs: Long, private val fn: suspend ()->Unit) {
    private var cancelled = false

    init { start() }

    fun cancel() = synchronized(this) { cancelled = true }

    private fun start() {
        GlobalScope.launch {
            delay(timeMs)
            val test: Boolean
            synchronized(this) { test = cancelled }
            if (!test) fn()
        }
    }
}

private object DbFJWThreadFactory : ForkJoinPool.ForkJoinWorkerThreadFactory {
    private val counter = AtomicInteger(1)

    override fun newThread(pool: ForkJoinPool) = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool).apply {
        name = "[NWMC O-DB FJW]${counter.getAndIncrement()}"
        if (isDaemon) isDaemon = false
        if (priority != Thread.NORM_PRIORITY) priority = Thread.NORM_PRIORITY
    }!!
}

private class SyncDispatcher(private val delegate: CoroutineDispatcher) : CoroutineDispatcher() {
    private val queue = ArrayDeque<Runnable>()
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        if (synchronized(queue) {
                val start = queue.isEmpty()
                queue.add(block)
                start
            }) delegate.dispatch(context, Runnable { drain(block) })
    }

    private tailrec fun drain(block: Runnable) {
        block.run()
        val next = spinTest()
        if (next != null) drain(next)
    }

    private fun spinTest(): Runnable? {
        for (i in 0 until 80) {
            val next = synchronized(queue) { queue.run{ if (size > 1) apply { removeFirst() }.peekFirst() else null } }
            if (next != null) return next
            Thread.onSpinWait()
        }
        return synchronized(queue) { queue.apply { removeFirst() }.peekFirst() }
    }
}

object Concurrency {
    private fun newWorkStealingPool() =
        ForkJoinPool(Runtime.getRuntime().availableProcessors(), DbFJWThreadFactory, null, true)

    private val poolThroughput = newWorkStealingPool()

    private val poolSync = newWorkStealingPool()

    val defaultCoroutineContext = poolThroughput.asCoroutineDispatcher()

    private val syncCoroutineContext = poolSync.asCoroutineDispatcher()

    fun newSynchronizedCoroutineContext(): CoroutineDispatcher = SyncDispatcher(syncCoroutineContext)

    fun join() {
        poolThroughput.shutdown()
        poolSync.shutdown()
    }
}

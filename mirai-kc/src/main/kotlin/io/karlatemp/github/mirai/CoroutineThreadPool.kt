/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/06/17 21:34:56
 *
 * Mirai-KC/Mirai-KC.main/CoroutineThreadPool.kt
 */

package io.karlatemp.github.mirai

import kotlinx.coroutines.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume


@OptIn(InternalCoroutinesApi::class)
@Suppress("MemberVisibilityCanBePrivate")
object CoroutineThreadPool : ExecutorCoroutineDispatcher(), Delay {

    val threadGroup = ThreadGroup("CoroutineThreadPool")
    private val threadCounter = AtomicInteger()
    val service: ScheduledExecutorService = Executors.newScheduledThreadPool(4) {
        Thread(threadGroup, it, "CoroutineThread - ${threadCounter.getAndIncrement()}").also { thread ->
            thread.isDaemon = true
        }
    }

    override val executor: Executor
        get() = service

    override fun close() {
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        service.execute {
            try {
                block.run()
            } catch (mainException: Throwable) {
                val handler = context[CoroutineExceptionHandler.Key]
                if (handler != null) {
                    try {
                        handler.handleException(context, mainException)
                    } catch (any: Throwable) {
                        exportException(mainException)
                        exportException(any)
                    }
                } else {
                    exportException(mainException)
                }
            }

        }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun exportException(any: Throwable) {
        Thread.currentThread().name.logger().log(Level.SEVERE, "Exception in Coroutine", any)
    }

    override fun scheduleResumeAfterDelay(timeMillis: Long, continuation: CancellableContinuation<Unit>) {
        service.schedule(Runnable {
            continuation.resume(Unit)
        }, timeMillis, TimeUnit.MILLISECONDS)
    }
}
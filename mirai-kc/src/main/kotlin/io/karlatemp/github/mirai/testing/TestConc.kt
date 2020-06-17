/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/06/17 21:34:56
 *
 * Mirai-KC/Mirai-KC.main/TestConc.kt
 */
@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package io.karlatemp.github.mirai.testing

import io.karlatemp.github.mirai.CoroutineThreadPool
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import net.mamoe.mirai.event.*
import net.mamoe.mirai.event.internal.GlobalEventListeners

@OptIn(ExperimentalStdlibApi::class)
suspend fun main() {

    val scope = CoroutineScope(CoroutineThreadPool)

    scope.subscribeAlways<Event>(priority = EventPriority.MONITOR) { println(this) }
    class TestEvent : AbstractEvent()
    TestEvent().broadcast()
    scope.cancel()
    TestEvent().broadcast()

    GlobalEventListeners[EventPriority.HIGHEST].forEach {
        println("${it.owner}, ${it.listener}")
    }
}
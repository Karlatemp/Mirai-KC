/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/06/18 13:54:47
 *
 * Mirai-KC/Mirai-KC.mirai-kc.test/testConsole.kt
 */

package console

import io.karlatemp.github.mirai.Bootstrap
import io.karlatemp.github.mirai.logging.initializeLoggingSystem
import io.karlatemp.github.mirai.startupConsoleThread

fun main() {
    initializeLoggingSystem()
    Bootstrap.initialize()
    startupConsoleThread()
    val o = Object()
    synchronized(o) {
        o.wait(0)
    }
}
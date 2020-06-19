/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/06/17 22:51:32
 *
 * Mirai-KC/Mirai-KC.mirai-kc.test/TestPluginManager.kt
 */

package pluginManager

import io.github.karlatemp.miraikc.plugin.PluginManager
import kotlin.system.exitProcess

fun main() {
    PluginManager.reload()
    Thread.sleep(5000)
    PluginManager.reload()
    Thread.sleep(5000)
    exitProcess(0)
}
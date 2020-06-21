/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/06/21 13:09:06
 *
 * Mirai-KC/Mirai-KC.MoRain.main/AutoInitializerDemo.kt
 */

package io.github.karlatemp.miraikc.demo

import io.github.karlatemp.miraikc.plugin.AutoInitializer
import kotlinx.coroutines.launch
import java.util.logging.Level

@Suppress("unused")
object AutoInitializerDemo : AutoInitializer {
    override fun initialize() {
        val logger = DemoCore.logger
        logger.log(Level.INFO, "Yep. auto call!")
        DemoCore.launch {
            // ww. 插件的协程应该使用 <PluginCore>.launch
            // 为了热重载！
            logger.log(Level.INFO, "[AutoInitializerDemo] Launched")
        }
    }
}
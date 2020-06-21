/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/06/21 13:04:45
 *
 * Mirai-KC/Mirai-KC.MoRain.main/DemoCore.kt
 */

package io.github.karlatemp.miraikc.demo

import io.github.karlatemp.miraikc.plugin.MainPlugin
import kotlinx.coroutines.launch
import net.mamoe.mirai.event.subscribeAlways
import net.mamoe.mirai.message.MessageEvent
import java.util.logging.Level

// 插件核心入口
// 只需要继承 MainPlugin, 不需要写 plugin.yml
@Suppress("unused")
object DemoCore : MainPlugin() {
    override val name: String
        get() = "Demo plugin"
    override val author: String
        get() = "Karlatemp"
    override val version: String
        get() = "Version"

    override fun onEnable() {
        launch {
            logger.log(Level.INFO, "HiHi! Here is DEMO!")
        }
        logger.log(Level.INFO, "OK~!")
        subscribeAlways<MessageEvent> {
            // Your code here.
        }
    }

    override fun onDisable() {
    }
}

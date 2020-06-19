/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/06/18 18:32:15
 *
 * Mirai-KC/Mirai-KC.mirai-kc.main/bot.kt
 */

package io.github.karlatemp.miraikc

import io.github.karlatemp.miraikc.logging.toMirai
import net.mamoe.mirai.Bot
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.SystemDeviceInfo
import java.io.File
import java.util.logging.Logger

fun initializeBot(
    qq: Long, passwd: String,
    configuration: BotConfiguration.() -> Unit = {}
) = Bot(qq = qq, password = passwd) {
    initialize(qq)
    configuration()
}

fun initializeBot(
    qq: Long, passwdMd5: ByteArray,
    configuration: BotConfiguration.() -> Unit = {}
) = Bot(qq = qq, passwordMd5 = passwdMd5) {
    initialize(qq)
    configuration()
}

private val deviceInfoDir = File("data/devices")
private fun BotConfiguration.initialize(qq: Long) {
    val lazy = lazy {
        "bot.$qq".logger().toMirai()
    }
    this.networkLoggerSupplier = { lazy.value }
    this.deviceInfo
    val deviceInfoFile = File(deviceInfoDir, "$qq.json")
    @Suppress("LiftReturnOrAssignment")
    if (deviceInfoFile.isFile) {
        deviceInfo = deviceInfoFile.readJson()
            .asJsonObject.toDeviceInfo().let { conf -> { conf } }
    } else {
        deviceInfo = SystemDeviceInfo().also { conf ->
            deviceInfoDir.mkdirs()
            deviceInfoFile.writeJson(conf.toJsonElement())
        }.let { conf -> { conf } }
    }
}
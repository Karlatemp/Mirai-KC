/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/06/18 18:32:15
 *
 * Mirai-KC/Mirai-KC.mirai-kc.main/bot.kt
 */

package io.karlatemp.github.mirai

import com.google.gson.JsonParser
import com.google.gson.internal.Streams
import com.google.gson.stream.JsonWriter
import io.karlatemp.github.mirai.logging.toMirai
import net.mamoe.mirai.Bot
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.SystemDeviceInfo
import java.io.ByteArrayOutputStream
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
        Logger.getLogger("bot.$qq").toMirai()
    }
    this.networkLoggerSupplier = { lazy.value }
    this.deviceInfo
    val deviceInfoFile = File(deviceInfoDir, "$qq.json")
    if (deviceInfoFile.isFile) {
        deviceInfo = deviceInfoFile.readBytes().inputStream().reader(Charsets.UTF_8).let { reader ->
            JsonParser.parseReader(reader)
        }.asJsonObject.toDeviceInfo().let { conf -> { conf } }
    } else {
        deviceInfo = SystemDeviceInfo().also { conf ->
            deviceInfoDir.mkdirs()
            deviceInfoFile.writeBytes(
                conf.toJsonElement().let { obj ->
                    ByteArrayOutputStream().also { baos ->
                        JsonWriter(baos.writer(Charsets.UTF_8)).use { jw ->
                            jw.setIndent("  ")
                            jw.isHtmlSafe = false
                            Streams.write(obj, jw)
                        }
                    }.toByteArray()
                }
            )
        }.let { conf -> { conf } }
    }
}
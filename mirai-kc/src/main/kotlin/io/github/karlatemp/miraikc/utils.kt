/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/06/17 21:34:56
 *
 * Mirai-KC/Mirai-KC.main/utils.kt
 */

package io.github.karlatemp.miraikc

import cn.mcres.karlatemp.mxlib.tools.Unsafe
import cn.mcres.karlatemp.mxlib.tools.security.AccessToolkit
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.internal.Streams
import com.google.gson.stream.JsonWriter
import net.mamoe.mirai.utils.ContextImpl
import net.mamoe.mirai.utils.DeviceInfo
import net.mamoe.mirai.utils.DeviceInfoData
import java.io.File
import java.lang.reflect.Modifier
import java.security.MessageDigest
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.reflect.KProperty

operator fun <T> ThreadLocal<T>.getValue(from: Any, property: KProperty<*>): T = get()

operator fun <T> ThreadLocal<T>.setValue(from: Any, property: KProperty<*>, value: T) {
    set(value)
}

val <T : Any> Class<T>.instance: T
    get() {
        val instance = this.kotlin
        instance.objectInstance?.let { return it }
        runCatching {
            getDeclaredMethod("getInstance").takeIf { Modifier.isStatic(it.modifiers) }?.let { met ->
                AccessToolkit.setAccessible(met, true)
                cast(met.invoke(null))?.let { return it }
            }
        }
        runCatching {
            getDeclaredField("INSTANCE").takeIf { Modifier.isStatic(it.modifiers) }?.let { field ->
                AccessToolkit.setAccessible(field, true)
                cast(field.get(null))?.let { return it }
            }
        }
        runCatching {
            val cons = getDeclaredConstructor()
            AccessToolkit.setAccessible(cons, true)
            return cons.newInstance()
        }
        return Unsafe.getUnsafe().allocateInstance(this)
    }

val JsonElement.asByteArray: ByteArray
    get() {
        if (isJsonArray) {
            with(asJsonArray) {
                val array = ByteArray(size())
                forEachIndexed { index, jsonElement ->
                    array[index] = jsonElement.asByte
                }
                return array
            }
        }
        return asString.toByteArray(Charsets.UTF_8)
    }

fun JsonObject.toDeviceInfo(): DeviceInfo {
    return DeviceInfoData(
        display = get("display").asByteArray,
        product = get("product").asByteArray,
        device = get("device").asByteArray,
        board = get("board").asByteArray,
        brand = get("brand").asByteArray,
        model = get("model").asByteArray,
        bootloader = get("bootloader").asByteArray,
        fingerprint = get("fingerprint").asByteArray,
        bootId = get("bootId").asByteArray,
        procVersion = get("procVersion").asByteArray,
        baseBand = get("baseBand").asByteArray,
        version = get("version").asJsonObject.let { ver ->
            DeviceInfoData.VersionData(
                incremental = ver["incremental"].asByteArray,
                release = ver["release"].asByteArray,
                codename = ver["codename"].asByteArray,
                sdk = ver["sdk"].asInt
            )
        },
        simInfo = get("simInfo").asByteArray,
        osType = get("osType").asByteArray,
        macAddress = get("macAddress").asByteArray,
        wifiBSSID = get("wifiBSSID").asByteArray,
        wifiSSID = get("wifiSSID").asByteArray,
        imsiMd5 = get("imsiMd5").asByteArray,
        imei = get("imei").asString,
        apn = get("apn").asByteArray
    ).also { it.context = ContextImpl() }
}

fun DeviceInfo.toJsonElement() = JsonObject().also { obj ->
    with(JsonObjectBuilder(obj)) {
        "display" value display.toString(Charsets.UTF_8)
        "product" value product.toString(Charsets.UTF_8)
        "device" value device.toString(Charsets.UTF_8)
        "board" value board.toString(Charsets.UTF_8)
        "brand" value brand.toString(Charsets.UTF_8)
        "model" value model.toString(Charsets.UTF_8)
        "bootloader" value bootloader.toString(Charsets.UTF_8)
        "fingerprint" value fingerprint.toString(Charsets.UTF_8)
        "bootId" value bootId.toString(Charsets.UTF_8)
        "procVersion" value procVersion.toString(Charsets.UTF_8)
        "baseBand" value baseBand.toString(Charsets.UTF_8)
        "version" obj {
            "codename" value version.codename.toString(Charsets.UTF_8)
            "incremental" value version.incremental.toString(Charsets.UTF_8)
            "release" value version.release.toString(Charsets.UTF_8)
            "sdk" value version.sdk
        }
        "simInfo" value simInfo.toString(Charsets.UTF_8)
        "osType" value osType.toString(Charsets.UTF_8)
        "macAddress" value macAddress.toString(Charsets.UTF_8)
        wifiBSSID?.let {
            "wifiBSSID" value it.toString(Charsets.UTF_8)
        }
        wifiSSID?.let {
            "wifiSSID" value it.toString(Charsets.UTF_8)
        }
        "imsiMd5" array {
            imsiMd5.forEach {
                value(it)
            }
        }
        "imei" value imei
        "androidId" value androidId.toString(Charsets.UTF_8)
        "apn" value apn.toString(Charsets.UTF_8)
    }
}

fun File.readJson(): JsonElement = readBytes().inputStream()
    .reader(Charsets.UTF_8).let { JsonParser.parseReader(it) }

fun File.writeJson(element: JsonElement) {
    JsonWriter(writer(Charsets.UTF_8).buffered(1024)).use { writer ->
        writer.isLenient = true
        writer.isHtmlSafe = false
        writer.setIndent("  ")
        Streams.write(element, writer)
    }
}

fun String.logger(): Logger = Logger.getLogger(this)
fun Logger.level(level: Level): Logger = also { this.level = level }
fun Logger.levelAll(): Logger = level(Level.ALL)

fun ByteArray.md5(): ByteArray {
    return md5(data = this, offset = 0, length = size)
}

fun md5(data: ByteArray, offset: Int, length: Int): ByteArray {
    return MessageDigest.getInstance("MD5").apply {
        update(data, offset, length)
    }.digest()
}

fun String.md5(): ByteArray = toByteArray(Charsets.UTF_8).md5()

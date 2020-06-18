/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/06/18 21:42:35
 *
 * Mirai-KC/Mirai-KC.mirai-kc.main/Main.kt
 */
@file:JvmName("Main")

package io.karlatemp.github.mirai.bootstrap

import com.google.gson.JsonObject
import io.karlatemp.github.mirai.*
import io.karlatemp.github.mirai.logging.initializeLoggingSystem
import kotlinx.coroutines.*
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.Listener
import net.mamoe.mirai.event.subscribeAlways
import net.mamoe.mirai.join
import org.fusesource.jansi.AnsiConsole
import java.io.File
import java.util.logging.Level

private val loginStorage = File("data/login.json")
private val rootScope = CoroutineScope(CoroutineThreadPool)

fun main() {
    if (System.getProperty("kc.no-ansi") === null) {
        AnsiConsole.systemInstall()
    }
    initializeLoggingSystem()
    Bootstrap.initialize()
    startupConsoleThread()

    val joiner: suspend () -> Unit = if (loginStorage.isFile) {
        val data = loginStorage.readJson().asJsonObject
        val qq = data["qq"].asLong
        val passwd = data["passwd-md5"].asByteArray
        initializeBot(qq, passwd).tryLogin()
    } else {
        null.tryLogin()
    }
    runBlocking {
        joiner()
    }
}

private fun Bot?.tryLogin(): suspend () -> Unit {
    this?.apply {
        val result = CompletableDeferred<Boolean>(
            rootScope.coroutineContext[Job]
        )
        rootScope.launch {
            try {
                login()
                result.complete(true)
            } catch (any: Throwable) {
                Bootstrap.logger.log(Level.SEVERE, "Failed to login. Please try agent.", any)
                loginStorage.delete()
                result.complete(false)
            }
        }
        return@tryLogin runBlocking {
            if (result.await()) {
                return@runBlocking this@apply::join
            } else {
                return@runBlocking null.tryLogin()
            }
        }
    } ?: run {
        fun awaitConsoleInput(): String {
            val def = CompletableDeferred<String>()
            rootScope.launch {
                val fx = coroutineContext[Job]!!
                subscribeAlways<ConsoleMessageEvent>(
                    priority = Listener.EventPriority.HIGHEST
                ) {
                    intercept()
                    cancel()
                    def.complete(this.message)
                    rootScope.launch {
                        delay(100)
                        fx.cancel()
                    }
                }
            }
            return runBlocking { return@runBlocking def.await() }
        }

        tailrec fun getQQ(): Long {
            Bootstrap.logger.info("Please enter your qq")
            val qq = awaitConsoleInput().toLongOrNull()
            if (qq != null) return qq
            Bootstrap.logger.warning("Please input a valid qq")
            return getQQ()
        }

        val qq = getQQ()
        Bootstrap.logger.info("Selected qq $qq, Please input your password")
        val passwd = awaitConsoleInput().md5()
        Bootstrap.logger.info("Trying login... Please wait")
        loginStorage.writeJson(JsonObject().build {
            "qq" value qq
            "passwd-md5" array { passwd.forEach { value(it) } }
        })
        return@tryLogin initializeBot(qq, passwd).tryLogin()
    }
    throw AssertionError()
}

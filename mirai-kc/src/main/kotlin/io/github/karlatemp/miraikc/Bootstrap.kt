/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/06/17 21:34:56
 *
 * Mirai-KC/Mirai-KC.main/Bootstrap.kt
 */

package io.github.karlatemp.miraikc

import io.github.karlatemp.miraikc.command.ArgumentParser
import io.github.karlatemp.miraikc.command.Commands
import io.github.karlatemp.miraikc.command.DefaultCommands
import io.github.karlatemp.miraikc.permission.MiraiContextChecker
import io.github.karlatemp.miraikc.permission.PermissionManager
import io.github.karlatemp.miraikc.plugin.PluginManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.subscribeAlways
import net.mamoe.mirai.message.MessageEvent
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.logging.Level
import kotlin.system.exitProcess

@Suppress("MemberVisibilityCanBePrivate")
object Bootstrap {
    val allowedPrefix = BitSet().also {
        with(it) {
            fun set(char: Char) {
                set(char.toInt(), true)
            }
            set('/')
            set('\\')
            set('-')
            set('!')
            set('！')
            set('#')
            set('#')
            set('$')
            set('%')
            set('^')
            set('&')
            set('*')
            set('_')
            set('=')
            set('+')
        }
    }

    @JvmStatic
    val logger = "Bootstrap".logger().levelAll()

    @JvmStatic
    val permissionManager = PermissionManager(
        ConcurrentHashMap(), ConcurrentHashMap()
    )

    @JvmStatic
    internal val usersFolder: File

    @JvmStatic
    internal val groupsFolder: File

    init {
        val permissionsFolder = File("data/perm")
        usersFolder = File(permissionsFolder, "users")
        groupsFolder = File(permissionsFolder, "groups")
        reloadPermissionManager()
        savePermissionManager()
    }

    fun savePermissionManager() {
        logger.log(Level.INFO, "Saving permission manager data.")
        try {
            permissionManager.saveData(usersFolder, groupsFolder)
        } catch (any: Throwable) {
            logger.log(Level.SEVERE, "Exception in saving permission manager's data.")
        }
    }

    fun reloadPermissionManager() {
        logger.log(Level.INFO, "Reloading permission manager data.")
        try {
            permissionManager.reload(usersFolder, groupsFolder)
        } catch (any: Throwable) {
            logger.log(Level.SEVERE, "Exception in reloading permission manager's data.")
        }
    }

    @JvmStatic
    fun initialize() {
        val scope = CoroutineScope(
            CoroutineThreadPool
        )
        reloadPermissionManager()
        PluginManager.reload()
        DefaultCommands.registerDefaultCommands()
        scope.launch {
            while (true) {
                delay(1000L * 60 * 5)
                savePermissionManager()
            }
        }
        scope.subscribeAlways<MessageEvent> {
            scope.launch {
                val user = permissionManager.findUser(sender.id)
                withContext(
                    MiraiContextChecker.newContext(
                        sender, subject, permissionManager
                    ) + user
                ) {
                    val tokens = ArgumentParser.parse(message)
                    val first = tokens.poll() ?: return@withContext
                    val cmd = first.asString
                    if (cmd.isNotEmpty()) {
                        if (allowedPrefix[cmd[0].toInt()]) {
                            Commands.commands[cmd.substring(1).toLowerCase()]?.also { command ->
                                if (user.isBanned) {
                                    subject.sendMessage("不可以!")
                                } else {
                                    command.permission?.let {
                                        if (!user.hasPermission(it)) {
                                            subject.sendMessage("不可以!")
                                            return@withContext
                                        }
                                    }
                                }
                                kotlin.runCatching {
                                    command.invoke(sender, subject, this@subscribeAlways, tokens)
                                }.onFailure {
                                    subject.sendMessage("An error occurred while executing the command")
                                    logger.log(Level.SEVERE, "Exception in executing command.", it)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private val shutdownHook = AtomicBoolean(false)
    fun shutdown() {
        if (!shutdownHook.compareAndSet(false, true)) return
        logger.info("Shut downing")
        kotlinx.coroutines.runBlocking {
            ShutdownEvent().broadcast()
        }
        savePermissionManager()
        PluginManager.disableAll()
        logger.info("Dropping all logon bots")
        Bot.botInstancesSequence.forEach { bot ->
            kotlin.runCatching {
                bot.close()
            }.onFailure {
                logger.log(Level.WARNING, "Exception in closing ${bot.id}", it)
            }
        }
        logger.info("Goodbye.")
        exitProcess(0)
    }
}
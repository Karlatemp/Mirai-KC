/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/06/17 21:34:56
 *
 * Mirai-KC/Mirai-KC.main/Bootstrap.kt
 */

package io.karlatemp.github.mirai

import io.karlatemp.github.mirai.command.ArgumentParser
import io.karlatemp.github.mirai.command.Commands
import io.karlatemp.github.mirai.permission.MiraiContextChecker
import io.karlatemp.github.mirai.permission.PermissionManager
import io.karlatemp.github.mirai.plugin.PluginManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.mamoe.mirai.event.subscribeAlways
import net.mamoe.mirai.message.MessageEvent
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Level
import java.util.logging.Logger

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
    val logger = Logger.getLogger("Bootstrap")

    @JvmStatic
    val permissionManager = PermissionManager(
        ConcurrentHashMap(), ConcurrentHashMap()
    )

    @JvmStatic
    private val usersFolder: File

    @JvmStatic
    private val groupsFolder: File

    init {
        val permissionsFolder = File("data/perm")
        usersFolder = File(permissionsFolder, "users")
        groupsFolder = File(permissionsFolder, "groups")
        reloadPermissionManager()
        savePermissionManager()
    }

    fun savePermissionManager() {
        try {
            permissionManager.saveData(usersFolder, groupsFolder)
        } catch (any: Throwable) {
            logger.log(Level.SEVERE, "Exception in saving permission manager's data.")
        }
    }

    fun reloadPermissionManager() {
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
        scope.subscribeAlways<MessageEvent> {
            scope.launch {
                withContext(
                    MiraiContextChecker.newContext(
                        sender, subject, permissionManager
                    )
                ) {
                    val user = permissionManager.findUser(sender.id)
                    val tokens = ArgumentParser.parse(message)
                    val first = tokens.poll() ?: return@withContext
                    val cmd = first.asString
                    if (cmd.isNotEmpty()) {
                        if (allowedPrefix[cmd[0].toInt()]) {
                            Commands.commands[cmd.substring(1)]?.also { command ->
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
                                command.invoke(sender, subject, this@subscribeAlways, tokens)
                            }
                        }
                    }
                }
            }
        }
    }
}
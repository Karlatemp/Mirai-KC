/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/06/17 21:34:56
 *
 * Mirai-KC/Mirai-KC.main/DefaultCommands.kt
 */

package io.karlatemp.github.mirai.command

import io.karlatemp.github.mirai.Bootstrap
import io.karlatemp.github.mirai.plugin.PluginManager
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.message.MessageEvent
import java.util.*

object DefaultCommands {
    fun registerDefaultCommands() {
        registerCommand(
            name = "reload",
            permission = "command.reload"
        ) { _: User, contact: Contact, _: MessageEvent, _: LinkedList<ArgumentToken> ->
            contact.sendMessage("Dropping commands...")
            Commands.commands.clear()

            contact.sendMessage("Re-register default commands...")
            registerDefaultCommands()

            contact.sendMessage("Reloading Permissing Manager")
            Bootstrap.reloadPermissionManager()

            contact.sendMessage("Reloading plugins...")
            PluginManager.reload()

            contact.sendMessage("Reload finished.")
        }
        registerCommand(
            name = "perm",
            command = Perms
        )
    }
}
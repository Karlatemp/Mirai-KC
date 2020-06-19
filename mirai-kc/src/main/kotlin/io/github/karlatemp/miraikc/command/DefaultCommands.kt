/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/06/17 21:34:56
 *
 * Mirai-KC/Mirai-KC.main/DefaultCommands.kt
 */

package io.github.karlatemp.miraikc.command

import io.github.karlatemp.miraikc.Bootstrap
import io.github.karlatemp.miraikc.plugin.PluginManager
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
        registerCommand(name = "shutdown", permission = "command.shutdown") { _, subject, _, args ->
            if (args.poll()?.asString != "confirm") {
                subject.sendMessage("Are you sure about that? Type `/shutdown confirm` to shutdown system.")
                return@registerCommand
            }
            subject.sendMessage("System shut downing...")
            Bootstrap.shutdown()
        }
    }
}
/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/06/17 21:34:56
 *
 * Mirai-KC/Mirai-KC.main/Commands.kt
 */

package io.github.karlatemp.miraikc.command

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.message.MessageEvent
import java.util.*
import kotlin.collections.HashMap

annotation class KCommand(val value: String)

interface Command {
    suspend operator fun invoke(
        sender: User,
        subject: Contact,
        message: MessageEvent,
        parsed: LinkedList<ArgumentToken>
    )

    val permission: String? get() = null
}

object Commands {
    val commands = HashMap<String, Command>()
}

open class BaseCommand(
    override val permission: String? = null,
    private val invoke: suspend (
        sender: User,
        subject: Contact,
        message: MessageEvent,
        parsed: LinkedList<ArgumentToken>
    ) -> Unit
) : Command {
    override suspend fun invoke(
        sender: User,
        subject: Contact,
        message: MessageEvent,
        parsed: LinkedList<ArgumentToken>
    ) {
        this.invoke.invoke(sender, subject, message, parsed)
    }
}

fun registerCommand(
    name: String,
    permission: String? = null,
    invoke: suspend (
        sender: User,
        subject: Contact,
        message: MessageEvent,
        parsed: LinkedList<ArgumentToken>
    ) -> Unit
) {
    registerCommand(name, newCommand(permission, invoke))
}

fun registerCommand(name: String, command: Command) {
    Commands.commands[name.toLowerCase()] = command
}

fun newCommand(
    permission: String? = null,
    invoke: suspend (
        sender: User,
        subject: Contact,
        message: MessageEvent,
        parsed: LinkedList<ArgumentToken>
    ) -> Unit
) = BaseCommand(permission, invoke)

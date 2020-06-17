/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/06/17 21:34:56
 *
 * Mirai-KC/Mirai-KC.main/Commands.kt
 */

package io.karlatemp.github.mirai.command

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


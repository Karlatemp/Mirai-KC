/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/06/17 21:34:56
 *
 * Mirai-KC/Mirai-KC.main/Console.kt
 */

package io.github.karlatemp.miraikc

import io.github.karlatemp.miraikc.command.ArgumentToken
import io.github.karlatemp.miraikc.command.Commands
import io.github.karlatemp.miraikc.logging.ConsoleSystem
import io.github.karlatemp.miraikc.permission.MiraiContextChecker
import io.github.karlatemp.miraikc.permission.Permissible
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.event.AbstractEvent
import net.mamoe.mirai.event.CancellableEvent
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.ExternalImage
import net.mamoe.mirai.utils.currentTimeSeconds
import java.util.*
import kotlin.concurrent.thread
import kotlin.coroutines.CoroutineContext

object ConsoleUser : User() {
    private val console = "Console".logger().levelAll()
    override val bot: Bot
        get() = TODO("No yet bot")
    override val coroutineContext: CoroutineContext
        get() = TODO("Not yet implemented")
    override val id: Long
        get() = 0
    override val nick: String
        get() = "Console"

    override suspend fun sendMessage(message: Message): MessageReceipt<User> {
        console.info(message.contentToString())
        return MessageReceipt(object : OnlineMessageSource.Outgoing.ToFriend() {
            override val bot: Bot
                get() = TODO("Not yet implemented")
            override val id: Int
                get() = 0
            override val internalId: Int
                get() = 0
            override val originalMessage: MessageChain
                get() = messageChainOf()
            override val sender: Bot
                get() = TODO("Not yet implemented")
            override val target: Friend
                get() = TODO("Not yet implemented")
            override val time: Int
                get() = 0
        }, ConsoleUser, null)
    }

    override fun toString(): String {
        return "Console"
    }

    override suspend fun uploadImage(image: ExternalImage): Image {
        TODO("Uploading a image into console is not supported")
    }

}

private val ROOTPermissible: Permissible = object : Permissible {
    override val isBanned: Boolean
        get() = false

    override suspend fun hasPermission(permission: String): Boolean {
        return true
    }
}
private val contextChecker = MiraiContextChecker.newContext(
    ConsoleUser, ConsoleUser, Bootstrap.permissionManager
)

fun startupConsoleThread() {
    thread(name = "Console Input Thread", isDaemon = true) {
        val jconsole = System.console()
        val scope = CoroutineScope(
            CoroutineThreadPool + contextChecker + ROOTPermissible
        )
        val lineReader = ConsoleSystem.lineReader
        while (true) {
            val nextCommand = lineReader(false) ?: break
            scope.launch { postCommand(nextCommand) }
        }
    }
}

suspend fun postCommand(nextCommand: String) {
    if (nextCommand.isEmpty()) return
    if (ConsoleMessageEvent(nextCommand).also { it.broadcast() }.isCancelled) return
    val arguments = LinkedList(
        nextCommand.split(' ').map { ArgumentToken(it) }
    )
    val cmd = arguments.poll()?.asString ?: return
    val c = Commands.commands[cmd.toLowerCase()]
    if (c == null) {
        ConsoleUser.sendMessage("Command [$cmd] not found.")
    } else {
        c.invoke(ConsoleUser, ConsoleUser, ConsoleMessage(nextCommand), arguments)
    }
}

class ConsoleMessage(message: String) : MessageEvent() {
    override val bot: Bot
        get() = TODO("Not yet bot")
    override val message: MessageChain = message.toMessage().asMessageChain()
    override val sender: User
        get() = ConsoleUser
    override val senderName: String
        get() = "Console"
    override val subject: Contact
        get() = ConsoleUser
    override val time: Int = currentTimeSeconds.toInt()

}

class ConsoleMessageEvent(val message: String) : AbstractEvent(), CancellableEvent

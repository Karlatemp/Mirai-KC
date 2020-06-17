/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/06/17 21:34:56
 *
 * Mirai-KC/Mirai-KC.main/Console.kt
 */

package io.karlatemp.github.mirai

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.ExternalImage
import java.util.logging.Logger
import kotlin.coroutines.CoroutineContext

object ConsoleUser : User() {
    private val console = Logger.getLogger("Console")
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
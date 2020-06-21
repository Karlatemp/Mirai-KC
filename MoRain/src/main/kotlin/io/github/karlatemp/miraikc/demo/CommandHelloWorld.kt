/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/06/21 13:06:49
 *
 * Mirai-KC/Mirai-KC.MoRain.main/CommandHelloWorld.kt
 */

package io.github.karlatemp.miraikc.demo

import io.github.karlatemp.miraikc.command.ArgumentToken
import io.github.karlatemp.miraikc.command.Command
import io.github.karlatemp.miraikc.command.RCommand
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.message.MessageEvent
import java.util.*

@RCommand("helloworld")
object CommandHelloWorld : Command {
    override suspend fun invoke(
        sender: User,
        subject: Contact,
        message: MessageEvent,
        parsed: LinkedList<ArgumentToken>
    ) {
        subject.sendMessage("Hello World!")
        subject.sendMessage("Arguments: " + parsed.joinToString(", "))
    }
}
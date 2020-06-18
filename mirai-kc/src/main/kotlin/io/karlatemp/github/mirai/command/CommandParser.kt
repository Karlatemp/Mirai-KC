/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/06/17 21:34:56
 *
 * Mirai-KC/Mirai-KC.main/CommandParser.kt
 */

package io.karlatemp.github.mirai.command

import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.PlainText
import java.util.*

open class ArgumentToken(
    open val value: Any?
) {
    open val asBoolean: Boolean get() = asString.toBoolean()
    open val asString: String get() = value.toString()
    open val asLong: Long? get() = asString.toLongOrNull()
    open val asLongNotNull: Long get() = asLong ?: 0L
    open val asInt: Int? get() = asString.toIntOrNull()
    open val asIntNotNull: Int get() = asInt ?: 0
    override fun toString(): String {
        return "Token{$value}"
    }
}

open class ArgumentAtToken(
    private val at: At
) : ArgumentToken(at) {
    override val asString: String
        get() = at.contentToString()
    override val asLong: Long
        get() = at.target
    override val asInt: Int
        get() = at.target.toInt()
    override val asIntNotNull: Int
        get() = at.target.toInt()
    override val asLongNotNull: Long
        get() = at.target

    override fun toString(): String {
        return "At{$at}"
    }
}

open class ArgumentImageToken(
    override val value: Image
) : ArgumentToken(value)


@Suppress("MemberVisibilityCanBePrivate")
object ArgumentParser {
    @JvmStatic
    fun parse(messages: MessageChain): LinkedList<ArgumentToken> {
        val tokens = LinkedList<ArgumentToken>()
        val buffer = StringBuilder()
        for (msg in messages) {
            when (msg) {
                is Image -> {
                    if (buffer.isNotEmpty()) {
                        parse(buffer.toString(), tokens)
                        buffer.setLength(0)
                    }
                    tokens.add(ArgumentImageToken(msg))
                }
                is At -> {
                    if (buffer.isNotEmpty()) {
                        parse(buffer.toString(), tokens)
                        buffer.setLength(0)
                    }
                    tokens.add(ArgumentAtToken(msg))
                }
                is PlainText -> {
                    buffer.append(msg.content)
                }
            }
        }
        if (buffer.isNotEmpty()) {
            parse(buffer.toString(), tokens)
        }
        return tokens
    }

    @JvmStatic
    fun <T : LinkedList<ArgumentToken>> parse(line: String, tokens: T): T {
        @Suppress("NAME_SHADOWING") val line = line.trim()
        var start = 0
        do {
            val index = line.indexOf(' ', start)
            start = if (index == -1) {
                tokens.add(ArgumentToken(line.substring(start)))
                break
            } else {
                tokens.add(ArgumentToken(line.substring(start, index)))
                index + 1
            }
        } while (true)
        return tokens
    }

    @JvmStatic
    fun parse(line: String): LinkedList<ArgumentToken> {
        return parse(line, LinkedList())
    }
}

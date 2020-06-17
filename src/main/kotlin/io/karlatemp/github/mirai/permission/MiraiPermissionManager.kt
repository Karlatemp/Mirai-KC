/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/06/17 21:34:56
 *
 * Mirai-KC/Mirai-KC.main/MiraiPermissionManager.kt
 */

package io.karlatemp.github.mirai.permission

import io.karlatemp.github.mirai.ConsoleUser
import net.mamoe.mirai.contact.*

class MiraiContextChecker(
        private val sender: User,
        private val subject: Contact
) : ContextChecker {
    override fun invoke(key: String, value: String): Boolean {
        return when (key) {
            "context" -> when (value) {
                "group" -> subject is Group
                "private" -> subject is User
                "console" -> subject is ConsoleUser
                else -> false
            }
            "level" -> when (value) {
                "admin" -> (sender as? Member)?.isOperator() ?: false
                else -> sender is ConsoleUser
            }
            else -> false
        }
    }

    companion object {
        @JvmStatic
        fun newContext(sender: User, subject: Contact, groupFinder: GroupFinder): WrappedPermissionContext = WrappedPermissionContext(MiraiContextChecker(sender, subject), groupFinder)
    }
}

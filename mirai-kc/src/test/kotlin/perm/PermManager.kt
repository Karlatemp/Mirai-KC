/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/06/17 22:40:12
 *
 * Mirai-KC/Mirai-KC.mirai-kc.test/PermManager.kt
 */

package perm

import io.github.karlatemp.miraikc.permission.GroupNode
import io.github.karlatemp.miraikc.permission.PermNode
import io.github.karlatemp.miraikc.permission.PermissionManager
import java.io.File
import java.util.concurrent.ConcurrentHashMap

fun main() {
    val defaultManager = PermissionManager(ConcurrentHashMap(), ConcurrentHashMap())
    defaultManager.findOrCreateUser(10086L)
    defaultManager.groups["admin"] = GroupNode().also {
        it.nodes.add(PermNode("command.*", true, mapOf()))
    }
    defaultManager.saveData(users = File("data/perm/users"), groups = File("data/perm/groups"))
}
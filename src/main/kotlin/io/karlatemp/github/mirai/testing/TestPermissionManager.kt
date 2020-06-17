/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/06/10 17:51:10
 *
 * MiraiPlugins/MiraiBootstrap/TestPermissionManager.kt
 */

package io.karlatemp.github.mirai.testing

import io.karlatemp.github.mirai.permission.GroupNode
import io.karlatemp.github.mirai.permission.PermNode
import io.karlatemp.github.mirai.permission.PermissionManager
import io.karlatemp.github.mirai.permission.WrappedPermissionContext
import java.io.File
import java.util.concurrent.ConcurrentHashMap

fun main() {
    val native = PermissionManager(ConcurrentHashMap(), ConcurrentHashMap())
    val accessor = WrappedPermissionContext({ _, _ -> true }, native)
    val data = File("data/testing")
    val users = File(data, "users")
    val groups = File(data, "groups")
    native.reload(users, groups)
    native.groups.computeIfAbsent("default") {
        GroupNode().also {
            it.nodes.add(PermNode("test", true, mapOf()))
        }
    }
    println(native.groups)
    native.findOrCreateUser(3279826484L).also { println(it) }.accessGet(
            accessor, "test"
    ).let { println(it) }
    native.saveData(users, groups)
}
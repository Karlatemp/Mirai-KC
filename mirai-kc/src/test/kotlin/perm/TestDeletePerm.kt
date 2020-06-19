/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/06/18 24:10:54
 *
 * Mirai-KC/Mirai-KC.mirai-kc.test/TestDeletePerm.kt
 */

package perm

import io.github.karlatemp.miraikc.permission.PermissionManager
import java.io.File
import java.util.concurrent.ConcurrentHashMap


fun main() {
    val defaultManager = PermissionManager(ConcurrentHashMap(), ConcurrentHashMap())
    val users = File("data/perm/users")
    val groups = File("data/perm/groups")

    defaultManager.reload(users = users, groups = groups)

    defaultManager.groups.remove("test")

    defaultManager.saveData(users = users, groups = groups)
}
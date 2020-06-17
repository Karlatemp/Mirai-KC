/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/06/17 21:34:56
 *
 * Mirai-KC/Mirai-KC.main/PermissionManager.kt
 */

package io.karlatemp.github.mirai.permission

import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

interface Permissible {
    val isBanned: Boolean

    suspend fun hasPermission(permission: String): Boolean
}

abstract class PermissibleImpl : Permissible {
    override suspend fun hasPermission(permission: String): Boolean {
        return (
                coroutineContext[PermissionContext.Key] ?: error("No PermissionContext found in current context")
                ).hasPermission0(permission)
    }

    private tailrec fun PermissionContext.hasPermission0(permission: String): Boolean {
        if (permission.isEmpty()) {
            return get(permission).asBoolean
        }
        val status = get(permission)
        if (status == PermissionStatus.ALLOWED) return true
        if (status == PermissionStatus.BLOCKED) return false
        if (permission == "*") return false
        val lsp = permission.lastIndexOf('.', startIndex = if (permission.endsWith(".*")) {
            permission.lastIndex - 2
        } else {
            val status0 = get("$permission.*")
            if (status0 == PermissionStatus.ALLOWED) return true
            if (status0 == PermissionStatus.BLOCKED) return false
            permission.lastIndex
        })
        return if (lsp == -1) {
            hasPermission0("*")
        } else {
            hasPermission0(permission.substring(0, lsp) + ".*")
        }
    }

    internal abstract fun PermissionContext.get(permission: String): PermissionStatus
    internal fun accessGet(context: PermissionContext, permission: String): PermissionStatus =
            context.get(permission)
}


class UserNode(
        val nodes: ConcurrentLinkedQueue<PermNode>,
        val groups: ConcurrentLinkedQueue<GroupSetNode>,
        override var isBanned: Boolean
) : PermissibleImpl() {

    override fun PermissionContext.get(permission: String): PermissionStatus {
        if (isBanned) return PermissionStatus.BLOCKED
        get(nodes, permission)?.also { return@get it }
        groups.forEach fr@{ group ->
            val g = findGroup(group.group) ?: return@fr
            group.content.forEach { (t, u) ->
                if (!checkContext(t, u)) return@fr
            }
            g.accessGet(this, permission).takeUnless { it == PermissionStatus.UNSET }?.also {
                return@get it
            }
        }
        return PermissionStatus.UNSET
    }

    override fun toString(): String {
        return "User{groups=$groups, nodes=$nodes, isBanned=$isBanned}"
    }
}

private fun PermissionContext.get(nodes: Collection<PermNode>, permission: String): PermissionStatus? {
    nodes.forEach root@{ node ->
        if (node.key == permission) {
            node.content.forEach { (t, u) ->
                if (!checkContext(t, u)) return@root
            }
            return@get PermissionStatus.fromStatus(node.value)
        }
    }
    return null
}

class GroupNode : PermissibleImpl() {
    val nodes = ConcurrentLinkedQueue<PermNode>()
    override val isBanned: Boolean
        get() = false

    override fun PermissionContext.get(permission: String): PermissionStatus {
        return get(nodes, permission) ?: PermissionStatus.UNSET
    }

    override fun toString(): String {
        return "$nodes"
    }
}

open class GroupSetNode(val group: String, val content: Map<String, String>) {
    override fun toString(): String {
        return "$group=$content"
    }
}

open class PermNode(val key: String, val value: Boolean, val content: Map<String, String>) {
    override fun toString(): String {
        return "{$key=$value, $content}"
    }
}

internal enum class PermissionStatus {
    ALLOWED, UNSET, BLOCKED;

    companion object {
        @JvmStatic
        fun fromStatus(status: Boolean): PermissionStatus = if (status) ALLOWED else BLOCKED
    }

    val asBoolean: Boolean get() = this == ALLOWED
}

abstract class PermissionContext : CoroutineContext.Element {
    companion object Key : CoroutineContext.Key<PermissionContext>

    final override val key: CoroutineContext.Key<*>
        get() = Key

    abstract fun checkContext(key: String, value: String): Boolean
    abstract fun findGroup(name: String): GroupNode?
}

object StandardContext {
    val newStandardUser
        get() = UserNode(
                ConcurrentLinkedQueue(),
                ConcurrentLinkedQueue(listOf(
                        GroupSetNode("default", mapOf()),
                        GroupSetNode("group", mapOf(
                                "context" to "group"
                        )),
                        GroupSetNode("group-admin", mapOf(
                                "context" to "group",
                                "level" to "admin"
                        ))
                )),
                false
        )

    @JvmStatic
    val standardUser = newStandardUser
}


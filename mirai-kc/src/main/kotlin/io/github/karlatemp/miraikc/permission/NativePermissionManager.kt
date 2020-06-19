/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/06/17 21:34:56
 *
 * Mirai-KC/Mirai-KC.main/NativePermissionManager.kt
 */

@file:Suppress("FunctionName")

package io.github.karlatemp.miraikc.permission

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.internal.Streams
import com.google.gson.stream.JsonWriter
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

typealias ContextChecker = (key: String, value: String) -> Boolean
typealias GroupFinder = (name: String) -> GroupNode?

class WrappedPermissionContext(
    var contextChecker: ContextChecker,
    var groupFinder: GroupFinder
) : PermissionContext() {
    override fun checkContext(key: String, value: String): Boolean = contextChecker(key, value)

    override fun findGroup(name: String): GroupNode? = groupFinder(name)
}

private val idGetter = """^(.+)\.json$""".toRegex()

// Used gson
open class PermissionManager(
    val groups: ConcurrentHashMap<String, GroupNode>,
    val users: ConcurrentHashMap<Long, UserNode>
) : GroupFinder {

    override fun invoke(name: String): GroupNode? = groups[name]

    fun findUser(user: Long): UserNode = users[user] ?: StandardContext.standardUser

    fun findOrCreateUser(user: Long): UserNode = users.computeIfAbsent(user) {
        StandardContext.newStandardUser
    }

    fun reload(users: File, groups: File) {
        this.users.clear()
        this.groups.clear()
        groups.listFiles { file -> file.isFile && file.extension == "json" }?.forEach { file ->
            idGetter.find(file.name)?.also { result ->
                val theId = result.groupValues[1]
                val data = file.readElement()
                this.groups[theId] = data.toGroupNode()
            }
        }
        users.listFiles { file -> file.isFile && file.extension == "json" }?.forEach { file ->
            idGetter.find(file.name)?.also { result ->
                val theId = result.groupValues[1].toLongOrNull() ?: return@also
                val data = file.readElement()
                this.users[theId] = data.toUserNode()
            }
        }
    }

    fun saveData(users: File, groups: File) {
        users.mkdirs()
        groups.mkdirs()
        this.users.forEach { (id, node) ->
            node.toJson() writeTo File(users, "$id.json")
        }
        this.groups.forEach { (id, node) ->
            node.toJson() writeTo File(groups, "$id.json")
        }
        users.listFiles { file -> file.isFile && file.extension == "json" }?.forEach { file ->
            idGetter.find(file.name)?.also { result ->
                val theId = result.groupValues[1].toLongOrNull() ?: return@also
                if (!this.users.containsKey(theId)) {
                    file.delete()
                }
            }
        }
        groups.listFiles { file -> file.isFile && file.extension == "json" }?.forEach { file ->
            idGetter.find(file.name)?.also { result ->
                val theId = result.groupValues[1]
                if (!this.groups.containsKey(theId)) {
                    file.delete()
                }
            }
        }
    }
}

private infix fun JsonElement.writeTo(file: File) {
    JsonWriter(file.bufferedWriter(Charsets.UTF_8)).use { writer ->
        writer.isHtmlSafe = false
        writer.isLenient = false
        writer.setIndent("    ")
        Streams.write(this, writer)
    }
}

private fun File.readElement(): JsonElement =
    reader(Charsets.UTF_8).use { JsonParser.parseReader(it) }


private fun UserNode.toJson(): JsonElement {
    val o = JsonObject()
    o.add("nodes", JsonArray().also { array -> nodes.forEach { array.add(it.toJson()) } })
    o.add("groups", JsonObject().also { jo ->
        this.groups.forEach { g ->
            jo.add(g.group, g.content.toJson() ?: JsonObject())
        }
    })
    o.addProperty("isBanned", isBanned)
    return o
}

private fun JsonElement.toUserNode(): UserNode {
    with(asJsonObject) {
        return UserNode(
            ConcurrentLinkedQueue<PermNode>().also { perms ->
                get("nodes").asJsonArray.forEach {
                    perms.add(it.asJsonObject.toPermNode())
                }
            },
            ConcurrentLinkedQueue<GroupSetNode>().also { sets ->
                getAsJsonObject("groups")?.entrySet()?.forEach { (k, v) ->
                    sets.add(GroupSetNode(k, v.`toMap{String,String}`()))
                }
            },
            getAsJsonPrimitive("isBanned").asBoolean
        )
    }
}

private fun GroupNode.toJson(): JsonElement =
    JsonArray().also { array -> nodes.forEach { array.add(it.toJson()) } }

private fun JsonElement.toGroupNode(): GroupNode {
    return GroupNode().also { node ->
        asJsonArray.forEach { element ->
            node.nodes.add(element.asJsonObject.toPermNode())
        }
    }
}


@JvmName("toMap_String_String")
private fun JsonElement?.`toMap{String,String}`(): Map<String, String> {
    val map = HashMap<String, String>()
    if (this == null) return map
    asJsonObject.entrySet().forEach { (k, v) ->
        map[k] = v.asString
    }
    return map
}

private fun JsonObject.toPermNode(): PermNode {
    return PermNode(
        getAsJsonPrimitive("key").asString,
        getAsJsonPrimitive("value").asBoolean,
        getAsJsonObject("context").`toMap{String,String}`()
    )
}

private fun Map<String, String>.toJson(): JsonObject? =
    this.takeIf { it.isNotEmpty() }?.let { ct ->
        JsonObject().also { ct0 ->
            ct.forEach { (t, u) ->
                ct0.addProperty(t, u)
            }
        }
    }

private fun PermNode.toJson(): JsonObject =
    JsonObject().also { obj ->
        obj.addProperty("key", this.key)
        obj.addProperty("value", this.value)
        obj.add("context", content.toJson() ?: return@also)
    }
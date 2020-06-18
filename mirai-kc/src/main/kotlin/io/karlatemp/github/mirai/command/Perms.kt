/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/06/17 23:04:15
 *
 * Mirai-KC/Mirai-KC.mirai-kc.main/Perms.kt
 */

package io.karlatemp.github.mirai.command

import io.karlatemp.github.mirai.Bootstrap
import io.karlatemp.github.mirai.permission.*
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.message.MessageEvent
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue


// Kar Perms
/*
* /perm groups
* /perm group [group]
*   |- info
*   |- permission
*   |   |- set   [node] [true/false] [contexts]
*   |   |- unset [node]
* /perm addgroup [group] [context]
* /perm delgroup [group] [context]
* /perm users
* /perm user [qq]
*   |- info
*   |- addgroup [group] [contexts]
*   |- delgroup [group] [contexts]
*   |- permission
*   |   |- set   [node] [true/false] [contexts]
*   |   |- unset [node]
*   |- reset
* /perm save
*/

/*
 * User: ${user}
 * Permissions:
 *   - perm.perm = true
 *   - perm.perm = true
 *      |  level = admin
 * Groups:
 *   - default
 *   - qq-group
 *      | context = group
 */

/*
 * Group: ${group}
 * Permissions:
 *   - perm.perm = true
 *   - perm.perm = true
 *      |  level = admin
 * Members:
 *   - ${qq}
 *   - q2
 *      |  level = admin
 */
@Suppress("DuplicatedCode")
object Perms : Command {

    private const val BASE_PERMISSION = "command.perm"
    private const val HELP_GROUPS = "/perm groups"
    private const val HELP_GROUP = """/perm group [group]
    |- info
    |- permission
    |   |- set   [node] [true/false] [contexts]
    |   |- unset [node]
"""
    private const val HELP_ADDGROUP = "/perm addgroup [group]"
    private const val HELP_DELGROUP = "/perm delgroup [group]"
    private const val HELP_USERS = "/perm users"
    private const val HELP_SAVE = "/perm reload"
    private const val HELP_RELOAD = "/perm save"
    private const val HELP_USER = """/perm user [qq]
    |- info
    |- addgroup [group] [contexts]
    |- delgroup [group] [contexts]
    |- permission
    |   |- set   [node] [true/false] [contexts]
    |   |- unset [node]
    |- reset
"""
    private const val UNKNOWN_SUB_COMMAND = "Unknown sub command."

    override suspend fun invoke(
        sender: User,
        subject: Contact,
        message: MessageEvent,
        parsed: LinkedList<ArgumentToken>
    ) {
        if (parsed.isEmpty()) {
            subject.sendMessage("This system is using KlPerms")
            if (BASE_PERMISSION.testPermission()) {
                sendHelp(subject)
            } else {
                subject.sendMessage("You don't have the permission to do that.")
            }
        } else {
            if (!"$BASE_PERMISSION.${parsed.peek().asString}".testPermission()) {
                subject.sendMessage("You don't have the permission to do that.")
                return
            }
            val permissionManager = Bootstrap.permissionManager
            val groups = permissionManager.groups
            when (parsed.poll().asString) {
                "save" -> {
                    Bootstrap.savePermissionManager()
                    subject.sendMessage("Successful save permission data.")
                }
                "reload" -> {
                    Bootstrap.reloadPermissionManager()
                    subject.sendMessage("Successful reload permission data.")
                }
                "groups" -> {
                    if (groups.isEmpty()) {
                        subject.sendMessage("No any group available")
                    } else {
                        subject.sendMessage(
                            "All available groups:\n" + groups.keys.joinToString(", ")
                        )
                    }
                }
                "group" -> {
                    val target = parsed.poll()?.asString ?: run {
                        subject.sendMessage(HELP_GROUP)
                        return
                    }
                    val group = groups[target] ?: run {
                        subject.sendMessage("Group [$target] not available. Please create it first.")
                        return
                    }
                    when (parsed.poll()?.asString ?: run {
                        subject.sendMessage(HELP_GROUP)
                        return
                    }) {
                        "info" -> {
                            subject.sendMessage(buildString {
                                append("Group: ").append(target)
                                append("\nPermissions:\n")
                                group.nodes.forEach { permissionNode ->
                                    append("  - ${permissionNode.key} = ${permissionNode.value}\n")
                                    permissionNode.content.forEach { (t, u) ->
                                        append("     |  $t = $u\n")
                                    }
                                }
                                append("Members:\n")
                                permissionManager.users.forEach { (qq: Long, un: UserNode) ->
                                    val groupNode = un.groups.first { it.group == target }
                                    if (groupNode != null) {
                                        append("  - $qq\n")
                                        groupNode.content.forEach { (t, u) ->
                                            append("     |  $t = $u\n")
                                        }
                                    }
                                }
                            })
                        }
                        "permission" -> {
                            exexutePermission(subject, parsed, HELP_GROUP, group.nodes, target)
                        }
                        else -> subject.sendMessage(UNKNOWN_SUB_COMMAND)
                    }
                }
                "addgroup" -> {
                    val name = parsed.poll()?.asString ?: run {
                        subject.sendMessage(HELP_ADDGROUP)
                        return
                    }
                    if (groups.containsKey(name)) {
                        subject.sendMessage("Group [$name] is exists.")
                        return
                    }
                    groups[name] = GroupNode()
                    subject.sendMessage("Successful to create new group [$name].")
                }
                "delgroup" -> {
                    val name = parsed.poll()?.asString ?: run {
                        subject.sendMessage(HELP_DELGROUP)
                        return
                    }
                    groups.remove(name)
                    File(Bootstrap.groupsFolder, "$name.json").delete()
                    subject.sendMessage("Successful to remove group [$name].")
                }
                "users" -> {
                    val users = permissionManager.users
                    subject.sendMessage(if (users.isEmpty()) {
                        "No any user recorded."
                    } else {
                        buildString {
                            append("All recorded users:\n")
                            users.keys.joinTo(this, ", ")
                        }
                    })
                }
                "user" -> {
                    val qq = parsed.poll()?.asLong ?: run {
                        subject.sendMessage(HELP_USER)
                        return
                    }
                    val userNode: UserNode
                    when ((parsed.poll()?.asString ?: run {
                        subject.sendMessage(HELP_USER)
                        return
                    }).also { userNode = permissionManager.findUser(qq) }) {
                        "info" -> {
                            subject.sendMessage(buildString {
                                append("User: $qq")
                                if (userNode === StandardContext.standardUser) {
                                    append(" (Standard User)")
                                }

                                append("\nPermissions:\n")
                                userNode.nodes.forEach { permissionNode ->
                                    append("  - ${permissionNode.key} = ${permissionNode.value}\n")
                                    permissionNode.content.forEach { (t, u) ->
                                        append("     |  $t = $u\n")
                                    }
                                }

                                append("Groups:\n")
                                userNode.groups.forEach { groupNode ->
                                    append("  - ${groupNode.group}\n")
                                    groupNode.content.forEach { (t, u) ->
                                        append("     |  $t = $u\n")
                                    }
                                }
                            })
                        }
                        "addgroup" -> {
                            val groupName = parsed.poll()?.asString ?: run {
                                subject.sendMessage(HELP_USER)
                                return
                            }
                            if (userNode.groups.any { it.group == groupName }) {
                                subject.sendMessage("Failed: User $qq was joined group $groupName")
                                return
                            }
                            @Suppress("NAME_SHADOWING")
                            val userNode = permissionManager.findOrCreateUser(qq)
                            userNode.groups.add(GroupSetNode(groupName, mutableMapOf<String, String>().also { content ->
                                parsed.forEach { argument ->
                                    val line = argument.asString.split("=")
                                    if (line.size != 2) {
                                        subject.sendMessage("Invalid context " + argument.asString)
                                    } else {
                                        content[line[0]] = line[1]
                                    }
                                }
                            }))
                            subject.sendMessage("Successful add group [$groupName] into $qq")
                        }
                        "delgroup" -> {
                            val groupName = parsed.poll()?.asString ?: run {
                                subject.sendMessage(HELP_USER)
                                return
                            }
                            if (!(userNode.groups.any { it.group == groupName })) {
                                subject.sendMessage("Failed: User $qq was not in group $groupName")
                                return
                            }
                            @Suppress("NAME_SHADOWING")
                            val userNode = permissionManager.findOrCreateUser(qq)
                            userNode.groups.removeIf { it.group == groupName }
                            subject.sendMessage("Successful remove group [$groupName] from $qq")
                        }
                        "permission" -> {
                            exexutePermission(
                                subject, parsed, HELP_USER,
                                permissionManager.findOrCreateUser(qq).nodes,
                                qq
                            )
                        }
                        "reset" -> {
                            permissionManager.users.remove(qq)
                            subject.sendMessage("Successful reset $qq")
                        }
                        else -> subject.sendMessage(UNKNOWN_SUB_COMMAND)
                    }
                }
            }
        }
    }

    private suspend fun sendHelp(subject: Contact) {
        val helps = LinkedList<String>()
        if (hasPermission("$BASE_PERMISSION.user")) {
            helps.add(HELP_USER)
        }
        if (hasPermission("$BASE_PERMISSION.delgroup")) {
            helps.add(HELP_DELGROUP)
        }
        if (hasPermission("$BASE_PERMISSION.addgroup")) {
            helps.add(HELP_ADDGROUP)
        }
        if (hasPermission("$BASE_PERMISSION.group")) {
            helps.add(HELP_GROUP)
        }
        if (hasPermission("$BASE_PERMISSION.groups")) {
            helps.add(HELP_GROUPS)
        }
        if (hasPermission("$BASE_PERMISSION.users")) {
            helps.add(HELP_USERS)
        }

        if (hasPermission("$BASE_PERMISSION.reload")) {
            helps.add(HELP_RELOAD)
        }
        if (hasPermission("$BASE_PERMISSION.save")) {
            helps.add(HELP_SAVE)
        }
        if (helps.isNotEmpty()) {
            subject.sendMessage(helps.joinToString("\n"))
        }
    }

    private suspend fun exexutePermission(
        subject: Contact,
        parsed: LinkedList<ArgumentToken>,
        HELP: String,
        nodes: ConcurrentLinkedQueue<PermNode>,
        target: Any
    ) {
        when (parsed.poll()?.asString ?: run {
            subject.sendMessage(HELP)
            return
        }) {
            "set" -> {
                if (parsed.size < 2) {
                    subject.sendMessage(HELP)
                    return
                }
                val perm = parsed.poll().asString
                val value = parsed.poll().asBoolean
                var permissionNode = nodes.first {
                    it.key == perm
                } ?: run {
                    PermNode(perm, value, mutableMapOf())
                        .also { nodes.add(it) }
                }
                if (permissionNode.value != value) {
                    nodes.remove(permissionNode)
                    permissionNode = PermNode(perm, value, permissionNode.content)
                        .also { nodes.add(it) }
                }
                if (parsed.isNotEmpty()) {
                    while (true) {
                        val content = permissionNode.content
                        if (content is MutableMap<*, *>) {
                            @Suppress("NAME_SHADOWING")
                            val content = content as MutableMap<String, String>
                            parsed.forEach { argument ->
                                val line = argument.asString.split("=")
                                if (line.size != 2) {
                                    subject.sendMessage("Invalid context " + argument.asString)
                                } else {
                                    content[line[0]] = line[1]
                                }
                            }
                            break
                        } else {
                            nodes.remove(permissionNode)
                            permissionNode = PermNode(perm, value, mutableMapOf())
                                .also { nodes.add(it) }
                        }
                    }
                }
                subject.sendMessage("Successful add $perm = $value into $target")
            }
            "unset" -> {
                val perm = parsed.poll()?.asString ?: run {
                    subject.sendMessage(HELP_GROUP)
                    return
                }
                nodes.first { it.key == perm }?.also {
                    nodes.remove(it)
                    subject.sendMessage("Successful remove node $perm")
                } ?: run {
                    subject.sendMessage("Failed: No perm node: $perm")
                }
            }
            else -> subject.sendMessage(UNKNOWN_SUB_COMMAND)
        }
    }

    override val permission: String? = null
}
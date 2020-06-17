/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/06/11 12:45:22
 *
 * MiraiPlugins/MiraiBootstrap/utils.kt
 */

package io.karlatemp.github.mirai

import cn.mcres.karlatemp.mxlib.tools.Unsafe
import cn.mcres.karlatemp.mxlib.tools.security.AccessToolkit
import java.lang.reflect.Modifier
import kotlin.reflect.KProperty

operator fun <T> ThreadLocal<T>.getValue(from: Any, property: KProperty<*>): T = get()

operator fun <T> ThreadLocal<T>.setValue(from: Any, property: KProperty<*>, value: T) {
    set(value)
}

val <T : Any> Class<T>.instance: T
    get() {
        val instance = this.kotlin
        instance.objectInstance?.let { return it }
        runCatching {
            getDeclaredMethod("getInstance").takeIf { Modifier.isStatic(it.modifiers) }?.let { met ->
                AccessToolkit.setAccessible(met, true)
                cast(met.invoke(null))?.let { return it }
            }
        }
        runCatching {
            getDeclaredField("INSTANCE").takeIf { Modifier.isStatic(it.modifiers) }?.let { field ->
                AccessToolkit.setAccessible(field, true)
                cast(field.get(null))?.let { return it }
            }
        }
        runCatching {
            val cons = getDeclaredConstructor()
            AccessToolkit.setAccessible(cons, true)
            return cons.newInstance()
        }
        return Unsafe.getUnsafe().allocateInstance(this)
    }

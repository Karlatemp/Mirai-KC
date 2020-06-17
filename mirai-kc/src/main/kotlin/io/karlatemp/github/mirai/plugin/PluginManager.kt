/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/06/17 21:34:56
 *
 * Mirai-KC/Mirai-KC.main/PluginManager.kt
 */

package io.karlatemp.github.mirai.plugin

import io.karlatemp.github.mirai.CoroutineThreadPool
import io.karlatemp.github.mirai.getValue
import io.karlatemp.github.mirai.instance
import io.karlatemp.github.mirai.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import java.io.File
import java.net.URLClassLoader
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.jar.JarFile
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.coroutines.CoroutineContext

object PluginManager {
    var jarFile: JarFile? by ThreadLocal()

    @JvmField
    val plugins = ConcurrentLinkedQueue<MainPlugin>()

    @JvmField
    val parent: ClassLoader? = PluginManager::class.java.classLoader

    @JvmStatic
    private val logger: Logger = Logger.getLogger("Plugin Manager")

    @JvmStatic
    private val pluginsFolder = File("plugins")

    @JvmStatic
    fun reload() {
        plugins.forEach { pl ->
            (pl.coroutineContext[Job] ?: run {
                System.err.println("Job of $pl not found!")
                return@forEach
            }).also {
                println("Cancelling $it")
            }.cancel()
        }
        plugins.clear()
        pluginsFolder.mkdirs()
        pluginsFolder.listFiles { file -> file.isFile && file.extension == "jar" }?.forEach { it.load() }
    }

    @JvmStatic
    private fun File.load() {
        kotlin.runCatching {
            URLClassLoader(arrayOf(this.toURI().toURL()), this@PluginManager.parent).use { loader ->
                JarFile(this).use { jar ->
                    this@PluginManager.jarFile = jar
                    jar.entries().iterator().forEach { entry ->
                        if (entry.name.endsWith(".class")) {
                            val cname = entry.name.let { it.substring(0, it.length - 6) }.replace('/', '.')
                            val ks = Class.forName(cname, false, loader)
                            if (MainPlugin::class.java.isAssignableFrom(ks)) {
                                val pl = ks.instance as MainPlugin
                                plugins.add(pl.also { plugin ->
                                    plugin.coroutineContext[Job]!!.invokeOnCompletion {
                                        plugin.onDisable()
                                    }
                                })
                                pl.onEnable()
                            }
                            if (AutoInitializer::class.java.isAssignableFrom(ks)) {
                                (ks.instance as AutoInitializer).initialize()
                            }
                        }
                    }
                }
                jarFile = null
            }
        }.onFailure {
            logger.log(Level.WARNING, "Exception in loading plugin $this", it)
        }
    }
}

@Suppress("LeakingThis")
abstract class MainPlugin : CoroutineScope, CoroutineContext.Element {
    companion object Key : CoroutineContext.Key<MainPlugin>

    final override val key: CoroutineContext.Key<*>
        get() = Key

    private val delegate: CoroutineScope = CoroutineScope(CoroutineThreadPool + this)
    val logger: Logger = Logger.getLogger(name)

    final override val coroutineContext: CoroutineContext
        get() = delegate.coroutineContext

    abstract val name: String
    abstract val author: String
    abstract val version: String

    abstract fun onEnable()
    abstract fun onDisable()
}

interface AutoInitializer {
    fun initialize()
}

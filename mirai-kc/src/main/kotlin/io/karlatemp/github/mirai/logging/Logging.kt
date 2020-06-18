/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/06/18 12:40:57
 *
 * Mirai-KC/Mirai-KC.mirai-kc.main/Logging.kt
 */

package io.karlatemp.github.mirai.logging

import cn.mcres.karlatemp.mxlib.MXBukkitLib
import cn.mcres.karlatemp.mxlib.logging.*
import cn.mcres.karlatemp.mxlib.tools.EmptyStream
import cn.mcres.karlatemp.mxlib.tools.InlinePrintStream
import io.karlatemp.github.mirai.levelAll
import io.karlatemp.github.mirai.logger
import net.mamoe.mirai.Bot
import net.mamoe.mirai.utils.DefaultLogger
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.MiraiLoggerPlatformBase
import org.fusesource.jansi.Ansi
import java.io.IOException
import java.io.RandomAccessFile
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.logging.Handler
import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.logging.Logger
import java.util.regex.Pattern

private val lv_ansi: Map<Level, String> = mapOf(
    Level.SEVERE to Ansi().fg(Ansi.Color.RED).a("SEVERE").reset().toString(),
    Level.INFO to Ansi().fg(Ansi.Color.CYAN).a("INFO").reset().toString(),
    Level.WARNING to Ansi().fg(Ansi.Color.YELLOW).a("WARNING").reset().toString(),
    Level.FINER to Ansi().fg(Ansi.Color.MAGENTA).a("FINER").reset().toString(),
    Level.FINE to Ansi().fg(Ansi.Color.MAGENTA).a("FINE").reset().toString(),
    Level.FINEST to Ansi().fg(Ansi.Color.MAGENTA).a("FINEST").reset().toString()
)

private fun filter(message: String): Boolean {
    val trim = message.trim()
    return trim == "Send done: Heartbeat.Alive"
            || trim.contains("Heartbeat.Alive")
}

class FLogging(private val raf: RandomAccessFile) : InlinePrintStream() {

    @Synchronized
    override fun print(s: String) {
        try {
            raf.seek(raf.length())
            raf.write(s.toByteArray(StandardCharsets.UTF_8))
        } catch (ignore: IOException) {
        }
    }

    @Synchronized
    override fun println() {
        try {
            raf.seek(raf.length())
            raf.write('\n'.toInt())
        } catch (ignore: IOException) {
        }
    }

    @Synchronized
    override fun println(x: String) {
        print(x)
        println()
    }

    override fun close() {
        raf.close()
    }
}

private val dropper = Pattern.compile("\\033\\[[0-9;]*?m")

val openFileLogging: AtomicBoolean = AtomicBoolean(true)

lateinit var logger: ILogger
    private set
lateinit var handler: MLoggerHandler
    private set

typealias LoggerCreator = (logger: Logger, bot: Bot?) -> Unit

var loggerCreator: LoggerCreator? = null

fun initializeLoggingSystem() {
    val yearFormat = SimpleDateFormat("yyyy/MM/dd")
    val hourFormat = SimpleDateFormat("HH:mm:ss")
    val raf = RandomAccessFile("logging.log", "rw")
    val loggingFile = FLogging(raf)
    val fileLogging = object : PrintStreamLogger(
        loggingFile,
        MessageFactoryImpl(),
        object :
            AlignmentPrefixSupplier(PrefixSupplier { error: Boolean, line: String?, level: Level?, record: LogRecord? ->
                if (record != null) {
                    record.loggerName
                } else "null"
            }) {
            override fun get(
                error: Boolean, line: String?, level: Level?, record: LogRecord?
            ): String {
                val date = Date()
                do {
                    val p = prln.get()
                    if (p < 20) break
                    if (prln.compareAndSet(p, 19)) break
                } while (true)
                return '[' + yearFormat.format(date) + ' ' + hourFormat.format(date) + "] " +
                        super.get(error, line, level, record)
            }
        }, loggingFile, loggingFile
    ) {
        override fun writeLine(pre: String?, message: String?, error: Boolean) {
            if (!openFileLogging.get()) return
            if (filter(message ?: return)) {
                return
            }
            super.writeLine(pre, message, error)
        }
    }

    val consoleLogger = object : PrintStreamLogger(
        ConsoleSystem,
        MessageFactoryAnsi(),
        object : AlignmentPrefixSupplier(
            PrefixSupplier { _: Boolean, _: String?, _: Level?, record: LogRecord? ->
                if (record != null) {
                    record.loggerName
                } else "null"
            }
        ) {
            override fun getCharsFontWidth(chars: String): Int {
                return super.getCharsFontWidth(dropper.matcher(chars).replaceAll(""))
            }

            override fun valueOf(lv: Level?): String {
                val s = lv_ansi[lv]
                return s ?: super.valueOf(lv)
            }

            override fun get(
                error: Boolean,
                line: String?,
                level: Level?,
                record: LogRecord?
            ): String {
                val date = Date()
                do {
                    val p = prln.get()
                    if (p < 20) break
                    if (prln.compareAndSet(p, 19)) break
                } while (true)
                return Ansi().reset()
                    .a('[').fgBrightYellow().a(yearFormat.format(date)).a(' ')
                    .fgBrightCyan().a(hourFormat.format(date)).reset().a("] ")
                    .a(super.get(error, line, level, record)).reset().toString()
            }
        },
        EmptyStream.stream.asPrintStream(),
        EmptyStream.stream.asPrintStream()
    ) {
        override fun writeLine(pre: String?, message: String?, error: Boolean) {
            if (filter(dropper.matcher(message ?: return).replaceAll(""))) {
                return
            }
            if (pre != null && pre.isNotEmpty()) {
                ConsoleSystem.lineWriter(pre + message)
            } else {
                ConsoleSystem.lineWriter(message)
            }
        }
    }
    MXBukkitLib.setLogger(AsyncLogger(
        MLogger(consoleLogger, fileLogging),
        Executors.newSingleThreadExecutor { task ->
            Thread(task, "Logger writer").also { it.isDaemon = true }
        }).also { logger = it })
    val rt = Logger.getGlobal().parent
    for (h in rt.handlers) {
        rt.removeHandler(h)
    }
    rt.addHandler(MLoggerHandler(object : AbstractBaseLogger(MessageFactoryImpl()) {
        override fun writeLine(pre: String, message: String, error: Boolean) {}
        override fun getPrefix(
            error: Boolean,
            line: String,
            level: Level,
            lr: LogRecord
        ): String {
            return ""
        }

        override fun publish(record: LogRecord, handler: Handler): ILogger {
            MXBukkitLib.getLogger().publish(record, handler)
            return this
        }
    }).also { handler = it })
    Thread.setDefaultUncaughtExceptionHandler { t: Thread, e: Throwable? ->
        "Thread#${t.name}".logger()
            .log(Level.SEVERE, "An unknown error caused the thread to stop", e)
    }
    handler.level = Level.ALL
    DefaultLogger = { name -> name.toString().logger().levelAll().toMirai(true) }
}


fun Logger.toMirai(initialize: Boolean = false): MiraiLogger {
    if (initialize) {
        loggerCreator?.invoke(this, null)
    }
    return object : MiraiLoggerPlatformBase() {
        override val identity: String?
            get() = this@toMirai.name

        override fun debug0(message: String?, e: Throwable?) {
            log(Level.FINER, message, e)
        }

        override fun error0(message: String?, e: Throwable?) {
            log(Level.SEVERE, message, e)
        }

        override fun info0(message: String?, e: Throwable?) {
            log(Level.INFO, message, e)
        }

        override fun verbose0(message: String?, e: Throwable?) {
            log(Level.FINE, message, e)
        }

        override fun warning0(message: String?, e: Throwable?) {
            log(Level.WARNING, message, e)
        }
    }
}

fun Bot.newLogger(): MiraiLogger {
    val logger = "bot.$id".logger()
    loggerCreator?.invoke(logger, this)
    return logger.toMirai(initialize = false)
}


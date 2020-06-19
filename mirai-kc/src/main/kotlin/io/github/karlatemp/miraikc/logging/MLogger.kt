/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/05/18 13:43:40
 *
 * MiraiPlugins/MiraiBootstrap/MLogger.kt
 */

package io.github.karlatemp.miraikc.logging

import cn.mcres.karlatemp.mxlib.logging.ILogger
import java.io.PrintStream
import java.lang.management.ThreadInfo
import java.util.*
import java.util.logging.Handler
import java.util.logging.Level
import java.util.logging.LogRecord

class MLogger(private vararg val loggers: ILogger) : ILogger {
    override fun printThreadInfo(thread: Thread, fullFrames: Boolean, emptyPrefix: Boolean): ILogger {
        loggers.forEach { it.printThreadInfo(thread, fullFrames, emptyPrefix) }
        return this
    }

    override fun printStackTrace(thr: Throwable): ILogger {
        loggers.forEach { it.printStackTrace(thr) }
        return this
    }

    override fun error(line: Any?): ILogger {
        loggers.forEach { it.error(line) }
        return this
    }

    override fun printf(data: Any?): ILogger {
        loggers.forEach { it.printf(data) }
        return this
    }

    override fun printf(err: Boolean, ln: String?): ILogger {
        loggers.forEach { it.printf(err, ln) }
        return this
    }

    override fun isError(level: Level?): Boolean {
        return loggers[0].isError(level)
    }

    override fun println(line: String?): ILogger {
        loggers.forEach { it.println(line) }
        return this
    }

    override fun format(format: String?, vararg args: Any?): ILogger {
        loggers.forEach { it.format(format, *args) }
        return this
    }

    override fun format(locale: Locale?, format: String?, vararg args: Any?): ILogger {
        loggers.forEach { it.format(locale, format, *args) }
        return this
    }

    override fun errformat(format: String?, vararg args: Any?): ILogger {
        loggers.forEach { it.errformat(format, *args) }
        return this
    }

    override fun errformat(locale: Locale?, format: String?, vararg args: Any?): ILogger {
        loggers.forEach { it.errformat(locale, format, args) }
        return this
    }

    override fun getStackTraceElementMessage(track: StackTraceElement?): String {
        return loggers[0].getStackTraceElementMessage(track)
    }

    override fun printThreadInfo(info: ThreadInfo, fullFrames: Boolean, emptyPrefix: Boolean): ILogger {
        loggers.forEach { it.printThreadInfo(info, fullFrames, emptyPrefix) }
        return this
    }

    override fun printStackTrace(thr: Throwable, printStacks: Boolean, isError: Boolean): ILogger {
        loggers.forEach { it.printStackTrace(thr, printStacks, isError) }
        return this
    }

    override fun publish(record: LogRecord?, handler: Handler?): ILogger {
        loggers.forEach { it.publish(record, handler) }
        return this
    }

    override fun error(line: String?): ILogger {
        loggers.forEach { it.error(line) }
        return this
    }

    override fun getPrintStream(): PrintStream {
        TODO("Not yet implemented")

    }

    override fun printf(line: String?): ILogger {
        loggers.forEach { it.printf(line) }
        return this
    }

    override fun getErrorStream(): PrintStream {
        TODO("Not yet implemented")
    }

}
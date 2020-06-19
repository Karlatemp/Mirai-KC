/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/06/19 24:04:42
 *
 * Mirai-KC/Mirai-KC.mirai-kc.main/ConsoleSystem.kt
 */

package io.github.karlatemp.miraikc.logging

import io.github.karlatemp.miraikc.Bootstrap
import org.jline.reader.LineReaderBuilder
import org.jline.reader.UserInterruptException
import org.jline.reader.impl.completer.NullCompleter
import org.jline.terminal.TerminalBuilder
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.util.*

object ConsoleSystem {
    val lineReader: (isPasswd: Boolean) -> String?
    val lineWriter: (String) -> Unit

    private fun findEncoding(): Charset {
        runCatching {
            return Charset.forName(System.getProperty("sun.stdout.encoding"))
        }
        runCatching {
            return Charset.forName(System.getProperty("file.encoding"))
        }
        return Charsets.UTF_8
    }

    fun init() {}

    init {
        if (System.getProperty("kc.no-jline") === null) {
            System.setProperty("kc.no-ansi", "")

            val dumb = System.getProperty("java.class.path")
                .contains("idea_rt.jar") || System.getProperty("kc.idea") !== null

            val terminal = TerminalBuilder.builder()
                .dumb(dumb)
                .build()
            val lineReader = LineReaderBuilder.builder()
                .terminal(terminal)
                .completer(NullCompleter())
                .build()
            this.lineReader = { isPasswd ->
                try {
                    lineReader.readLine("> ", if (isPasswd) '\u0000' else null)
                } catch (uie: UserInterruptException) {
                    Bootstrap.shutdown()
                    null
                }
            }
            this.lineWriter = {
                lineReader.printAbove(it)
            }
        } else {
            val console = System.console()
            if (console != null) {
                lineReader = {
                    if (it)
                        String(console.readPassword("> "))
                    else
                        console.readLine("> ")
                }
                lineWriter = { console.writer().also { p -> p.println(it) }.flush() }
            } else {
                val sout = System.out
                val scanner = Scanner(InputStreamReader(System.`in`, findEncoding()))
                lineReader = {
                    if (scanner.hasNextLine())
                        scanner.nextLine()
                    else null
                }
                lineWriter = { sout.also { p -> p.println(it) }.flush() }
            }
        }
    }
}

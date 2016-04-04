package net.nonylene.gradle.external

import com.google.common.escape.Escapers
import com.intellij.tools.Tool

/**
 * @see Tool
 */
class ExternalBeforeRunTaskTool(val tasks: List<String>, val commandLineArguments : List<String>) : Tool() {

    val SHELL_ESCAPE = Escapers.builder().addEscape('\'', "'\"'\"'").build()

    init {
        isEnabled = true
        setFilesSynchronizedAfterRun(true)
        program = "ls"
        parameters = "\$GRADLE_TASKS\$ \$GRADLE_ARGS\$"
        workingDirectory = ""

        val tasks = tasks.fold(StringBuilder()) { builder, text ->
            builder.append(shellEscapedString(text))
            builder.append(" ")
        }.toString()

        val args = commandLineArguments.fold(StringBuilder()) { builder, text ->
            builder.append(shellEscapedString(text))
            builder.append(" ")
        }.toString()

        parameters = parameters
                .replace("\$GRADLE_TASKS\$", shellEscapedString(tasks))
                .replace("\$GRADLE_ARGS\$", shellEscapedString(args))

        with(javaClass.superclass.getDeclaredMethod("setName", String::class.java)) {
            isAccessible = true
            invoke(this@ExternalBeforeRunTaskTool, "Gralde External Build Task")
        }
        with(javaClass.superclass.getDeclaredMethod("setUseConsole", Boolean::class.java)) {
            isAccessible = true
            invoke(this@ExternalBeforeRunTaskTool, true)
        }
    }

    private fun shellEscapedString(text: String): String {
        return "'" + SHELL_ESCAPE.escape(text) + "'"
    }
}

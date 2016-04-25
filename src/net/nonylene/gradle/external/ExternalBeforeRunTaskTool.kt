package net.nonylene.gradle.external

import com.intellij.openapi.components.ServiceManager
import com.intellij.tools.Tool

/**
 * @see Tool
 */
class ExternalBeforeRunTaskTool(val tasks: List<String>, val commandLineArguments : List<String>) : Tool() {

    private val provider = ServiceManager.getService(ExternalPreferenceProvider::class.java)

    init {
        isEnabled = true
        setFilesSynchronizedAfterRun(true)
        val state = provider.state!!
        program = state.program
        parameters = state.parameters
        workingDirectory = state.workingDirectory

        val tasks = tasks.fold(StringBuilder()) { builder, text ->
            builder.append(text)
                    .append(',')
        }.toString()

        val args = commandLineArguments.fold(StringBuilder()) { builder, text ->
            builder.append(text)
                    .append(',')
        }.toString()

        parameters = parameters
                .replace("\$GRADLE_TASKS\$", tasks)
                .replace("\$GRADLE_ARGS\$", args)

        with(javaClass.superclass.getDeclaredMethod("setName", String::class.java)) {
            isAccessible = true
            invoke(this@ExternalBeforeRunTaskTool, "Gralde External Build Task")
        }
        with(javaClass.superclass.getDeclaredMethod("setUseConsole", Boolean::class.java)) {
            isAccessible = true
            invoke(this@ExternalBeforeRunTaskTool, true)
        }
    }
}

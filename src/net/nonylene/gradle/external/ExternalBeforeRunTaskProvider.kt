package net.nonylene.gradle.external

import com.android.tools.idea.gradle.run.GradleInvokerOptions
import com.intellij.execution.BeforeRunTask
import com.intellij.execution.BeforeRunTaskProvider
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.Ref
import com.intellij.util.concurrency.Semaphore
import com.android.tools.idea.gradle.run.MakeBeforeRunTaskProvider

import javax.swing.*

/**
 * @see MakeBeforeRunTaskProvider
 */
class ExternalBeforeRunTaskProvider(private val myProject: Project) : BeforeRunTaskProvider<ExternalBeforeRunTask>() {

    override fun getId(): Key<ExternalBeforeRunTask> {
        return ID
    }

    override fun getIcon(): Icon? {
        return null
    }

    override fun getTaskIcon(task: ExternalBeforeRunTask?): Icon? {
        return null
    }

    override fun getName(): String {
        return TASK_NAME
    }

    override fun getDescription(task: ExternalBeforeRunTask): String {
        return TASK_NAME
    }

    override fun isConfigurable(): Boolean {
        return false
    }

    override fun createTask(runConfiguration: RunConfiguration): ExternalBeforeRunTask? {
        return ExternalBeforeRunTask()
    }

    override fun configureTask(runConfiguration: RunConfiguration, task: ExternalBeforeRunTask): Boolean {
        return false
    }

    override fun canExecuteTask(configuration: RunConfiguration, task: ExternalBeforeRunTask): Boolean {
        return true
    }

    override fun executeTask(context: DataContext, configuration: RunConfiguration,
                             env: ExecutionEnvironment, task: ExternalBeforeRunTask): Boolean {
        val options = GradleInvokerOptions.create(myProject, context, configuration, env, null)
        val tool = ExternalBeforeRunTaskTool(options.tasks, options.commandLineArguments);

        val targetDone = Semaphore()
        val result = Ref(false)

        try {
            ApplicationManager.getApplication().invokeAndWait({
                tool.execute(null, context, env.executionId, object : ProcessAdapter() {
                    override fun startNotified(event: ProcessEvent?) {
                        targetDone.down()
                    }

                    override fun processTerminated(event: ProcessEvent?) {
                        result.set(event!!.exitCode == 0)
                        targetDone.up()
                    }
                })
            }, ModalityState.NON_MODAL)
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

        targetDone.waitFor()
        return result.get()
    }

    companion object {
        val ID = Key.create<ExternalBeforeRunTask>("Nonylene.Gradle.ExternalBeforeRunTask")
        private val TASK_NAME = "Gradle Make on External Tool"
    }
}

class ExternalBeforeRunTask constructor() : BeforeRunTask<ExternalBeforeRunTask>(ExternalBeforeRunTaskProvider.ID)

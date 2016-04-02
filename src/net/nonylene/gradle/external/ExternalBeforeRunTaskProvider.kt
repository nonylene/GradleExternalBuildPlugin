package net.nonylene.gradle.external

import com.android.tools.idea.gradle.compiler.AndroidGradleBuildConfiguration
import com.android.tools.idea.gradle.run.GradleInvokerOptions
import com.android.tools.idea.gradle.util.GradleBuilds
import com.intellij.compiler.CompilerWorkspaceConfiguration
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

import javax.swing.*

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

        val commandLineArgs = options.commandLineArguments

        val buildConfiguration = AndroidGradleBuildConfiguration.getInstance(myProject)

        if (buildConfiguration.USE_CONFIGURATION_ON_DEMAND && !commandLineArgs.contains(GradleBuilds.CONFIGURE_ON_DEMAND_OPTION)) {
            commandLineArgs.add(GradleBuilds.CONFIGURE_ON_DEMAND_OPTION);
        }

        if (!commandLineArgs.contains(GradleBuilds.PARALLEL_BUILD_OPTION) &&
                CompilerWorkspaceConfiguration.getInstance(myProject).PARALLEL_COMPILATION) {
            commandLineArgs.add(GradleBuilds.PARALLEL_BUILD_OPTION);
        }


        val application = ApplicationManager.getApplication()
        if (application != null && application.isUnitTestMode) {
            commandLineArgs.add("--info")
            commandLineArgs.add("--recompile-scripts")
        }

        val tool = ExternalBeforeRunTaskTool(options.tasks, commandLineArgs);

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

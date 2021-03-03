package org.kubicz.mavenexecutor.runconfiguration

import com.intellij.compiler.options.CompileStepBeforeRun
import com.intellij.compiler.options.CompileStepBeforeRunNoErrorCheck
import com.intellij.execution.BeforeRunTask
import com.intellij.execution.ExecutionException
import com.intellij.execution.RunManager
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.ConfigurationTypeUtil
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.impl.DefaultJavaProgramRunner
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ProgramRunner
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.LocalFileSystem
import org.jetbrains.idea.maven.execution.MavenRunnerParameters
import org.jetbrains.idea.maven.execution.MavenRunnerSettings
import org.jetbrains.idea.maven.execution.RunnerBundle
import org.jetbrains.idea.maven.project.MavenGeneralSettings
import org.jetbrains.idea.maven.project.MavenProject
import org.jetbrains.idea.maven.project.MavenProjectsManager
import org.jetbrains.idea.maven.utils.MavenUtil
import javax.swing.Icon

class MavenExecutorRunConfigurationType internal constructor() : ConfigurationType {

    private val myFactory = object : ConfigurationFactory(this) {
        override fun createTemplateConfiguration(project: Project): RunConfiguration {
            return MavenExecutorRunConfiguration(project, this, "")
        }

        override fun createTemplateConfiguration(project: Project, runManager: RunManager): RunConfiguration {
            return MavenExecutorRunConfiguration(project, this, "")
        }

        override fun createConfiguration(name: String?, template: RunConfiguration): RunConfiguration {
            val cfg = super.createConfiguration(name, template) as MavenExecutorRunConfiguration
            if (!StringUtil.isEmptyOrSpaces(cfg.runnerParameters.workingDirPath)) {
                return cfg
            } else {
                val project = cfg.project

                val projectsManager = MavenProjectsManager.getInstance(project)
                val projects = projectsManager.projects
                if (projects.size != 1) {
                    return cfg
                } else {
                    val directory = (projects[0] as MavenProject).directoryFile
                    cfg.runnerParameters.workingDirPath = directory.path
                    return cfg
                }

            }
        }

        override fun configureBeforeRunTaskDefaults(providerID: Key<out BeforeRunTask<*>>?, task: BeforeRunTask<*>?) {
            if (providerID === CompileStepBeforeRun.ID || providerID === CompileStepBeforeRunNoErrorCheck.ID) {
                task!!.isEnabled = false
            }

        }
    }

    override fun getDisplayName(): String {
        return RunnerBundle.message("maven.run.configuration.name", *arrayOfNulls(0))
    }

    override fun getConfigurationTypeDescription(): String {
        return RunnerBundle.message("maven.run.configuration.description", *arrayOfNulls(0))
    }

    override fun getIcon(): Icon {
        return IconLoader.getIcon("/icons/task.svg")
    }

    override fun getConfigurationFactories(): Array<ConfigurationFactory> {
        return arrayOf(this.myFactory)
    }

    override fun getId(): String {
        return this.javaClass.name
    }

    companion object {
        private val MAX_NAME_LENGTH = 40

        val instance: MavenExecutorRunConfigurationType
            get() = ConfigurationTypeUtil.findConfigurationType(MavenExecutorRunConfigurationType::class.java)

        fun generateName(project: Project, runnerParameters: MavenRunnerParameters): String {
            val stringBuilder = StringBuilder()
            val name = getMavenProjectName(project, runnerParameters)
            if (!StringUtil.isEmptyOrSpaces(name)) {
                stringBuilder.append(name)
                stringBuilder.append(" ")
            }

            stringBuilder.append("[")
            listGoals(stringBuilder, runnerParameters.goals)
            stringBuilder.append("]")
            return stringBuilder.toString()
        }

        private fun listGoals(stringBuilder: StringBuilder, goals: List<String>) {
            var index = 0

            val var3 = goals.iterator()
            while (var3.hasNext()) {
                val goal = var3.next()
                if (index != 0) {
                    if (stringBuilder.length + goal.length >= 40) {
                        stringBuilder.append("...")
                        break
                    }

                    stringBuilder.append(",")
                }

                stringBuilder.append(goal)
                ++index
            }

        }

        private fun getMavenProjectName(project: Project, runnerParameters: MavenRunnerParameters): String? {
            val virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(runnerParameters.workingDirPath + "/pom.xml")
            if (virtualFile != null) {
                val mavenProject = MavenProjectsManager.getInstance(project).findProject(virtualFile)
                if (mavenProject != null && !StringUtil.isEmptyOrSpaces(mavenProject.mavenId.artifactId)) {
                    return mavenProject.mavenId.artifactId
                }
            }

            return null
        }

        fun runConfiguration(project: Project, params: MavenRunnerParameters, callback: ProgramRunner.Callback?) {
            runConfiguration(project, params, null as MavenGeneralSettings?, null as MavenRunnerSettings?, callback)
        }

        fun runConfiguration(project: Project, params: MavenRunnerParameters, settings: MavenGeneralSettings?,
                             runnerSettings: MavenRunnerSettings?, callback: ProgramRunner.Callback?) {
            val configSettings = createRunnerAndConfigurationSettings(settings, runnerSettings, params, project)
            val runner = DefaultJavaProgramRunner.getInstance()
            val executor = DefaultRunExecutor.getRunExecutorInstance()

            try {
                runner.execute(ExecutionEnvironment(executor, runner, configSettings, project), callback)
            } catch (var9: ExecutionException) {
                MavenUtil.showError(project, "Failed to execute Maven goal", var9)
            }

        }

        fun createRunnerAndConfigurationSettings(generalSettings: MavenGeneralSettings?,
                                                 runnerSettings: MavenRunnerSettings?, params: MavenRunnerParameters, project: Project): RunnerAndConfigurationSettings {
            val settings = RunManager.getInstance(project)
                    .createConfiguration(generateName(project, params), ConfigurationTypeUtil
                            .findConfigurationType(MavenExecutorRunConfigurationType::class.java).myFactory)
            val runConfiguration = settings.configuration as MavenExecutorRunConfiguration
            runConfiguration.runnerParameters = params
            runConfiguration.generalSettings = generalSettings
            runConfiguration.runnerSettings = runnerSettings

            return settings
        }
    }
}


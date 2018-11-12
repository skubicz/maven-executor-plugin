package org.kubicz.mavenexecutor.view.window.panels

import com.intellij.execution.ExecutionException
import com.intellij.execution.RunManagerEx
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.configurations.ConfigurationTypeUtil
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.impl.DefaultJavaProgramRunner
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.project.Project
import org.kubicz.mavenexecutor.model.settings.ExecutionSettings
import org.kubicz.mavenexecutor.model.settings.ProjectToBuild
import org.kubicz.mavenexecutor.runconfiguration.MavenExecutorRunConfiguration
import org.kubicz.mavenexecutor.runconfiguration.MavenExecutorRunConfigurationType
import org.jetbrains.idea.maven.execution.MavenRunnerParameters
import org.jetbrains.idea.maven.execution.MavenRunnerSettings
import org.jetbrains.idea.maven.project.MavenGeneralSettings
import org.jetbrains.idea.maven.project.MavenProjectsManager
import org.jetbrains.idea.maven.utils.MavenUtil
import org.kubicz.mavenexecutor.view.window.MavenAdditionalParameters
import org.kubicz.mavenexecutor.view.window.ExecutionSettingsService
import java.awt.event.ActionEvent
import java.awt.event.ActionListener

class RunMavenActionListener(private val project: Project) : ActionListener {

    override fun actionPerformed(event: ActionEvent) {
        val settings = ExecutionSettingsService.getInstance(project).currentSettings

        val runConfigurationType = ConfigurationTypeUtil.findConfigurationType(MavenExecutorRunConfigurationType::class.java)

        settings.projectsToBuild.forEach { projectToBuild ->
            val runnerAndConfigurationSettings = RunManagerEx.getInstanceEx(project)
                    .createRunConfiguration(projectToBuild.displayName, runConfigurationType.configurationFactories[0])

            runnerAndConfigurationSettings.isActivateToolWindowBeforeRun = true

            val runConfiguration = runnerAndConfigurationSettings.configuration as MavenExecutorRunConfiguration

            runConfiguration.runnerSettings = mavenRunnerSettings(settings)
            runConfiguration.generalSettings = mavenGeneralSettings(settings)
            runConfiguration.runnerParameters = mavenRunnerParameters(settings, projectToBuild)
            runConfiguration.additionalParameters = mavenAdditionalParameters(projectToBuild)

            run(runnerAndConfigurationSettings)
        }
    }

    private fun run(settings: RunnerAndConfigurationSettings) {
        val runner = DefaultJavaProgramRunner.getInstance()

        val executor = DefaultRunExecutor.getRunExecutorInstance()

        val env = ExecutionEnvironment(executor, runner, settings, project)
        try {
            runner.execute(env)
        } catch (e: ExecutionException) {
            e.printStackTrace()
        }
    }

    private fun mavenAdditionalParameters(projectToBuild: ProjectToBuild): MavenAdditionalParameters {
        val mavenAdditionalParameters = MavenAdditionalParameters()

        mavenAdditionalParameters.projects = projectToBuild.selectedModulesAsText()

        return mavenAdditionalParameters
    }

    private fun mavenRunnerParameters(setting: ExecutionSettings, projectToBuild: ProjectToBuild): MavenRunnerParameters {
        val mavenRunnerParameters = MavenRunnerParameters()

        mavenRunnerParameters.workingDirPath = projectToBuild.projectDictionary
        mavenRunnerParameters.goals = setting.goals
        mavenRunnerParameters.profilesMap = setting.profiles.map { it to true }.toMap()

        return mavenRunnerParameters
    }

    private fun mavenGeneralSettings(setting: ExecutionSettings): MavenGeneralSettings {
        val mavenGeneralSettings = MavenGeneralSettings()

        mavenGeneralSettings.isWorkOffline = setting.isOfflineMode
        mavenGeneralSettings.isAlwaysUpdateSnapshots = setting.isAlwaysUpdateSnapshot
        setting.threadCount?.let {
            if (it > 0) {
                mavenGeneralSettings.threads = it.toString()
            }
        }

        val projectsManager = MavenProjectsManager.getInstance(project)
        val mavenHome = MavenUtil.resolveMavenHomeDirectory(projectsManager.generalSettings.mavenHome)
        mavenGeneralSettings.mavenHome = mavenHome!!.path // TODO co z !!

        return mavenGeneralSettings

    }

    private fun mavenRunnerSettings(setting: ExecutionSettings): MavenRunnerSettings {
        val mavenRunnerSettings = MavenRunnerSettings()

        var jvmOptions = setting.jvmOptionsAsText()
        if (setting.isUseOptionalJvmOptions && !setting.optionalJvmOptions.isEmpty()) {
            jvmOptions = jvmOptions + " " + setting.optionalJvmOptionsAsText()
        }
        mavenRunnerSettings.setVmOptions(jvmOptions)

        mavenRunnerSettings.environmentProperties = setting.environmentProperties
        mavenRunnerSettings.isSkipTests = setting.isSkipTests

        return mavenRunnerSettings
    }
}
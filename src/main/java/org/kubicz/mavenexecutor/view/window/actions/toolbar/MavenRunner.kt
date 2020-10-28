package org.kubicz.mavenexecutor.view.window.actions.toolbar

import com.intellij.execution.ExecutionException
import com.intellij.execution.RunManagerEx
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.configurations.ConfigurationTypeUtil
import com.intellij.execution.configurations.ParametersList
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.impl.DefaultJavaProgramRunner
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.project.Project
import org.jetbrains.idea.maven.execution.MavenRunnerParameters
import org.jetbrains.idea.maven.execution.MavenRunnerSettings
import org.jetbrains.idea.maven.project.MavenGeneralSettings
import org.jetbrains.idea.maven.project.MavenProjectsManager
import org.jetbrains.idea.maven.utils.MavenUtil
import org.kubicz.mavenexecutor.model.MavenArtifact
import org.kubicz.mavenexecutor.model.MavenGroupAndArtifactKey
import org.kubicz.mavenexecutor.model.settings.ExecutionSettings
import org.kubicz.mavenexecutor.model.settings.ProjectToBuild
import org.kubicz.mavenexecutor.runconfiguration.MavenExecutorRunConfiguration
import org.kubicz.mavenexecutor.runconfiguration.MavenExecutorRunConfigurationType
import org.kubicz.mavenexecutor.view.window.MavenAdditionalParameters

class MavenRunner(private val settings: ExecutionSettings, private val project: Project) {

    val runnerAndConfigurationSettingsList = createRunConfigurations(settings.projectsToBuild)

    init {
    }

    fun createRunConfigurations(projectToBuildList: List<ProjectToBuild>): Map<MavenGroupAndArtifactKey, RunnerAndConfigurationSettings> {
        val result = mutableMapOf<MavenGroupAndArtifactKey, RunnerAndConfigurationSettings>()

        val runConfigurationType = ConfigurationTypeUtil.findConfigurationType(MavenExecutorRunConfigurationType::class.java)

        projectToBuildList.forEach { projectToBuild ->
            val runnerAndConfigurationSettings = RunManagerEx.getInstanceEx(project)
                    .createConfiguration(projectToBuild.displayName, runConfigurationType.configurationFactories[0])

            runnerAndConfigurationSettings.isActivateToolWindowBeforeRun = true

            val runConfiguration = runnerAndConfigurationSettings.configuration as MavenExecutorRunConfiguration

            runConfiguration.runnerSettings = mavenRunnerSettings(settings)
            runConfiguration.generalSettings = mavenGeneralSettings(settings, project)
            runConfiguration.runnerParameters = mavenRunnerParameters(settings, projectToBuild, project)
            runConfiguration.additionalParameters = mavenAdditionalParameters(settings, projectToBuild)

            result.put(projectToBuild.mavenArtifact.getGroupAndArtifactKey(), runnerAndConfigurationSettings)
        }

        return result
    }

    fun runConfigurationList(): List<MavenExecutorRunConfiguration> {
        return runnerAndConfigurationSettingsList.values.map { it.configuration as MavenExecutorRunConfiguration }
    }

    fun commandLine(mavenArtifact: MavenArtifact): String {
        return commandLine(runnerAndConfigurationSettingsList.get(mavenArtifact.getGroupAndArtifactKey())!!)
    }

    fun commandLine(runnerSettings: RunnerAndConfigurationSettings): String {
        return (runnerSettings.configuration as MavenExecutorRunConfiguration).createJavaParameters(project).toCommandLine().preparedCommandLine
    }

    fun simplifiedCommandLine(mavenArtifact: MavenArtifact): String {
        return simplifiedCommandLine(runnerAndConfigurationSettingsList.get(mavenArtifact.getGroupAndArtifactKey())!!)
    }

    fun simplifiedCommandLine(runnerSettings: RunnerAndConfigurationSettings): String {
        val programParametersList = (runnerSettings.configuration as MavenExecutorRunConfiguration).createJavaParameters(project).programParametersList
        programParametersList.addParametersString(settings.allJvmOptionsAsText())

        var parameters = programParametersList.parameters.filterNot { it.startsWith("-Didea.version")}.toMutableList()

        return "mvn " + ParametersList.join(parameters)
    }

    fun runConfiguration(projectToBuild: ProjectToBuild): MavenExecutorRunConfiguration {
        return runConfiguration(projectToBuild)
    }

    fun runConfiguration(mavenArtifact: MavenArtifact): MavenExecutorRunConfiguration {
        return runnerAndConfigurationSettingsList.getValue(mavenArtifact.getGroupAndArtifactKey()).configuration as MavenExecutorRunConfiguration
    }

    fun run() {
        run(runnerAndConfigurationSettingsList.values)
    }

    fun run(runnerAndConfigurationSettings: Collection<RunnerAndConfigurationSettings>) {
        runnerAndConfigurationSettings.forEach { it ->
            run(it, project)
        }
    }

    private fun run(settings: RunnerAndConfigurationSettings, project: Project) {
        val runner = DefaultJavaProgramRunner.getInstance()

        val executor = DefaultRunExecutor.getRunExecutorInstance()

        val env = ExecutionEnvironment(executor, runner, settings, project)
        try {
            runner.execute(env)
        } catch (e: ExecutionException) {
            e.printStackTrace()
        }
    }

    private fun mavenAdditionalParameters(setting: ExecutionSettings, projectToBuild: ProjectToBuild): MavenAdditionalParameters {
        val mavenAdditionalParameters = MavenAdditionalParameters()

        mavenAdditionalParameters.projects = projectToBuild.selectedModulesAsText()
        mavenAdditionalParameters.additionalParameters = setting.additionalParameters

        return mavenAdditionalParameters
    }

    private fun mavenRunnerParameters(setting: ExecutionSettings, projectToBuild: ProjectToBuild, project: Project): MavenRunnerParameters {
        val mavenRunnerParameters = MavenRunnerParameters()

        mavenRunnerParameters.workingDirPath = projectToBuild.projectDictionary
        mavenRunnerParameters.goals = setting.goals
        mavenRunnerParameters.profilesMap = setting.profiles.map { it to true }.toMap()

        return mavenRunnerParameters
    }

    private fun mavenGeneralSettings(setting: ExecutionSettings, project: Project): MavenGeneralSettings {
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

        mavenRunnerSettings.setVmOptions(setting.allJvmOptionsAsText())

        mavenRunnerSettings.environmentProperties = setting.environmentProperties
        mavenRunnerSettings.isSkipTests = setting.isSkipTests

        return mavenRunnerSettings
    }

}
package org.kubicz.mavenexecutor.view.window.actions.toolbar

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import org.jetbrains.idea.maven.project.MavenProjectsManager
import org.kubicz.mavenexecutor.model.settings.ProjectToBuild
import org.kubicz.mavenexecutor.view.window.ExecutionSettingsService
import org.kubicz.mavenexecutor.view.window.LastModifiedFilesService


class RunAllMavenAction : AnAction("") {

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project!!
        val settingsService = ExecutionSettingsService.getInstance(project)
        val settings = settingsService.currentSettings

        LastModifiedFilesService.getInstance(project).clearFiles()

        val mavenRunner = MavenRunner(settings, event.project!!)

        val projectToBuild = settingsService.currentRootProjectsAsMavenize(MavenProjectsManager.getInstance(event.project!!))
                .map { ProjectToBuild(it.displayName, it.mavenArtifact, it.projectDirectory.path) }

        mavenRunner.run(mavenRunner.createRunConfigurations(projectToBuild).values)
    }


    override fun update(event: AnActionEvent) {
        val project = event.project!!
        val presentation = event.presentation
        val settings = ExecutionSettingsService.getInstance(project)

        val canExecute = settings.currentSettings.goals.isNotEmpty()

        presentation.isEnabled = canExecute
    }

}
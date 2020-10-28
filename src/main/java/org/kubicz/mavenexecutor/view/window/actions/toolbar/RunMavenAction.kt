package org.kubicz.mavenexecutor.view.window.actions.toolbar

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import org.kubicz.mavenexecutor.view.window.ExecutionSettingsService
import org.kubicz.mavenexecutor.view.window.LastModifiedFilesService


class RunMavenAction : AnAction("") {

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project!!
        val settings = ExecutionSettingsService.getInstance(project).currentSettings

        LastModifiedFilesService.getInstance(project).clearFiles()

        val mavenRunner = MavenRunner(settings, event.project!!)
        mavenRunner.run()
    }


    override fun update(event: AnActionEvent) {
        val project = event.project!!
        val presentation = event.presentation
        val settings = ExecutionSettingsService.getInstance(project)

        val canExecute = settings.currentSettings.goals.isNotEmpty() && settings.currentSettings.projectsToBuild.isNotEmpty()

        presentation.isEnabled = canExecute
    }

}
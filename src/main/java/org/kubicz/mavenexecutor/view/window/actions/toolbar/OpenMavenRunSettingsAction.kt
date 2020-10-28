package org.kubicz.mavenexecutor.view.window.actions.toolbar

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.DialogBuilder
import org.jetbrains.idea.maven.project.MavenProjectsManager
import org.kubicz.mavenexecutor.view.MavenExecutorBundle
import org.kubicz.mavenexecutor.view.MavenExecutorBundle.Companion.message
import org.kubicz.mavenexecutor.view.window.MavenExecutorToolWindow
import org.kubicz.mavenexecutor.view.window.panels.OpenMavenRunSettingsPanel


class OpenMavenRunSettingsAction : AnAction("") {

    override fun actionPerformed(event: AnActionEvent) {
             val project = event.project!!

        if (MavenProjectsManager.getInstance(project).rootProjects.isEmpty()){
            Notifications.Bus.notify(Notification("Maven Executor","", message("mavenExecutor.notFoundProjects.warning"), NotificationType.WARNING))
            return
        }

        val builder = DialogBuilder()
        builder.setTitle(message("mavenExecutor.mavenRunSettings.title"))
        builder.addOkAction()
        builder.resizable(false)

        builder.centerPanel(OpenMavenRunSettingsPanel(project).create())

        if (builder.showAndGet()) {
            MavenExecutorToolWindow.getInstance(project).updateAll()
        }

    }

}
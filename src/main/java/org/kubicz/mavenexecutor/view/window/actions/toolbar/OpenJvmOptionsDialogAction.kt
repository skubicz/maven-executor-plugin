package org.kubicz.mavenexecutor.view.window.actions.toolbar

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import org.kubicz.mavenexecutor.view.window.ExecutionSettingsService
import myToolWindow.JvmOptionsDialog


class OpenJvmOptionsDialogAction : AnAction("") {

    override fun actionPerformed(event: AnActionEvent) {
        val setting = ExecutionSettingsService.getInstance(event.project!!).currentSettings

        val jvmOptionsDialog = JvmOptionsDialog(event.project)
        jvmOptionsDialog.jvmOptions = setting.jvmOptions

        if (jvmOptionsDialog.showAndGet()) {
            setting.jvmOptions = jvmOptionsDialog.jvmOptions
        }
    }

}
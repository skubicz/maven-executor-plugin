package org.kubicz.mavenexecutor.view.window.actions.toolbar

import com.google.gson.Gson
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import myToolWindow.SaveConfirmationDialog
import org.kubicz.mavenexecutor.model.settings.ExecutionSettings
import org.kubicz.mavenexecutor.model.settings.VisibleSetting
import org.kubicz.mavenexecutor.view.window.ExecutionSettingsService
import org.kubicz.mavenexecutor.view.window.MavenExecutorToolWindow


class SaveSettingsAction : AnAction("") {

    override fun actionPerformed(event: AnActionEvent) {
        val settingsService = ExecutionSettingsService.getInstance(event.project!!)

        val saveConfirmationDialog = SaveConfirmationDialog(event.project)
        saveConfirmationDialog.setSettingsName(settingsService.currentSettingsLabel)

        if (saveConfirmationDialog.showAndGet()) {
            settingsService.addSettings(saveConfirmationDialog.getSettingsName(), copySetting(settingsService.currentSettings))
            settingsService.loadSettings(saveConfirmationDialog.getSettingsName())

            MavenExecutorToolWindow.getInstance(event.project!!).updateAll()
        }
    }

    private fun copySetting(setting: ExecutionSettings): ExecutionSettings {
        val gson = Gson()
        return gson.fromJson(gson.toJson(setting), setting.javaClass)
    }


    override fun update(event: AnActionEvent) {
        val project = event.project ?: return
        val presentation = event.presentation
        val settings = ExecutionSettingsService.getInstance(project)

        presentation.isEnabled = settings.getVisibleSettings().contains(VisibleSetting.FAVORITE)
    }

}
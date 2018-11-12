package org.kubicz.mavenexecutor.view.window.actions.toolbar

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.util.xmlb.XmlSerializerUtil
import org.kubicz.mavenexecutor.view.window.ExecutionSettingsService
import org.kubicz.mavenexecutor.model.settings.ExecutionSettings
import org.kubicz.mavenexecutor.view.window.MavenExecutorToolWindow
import myToolWindow.SaveConfirmationDialog

class SaveSettingsAction : AnAction("") {

    override fun actionPerformed(event: AnActionEvent) {
        val settingsService = ExecutionSettingsService.getInstance(event.project!!)

        val saveConfirmationDialog = SaveConfirmationDialog(event.project)
        saveConfirmationDialog.setSettingsName(settingsService.currentSettingsLabel)

        if (saveConfirmationDialog.showAndGet()) {
            settingsService.addSettings(saveConfirmationDialog.getSettingsName(), copySetting(settingsService.currentSettings))
            settingsService.loadSettings(saveConfirmationDialog.getSettingsName())

            MavenExecutorToolWindow.getInstance(event.project!!).updateFavorite()
        }
    }

    private fun copySetting(setting: ExecutionSettings): ExecutionSettings {
        return XmlSerializerUtil.createCopy(setting)
    }

}
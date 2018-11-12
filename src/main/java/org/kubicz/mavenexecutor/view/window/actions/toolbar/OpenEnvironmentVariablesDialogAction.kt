package org.kubicz.mavenexecutor.view.window.actions.toolbar

import com.intellij.execution.util.EnvVariablesTable
import com.intellij.execution.util.EnvironmentVariable
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.DialogBuilder
import org.kubicz.mavenexecutor.view.window.ExecutionSettingsService


class OpenEnvironmentVariablesDialogAction : AnAction("") {

    override fun actionPerformed(event: AnActionEvent) {
        val setting = ExecutionSettingsService.getInstance(event.project!!).currentSettings

        val builder = DialogBuilder()
        builder.setTitle("Configure Environment Variables")
        builder.addOkAction()
        builder.addCancelAction()

        val table = EnvVariablesTable()
        table.setValues(toEnvVariables(setting.environmentProperties))
        table.actionsPanel.isVisible = true
        builder.centerPanel(table.component)

        if (builder.showAndGet()) {
            setting.environmentProperties = toEnvironmentPropertiesMap(table.environmentVariables)
        }

    }

    private fun toEnvVariables(environmentProperties: MutableMap<String, String>): List<EnvironmentVariable> {
        return environmentProperties.entries.map { EnvironmentVariable(it.key, it.value, false) }.toList()
    }


    private fun toEnvironmentPropertiesMap(environmentProperties: List<EnvironmentVariable>): MutableMap<String, String> {
        return environmentProperties.map { it.name to it.value }.toMap().toMutableMap()
    }

}
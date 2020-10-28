package org.kubicz.mavenexecutor.view.window.actions.toolbar

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.options.ShowSettingsUtil
import org.jetbrains.idea.maven.utils.actions.MavenAction

class OpenMavenGeneralSettingsAction: MavenAction() {

    override fun actionPerformed(event: AnActionEvent) {
        ShowSettingsUtil.getInstance().showSettingsDialog(event.project, "Maven")
    }

}
package org.kubicz.mavenexecutor.view.window.actions.toolbar

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.FileDocumentManager
import org.jetbrains.idea.maven.utils.actions.MavenAction
import org.jetbrains.idea.maven.utils.actions.MavenActionUtil


class MavenReimportAction: MavenAction() {

    override fun actionPerformed(event: AnActionEvent) {
        val projectsManager = MavenActionUtil.getProjectsManager(event.dataContext)

        FileDocumentManager.getInstance().saveAllDocuments()
        projectsManager!!.forceUpdateAllProjectsOrFindAllAvailablePomFiles()
    }

}
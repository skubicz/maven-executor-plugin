package org.kubicz.mavenexecutor.view.window.actions.toolbar

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import org.jetbrains.idea.maven.utils.actions.MavenActionUtil


class MavenReimportAction : AnAction("") {

    override fun actionPerformed(event: AnActionEvent) {
        val projectsManager = MavenActionUtil.getProjectsManager(event.dataContext)

        projectsManager!!.forceUpdateAllProjectsOrFindAllAvailablePomFiles()
    }

}
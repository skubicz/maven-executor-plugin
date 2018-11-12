package org.kubicz.mavenexecutor.view.window

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory


class MavenExecutorToolWindowFactory : ToolWindowFactory, DumbAware {

    private fun createUIComponents() {

    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        MavenExecutorToolWindow.getInstance(project).createToolWindowContent(toolWindow)
    }

}

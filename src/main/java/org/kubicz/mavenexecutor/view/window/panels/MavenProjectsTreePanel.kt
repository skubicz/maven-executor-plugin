package org.kubicz.mavenexecutor.view.window.panels

import com.intellij.ui.CheckboxTreeAdapter
import com.intellij.ui.CheckedTreeNode
import com.intellij.ui.ScrollPaneFactory
import org.jetbrains.idea.maven.project.MavenProjectsManager
import org.kubicz.mavenexecutor.model.settings.ProjectToBuild
import org.kubicz.mavenexecutor.model.tree.Mavenize
import org.kubicz.mavenexecutor.model.tree.ProjectRootNode
import org.kubicz.mavenexecutor.view.window.ExecutionSettingsService
import javax.swing.JComponent

class MavenProjectsTreePanel(projectsManager: MavenProjectsManager, settingsService: ExecutionSettingsService, nodeStateChangedListener: () -> Unit) {

    private val projectsTree = MavenProjectsTree(projectsManager, settingsService.currentSettings.projectsToBuild)

    private val scrollPane = ScrollPaneFactory.createScrollPane(projectsTree.treeComponent)

    private val settingsService = settingsService

    val component
        get() : JComponent = scrollPane

    init {
        projectsTree.addCheckboxTreeListener(object : CheckboxTreeAdapter() {
            override fun nodeStateChanged(node: CheckedTreeNode) {
                val selectedProjects = projectsTree.findSelectedProjects()

                val projectsToBuild = selectedProjects.entries.map { toProjectToBuild(it) }.toMutableList()

                settingsService.currentSettings.projectsToBuild = projectsToBuild

                nodeStateChangedListener()
            }
        })

    }

    fun update() {
        projectsTree.update(settingsService.currentSettings.projectsToBuild)
    }

    fun updateTreeSelection() {
        projectsTree.updateTreeSelection(settingsService.currentSettings.projectsToBuild)
    }

    private fun toProjectToBuild(selectedProjectEntry: Map.Entry<ProjectRootNode, List<Mavenize>>): ProjectToBuild {
        val projectRootNode = selectedProjectEntry.key
        val selectedModule = selectedProjectEntry.value

        val modules = selectedModule.map { it.mavenArtifact }.toMutableList()

        return ProjectToBuild(projectRootNode.displayName, projectRootNode.mavenArtifact, projectRootNode.projectDirectory.path, modules)
    }
}
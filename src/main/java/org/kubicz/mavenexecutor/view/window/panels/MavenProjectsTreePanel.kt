package org.kubicz.mavenexecutor.view.window.panels

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.CheckboxTreeListener
import com.intellij.ui.CheckedTreeNode
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.awt.RelativePoint
import com.intellij.util.xmlb.XmlSerializerUtil
import org.kubicz.mavenexecutor.model.Mavenize
import org.kubicz.mavenexecutor.model.settings.ExecutionSettings
import org.kubicz.mavenexecutor.model.settings.ProjectToBuild
import org.kubicz.mavenexecutor.model.tree.ProjectRootNode
import org.kubicz.mavenexecutor.view.MavenExecutorBundle.Companion.message
import org.kubicz.mavenexecutor.view.MavenProjectsHelper
import org.kubicz.mavenexecutor.view.components.CheckboxTreeExpandListener
import org.kubicz.mavenexecutor.view.window.ExecutionSettingsService
import org.kubicz.mavenexecutor.view.window.actions.toolbar.MavenRunner
import java.awt.Dimension
import java.awt.GridLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.SwingUtilities

class MavenProjectsTreePanel(private val project: Project, projectsHelper: MavenProjectsHelper, private val settingsService: ExecutionSettingsService) {

    private val projectsTree = MavenProjectsTree(projectsHelper, settingsService.currentSettings.projectsToBuild, settingsService.currentSettings.collapseModules)

    private val scrollPane = ScrollPaneFactory.createScrollPane(projectsTree.treeComponent)

    val component
        get() : JComponent = scrollPane

    init {
        scrollPane.preferredSize = Dimension(-1, -1)

        projectsTree.addCheckboxTreeListener(object : CheckboxTreeListener {
            override fun nodeStateChanged(node: CheckedTreeNode) {
                updateProjectsToBuild()
            }
        })

        projectsTree.addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent?) {
                val row = projectsTree.treeComponent.getRowForLocation(e?.point!!.x, e.point.y)

                if (row >= 0 && SwingUtilities.isRightMouseButton(e)) {
                    showMenu(row, RelativePoint(e))
                }
            }
        })

        projectsTree.setCheckboxTreeExpandListener(object : CheckboxTreeExpandListener {
            override fun possibleStageChange(node: CheckedTreeNode, isExpand: Boolean) {
                updateCollapseModules()
            }
        })

    }

    private fun showMenu(row: Int, point: RelativePoint) {
        val panel = JPanel(GridLayout(3,1))
        panel.background = null
        panel.border = null


        val popup = JBPopupFactory.getInstance()
                .createComponentPopupBuilder(panel, null)
                .setMayBeParent(true)
                .setFocusable(true)
                .setResizable(false)
                .setRequestFocus(true)
                .setLocateByContent(true)
                .setShowBorder(false)
                .setShowShadow(false)
                .setCancelOnWindowDeactivation(false)
                .setCancelCallback {
                    true
                }.createPopup()
        
        val buildThisButton = JButton(message("mavenExecutor.projectsTree.buildThis.button"))
        buildThisButton.toolTipText =message("mavenExecutor.projectsTree.buildThis.toolTip")
        buildThisButton.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent?) {
                val checkedTreeNode = (projectsTree.treeComponent.getPathForRow(row).lastPathComponent as? CheckedTreeNode)!!

                val selectedNode = checkedTreeNode.userObject as Mavenize
                val parent = findParent(checkedTreeNode)
                val projectRootNode = ProjectToBuild(parent.displayName, parent.mavenArtifact, parent.projectDirectory.path, mutableListOf(selectedNode.mavenArtifact))

                val settings = ExecutionSettings()
                XmlSerializerUtil.copyBean(settingsService.currentSettings, settings)
                checkedTreeNode.userObject as Mavenize
                settings.projectsToBuild =  mutableListOf(projectRootNode)
                val mavenRunner = MavenRunner(settings, project)
                mavenRunner.run()

                popup.cancel()
            }

            fun findParent(node: CheckedTreeNode): ProjectRootNode {
                if (node.userObject is ProjectRootNode) {
                    return node.userObject as ProjectRootNode
                }
                return findParent(node.parent as CheckedTreeNode)
            }
        })

        val selectOthersButton = JButton(message("mavenExecutor.projectsTree.selectOthers"))
        selectOthersButton.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent?) {
                projectsTree.changeAll(true)
                val checkedTreeNode = (projectsTree.treeComponent.getPathForRow(row).lastPathComponent as? CheckedTreeNode)!!
                projectsTree.treeComponent.setNodeState(checkedTreeNode, false)
                popup.cancel()
            }
        })

        val deselectOthersButton = JButton(message("mavenExecutor.projectsTree.deselectOthers"))
        deselectOthersButton.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent?) {
                projectsTree.changeAll(false)

                val checkedTreeNode = (projectsTree.treeComponent.getPathForRow(row).lastPathComponent as? CheckedTreeNode)!!
                projectsTree.treeComponent.setNodeState(checkedTreeNode, true)

                popup.cancel()
            }
        })

        panel.add(buildThisButton)
        panel.add(selectOthersButton)
        panel.add(deselectOthersButton)

        popup.show(point)
    }

    fun update() {
        projectsTree.update(settingsService.currentSettings.projectsToBuild, settingsService.currentSettings.collapseModules)

        updateProjectsToBuild()
    }

    fun updateTreeSelection() {
        projectsTree.updateTreeSelection(settingsService.currentSettings.projectsToBuild)
    }

    fun collapseAll() {
        projectsTree.collapseAll()
        updateCollapseModules()
    }

    fun expandAll() {
        projectsTree.expandAll()
        updateCollapseModules()
    }

    private fun updateCollapseModules() {
        settingsService.currentSettings.collapseModules.clear()
        settingsService.currentSettings.collapseModules.addAll(projectsTree.getCollapses())
    }

    private fun updateProjectsToBuild() {
        val selectedProjects = projectsTree.findSelectedProjects(settingsService.currentSettings.alwaysBuildPomModules)

        val projectsToBuild = selectedProjects.entries.map { toProjectToBuild(it) }.toMutableList()

        settingsService.currentSettings.projectsToBuild = projectsToBuild
    }

    private fun toProjectToBuild(selectedProjectEntry: Map.Entry<ProjectRootNode, List<Mavenize>>): ProjectToBuild {
        val projectRootNode = selectedProjectEntry.key
        val selectedModule = selectedProjectEntry.value

        val modules = selectedModule.map { it.mavenArtifact }.toMutableList()

        return ProjectToBuild(projectRootNode.displayName, projectRootNode.mavenArtifact, projectRootNode.projectDirectory.path, modules)
    }
}
package org.kubicz.mavenexecutor.view.window.panels

import com.intellij.ui.CheckboxTreeListener
import com.intellij.ui.CheckedTreeNode
import com.intellij.ui.JBColor
import com.intellij.ui.SimpleTextAttributes
import org.jetbrains.idea.maven.project.MavenProject
import org.jetbrains.idea.maven.project.MavenProjectsManager
import org.kubicz.mavenexecutor.model.MavenArtifact
import org.kubicz.mavenexecutor.model.settings.MavenArtifactFactory
import org.kubicz.mavenexecutor.model.settings.ProjectToBuild
import org.kubicz.mavenexecutor.model.tree.Mavenize
import org.kubicz.mavenexecutor.model.tree.ProjectModuleNode
import org.kubicz.mavenexecutor.model.tree.ProjectRootNode
import org.kubicz.mavenexecutor.view.components.CustomCheckboxTree
import org.kubicz.mavenexecutor.view.components.CustomCheckboxTreeBase
import java.util.*
import java.util.function.Predicate
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeModel

class MavenProjectsTree(projectsManager: MavenProjectsManager, selectedNodes: List<ProjectToBuild>) {

    private val tree: CustomCheckboxTree

    private val projectsManager = projectsManager

    private val findProject: List<ProjectToBuild>.(MavenArtifact) -> ProjectToBuild? = {
        searchedProject -> firstOrNull { it.mavenArtifact.equalsGroupAndArtifactId(searchedProject) }
    }

    private val findArtifact: List<MavenArtifact>.(MavenArtifact) -> MavenArtifact? = {
        searchedProject -> firstOrNull { it.equalsGroupAndArtifactId(searchedProject) }
    }

    private val nodeData: CheckedTreeNode.() -> Mavenize = { userObject as Mavenize }

    private val renderer = object : CustomCheckboxTreeBase.CheckboxTreeCellRendererBase() {
        override fun customizeRenderer(tree: JTree, value: Any, selected: Boolean, expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean) {
            var userObject = (value as DefaultMutableTreeNode).userObject

            if (userObject is Mavenize) {
                val fgColor = JBColor.BLACK

                textRenderer.append(userObject.displayName, SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, fgColor))
            }
        }
    }

    val treeComponent: JTree
        get() = tree

    init {
        this.tree = CustomCheckboxTree(renderer, null)

        update(selectedNodes)
    }

    fun update(selectedNodes: List<ProjectToBuild>) {
        val root = CheckedTreeNode(null)
        for (mavenProject in projectsManager.rootProjects) {
            val rootMavenArtifact = MavenArtifactFactory.from(mavenProject.mavenId)
            val rootProjectNode = CheckedTreeNode(ProjectRootNode.of(mavenProject.displayName, rootMavenArtifact, mavenProject.directoryFile))

            val project = selectedNodes.findProject(rootMavenArtifact)
            tree.setNodeState(rootProjectNode, project?.buildEntireProject() ?: false)

            createChildrenNodes(mavenProject, rootProjectNode)

            root.add(rootProjectNode)
        }
        tree.model = DefaultTreeModel(root)

        updateTreeSelection(selectedNodes)

        expandAll()
    }

    fun addCheckboxTreeListener(checkboxTreeListener: CheckboxTreeListener) {
        this.tree.addCheckboxTreeListener(checkboxTreeListener)
    }

    fun findSelectedProjects(): Map<ProjectRootNode, List<Mavenize>> {
        val projectRootMap = HashMap<ProjectRootNode, List<Mavenize>>()

        val projectRootNodes = findProjectRootNodes(tree.model)

        projectRootNodes.forEach { projectRootNode ->
            if(isAllNodesChecked(projectRootNode)) {
                projectRootMap[projectRootNode.userObject as ProjectRootNode] = emptyList()
            }
            else {
                val subModules = getCheckedNodes(projectRootNode)
                if (!subModules.isEmpty()) {
                    projectRootMap[projectRootNode.userObject as ProjectRootNode] = subModules
                }
            }
        }

        return projectRootMap
    }

    fun updateTreeSelection(selectedNodes: List<ProjectToBuild>) {
        val root = this.tree.model.root as CheckedTreeNode

        val childCount = root.childCount

        for (i in 0 until childCount) {
            val childNode = root.getChildAt(i) as CheckedTreeNode

            val selectedProject = selectedNodes.findProject(childNode.nodeData().mavenArtifact)

            val buildEntireProject = selectedProject?.buildEntireProject() ?: false
            if (buildEntireProject) {
                checkedAllTreeNode(childNode)
            } else {
                if (childNode.isLeaf) {
                    tree.setNodeState(childNode, selectedProject?.buildEntireProject() ?: false)
                } else {
                    checkedSelectedTreeNode(childNode, selectedProject?.selectedModules ?: ArrayList())
                }
            }
        }

        tree.repaint()
    }

    private fun findProjectRootNodes(model: TreeModel): List<CheckedTreeNode> {
        return findNodes(model.root as CheckedTreeNode, Predicate { it.userObject is ProjectRootNode })
    }

    private fun getCheckedNodes(root: CheckedTreeNode): List<Mavenize> {
        return findNodes(root, Predicate { it.isChecked }).map { it.nodeData() }
    }

    private fun findNodes(root: CheckedTreeNode, predicate: Predicate<CheckedTreeNode>): List<CheckedTreeNode> {
        val nodes = ArrayList<CheckedTreeNode>()

        object : Any() {
            fun collect(node: CheckedTreeNode) {
                if (node.isLeaf) {
                    if (predicate.test(node)) {
                        nodes.add(node)
                    }
                } else {
                    if (predicate.test(node)) {
                        nodes.add(node)
                    }
                    for (i in 0 until node.childCount) {
                        collect(node.getChildAt(i) as CheckedTreeNode)
                    }

                }
            }
        }.collect(root)

        return nodes
    }

    private fun isAllNodesChecked(root: CheckedTreeNode): Boolean {
        val isAllChecked = object : Any() {
            fun isAllChecked(node: CheckedTreeNode): Boolean {
                if (node.isLeaf) {
                    if (!node.isChecked) {
                        return false
                    }
                } else {
                    if (!node.isChecked) {
                        return false
                    }
                    for (i in 0 until node.childCount) {
                        return isAllChecked(node.getChildAt(i) as CheckedTreeNode)
                    }
                }
                return true
            }
        }.isAllChecked(root)

        return isAllChecked
    }

    private fun checkedAllTreeNode(node: CheckedTreeNode) {
        tree.setNodeState(node, true)

        val childCount = node.childCount

        for (i in 0 until childCount) {
            val childNode = node.getChildAt(i) as CheckedTreeNode

            if (childNode.isLeaf) {
                tree.setNodeState(childNode, true)
            } else {
                checkedAllTreeNode(childNode)
            }
        }
    }

    private fun checkedSelectedTreeNode(node: CheckedTreeNode, selectedNodes: List<MavenArtifact>) {
        if (node.isLeaf) {
            tree.setNodeState(node, selectedNodes.findArtifact(node.nodeData().mavenArtifact) != null)
            return
        }

        val childCount = node.childCount

        for (i in 0 until childCount) {
            val childNode = node.getChildAt(i) as CheckedTreeNode

            if (childNode.isLeaf) {
                tree.setNodeState(childNode, selectedNodes.findArtifact(childNode.nodeData().mavenArtifact) != null)
            } else {
                checkedSelectedTreeNode(childNode, selectedNodes)
            }
        }
    }

    private fun createChildrenNodes(rootProject: MavenProject, root: CheckedTreeNode) {
        for (mavenProject in projectsManager.findInheritors(rootProject)) {
            val nodeMavenArtifact = MavenArtifactFactory.from(mavenProject.mavenId)
            val projectNode = CheckedTreeNode(ProjectModuleNode(mavenProject.displayName, nodeMavenArtifact))

            root.add(projectNode)

            createChildrenNodes(mavenProject, projectNode)
        }
    }

    private fun expandAll() {
        var size = tree.rowCount
        var i = 0
        while (i < size) {
            tree.expandRow(i)

            i++
            size = tree.rowCount // returns only visible nodes
        }
    }

}

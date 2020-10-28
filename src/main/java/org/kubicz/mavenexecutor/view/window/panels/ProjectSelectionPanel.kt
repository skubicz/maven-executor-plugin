package org.kubicz.mavenexecutor.view.window.panels

import com.intellij.icons.AllIcons
import com.intellij.openapi.project.Project
import com.intellij.util.ui.JBUI
import org.kubicz.mavenexecutor.model.settings.MavenArtifactFactory
import org.kubicz.mavenexecutor.view.MavenExecutorBundle.Companion.message
import org.kubicz.mavenexecutor.view.MavenProjectsHelper
import org.kubicz.mavenexecutor.view.components.CustomComboBox
import org.kubicz.mavenexecutor.view.components.MavenizeComboBoxItem
import org.kubicz.mavenexecutor.view.window.ExecutionSettingsService
import java.awt.Dimension
import javax.swing.*

class ProjectSelectionPanel(private val project: Project,
                            private val settingsService: ExecutionSettingsService,
                            private val projectsHelper: MavenProjectsHelper,
                            private val projectsTreePane: MavenProjectsTreePanel,
                            private var changeListener: () -> Unit = {}) {

    private var panel = JPanel()

    private var projectComboBox = CustomComboBox<MavenizeComboBoxItem>()

    private var expandButton = JButton()
    private var collapseButton = JButton()

    val component
        get() : JComponent = panel

    init {
        createAll()
    }

    private fun createAll() {
        val groupLayout = GroupLayout(panel)
        groupLayout.autoCreateGaps = false
        groupLayout.autoCreateContainerGaps = false
        panel.layout = groupLayout
        panel.border = JBUI.Borders.customLine(UIManager.getColor("Component.borderColor"), 0, 0, 0, 0)

        projectComboBox.setMinimumAndPreferredWidth(30)

        projectComboBox.addItem(MavenizeComboBoxItem(message("mavenExecutor.projectSelection.label"), MavenProjectsHelper.EMPTY_ARTIFACT))
        projectsHelper.manager.rootProjects.forEach {
            projectComboBox.addItem(MavenizeComboBoxItem(it.displayName, MavenArtifactFactory.from(it.mavenId)))
        }

        update()

        projectComboBox.addItemListener {
            settingsService.currentSettings.selectedProject = (projectComboBox.selectedItem as MavenizeComboBoxItem).mavenArtifact

            changeListener()
        }

        expandButton.icon = AllIcons.Actions.Expandall
        expandButton.addActionListener { projectsTreePane.expandAll() }
        expandButton.preferredSize = Dimension(30, expandButton.height)
        expandButton.size = Dimension(30, expandButton.height)
        expandButton.minimumSize = Dimension(30, expandButton.height)
        expandButton.maximumSize = Dimension(30, expandButton.height)
        expandButton.toolTipText = message("mavenExecutor.expandAll.toolTip")

        collapseButton.icon = AllIcons.Actions.Collapseall
        collapseButton.addActionListener { projectsTreePane.collapseAll() }
        collapseButton.preferredSize = Dimension(30, collapseButton.height)
        collapseButton.size = Dimension(30, collapseButton.height)
        collapseButton.minimumSize = Dimension(30, collapseButton.height)
        collapseButton.maximumSize = Dimension(30, collapseButton.height)
        collapseButton.toolTipText = message("mavenExecutor.collapseAll.toolTip")

        groupLayout.setHorizontalGroup(
                groupLayout.createSequentialGroup()
                        .addComponent(projectComboBox)
                        .addComponent(expandButton)
                        .addComponent(collapseButton)
        )
        groupLayout.setVerticalGroup(
                groupLayout.createSequentialGroup()
                        .addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(projectComboBox, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, java.lang.Short.MAX_VALUE.toInt())
                                .addComponent(expandButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, java.lang.Short.MAX_VALUE.toInt())
                                .addComponent(collapseButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, java.lang.Short.MAX_VALUE.toInt())
                        )
        )
    }

    fun update() {
        val selectedProject = settingsService.currentSettings.selectedProject

        for (i in 0 until projectComboBox.itemCount) {
            val item = projectComboBox.getItemAt(i)
            if (item.mavenArtifact.equalsGroupAndArtifactId(selectedProject)) {
                projectComboBox.selectedIndex = i
            }
        }
    }

}

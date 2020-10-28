package org.kubicz.mavenexecutor.view.window.panels

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.vfs.VirtualFile
import org.kubicz.mavenexecutor.model.MavenArtifact
import org.kubicz.mavenexecutor.model.settings.MavenArtifactFactory
import org.kubicz.mavenexecutor.model.settings.ProjectToBuildBuilder
import org.kubicz.mavenexecutor.view.MavenExecutorBundle.Companion.message
import org.kubicz.mavenexecutor.view.MavenProjectsHelper
import org.kubicz.mavenexecutor.view.window.ExecutionSettingsService
import org.kubicz.mavenexecutor.view.window.LastModifiedFilesService
import java.awt.Dimension
import java.awt.GridLayout
import java.util.*
import javax.swing.*

class SelectCurrentPanel(
    private val projectsHelper: MavenProjectsHelper,
    private val settingsService: ExecutionSettingsService,
    private val fileEditorManager: FileEditorManager, selectCurrentListener: () -> Unit,
    private val lastModifiedFilesService: LastModifiedFilesService
) {

    private var panel = JPanel()

    private var selectCurrentButton: JButton = JButton(message("mavenExecutor.selectCurrent.label"))

    private val selectModifiedButton: JButton = JButton(message("mavenExecutor.selectModified.label"))

    init {
        panel.layout = BoxLayout(panel, BoxLayout.PAGE_AXIS)
        panel.maximumSize = Dimension(Integer.MAX_VALUE, selectCurrentButton.maximumSize.getHeight().toInt())
        panel.preferredSize = Dimension(Integer.MAX_VALUE, selectCurrentButton.maximumSize.getHeight().toInt())

        val groupLayout = GroupLayout(panel)
        groupLayout.autoCreateGaps = true
        groupLayout.autoCreateContainerGaps = true

        val innerPanel = JPanel(GridLayout(0, 2))
        innerPanel.maximumSize = Dimension(Integer.MAX_VALUE, selectCurrentButton.maximumSize.getHeight().toInt())
        innerPanel.preferredSize = Dimension(Integer.MAX_VALUE, selectCurrentButton.maximumSize.getHeight().toInt())

        selectCurrentButton.toolTipText = message("mavenExecutor.selectCurrent.toolTip")
        selectCurrentButton.maximumSize = Dimension(Integer.MAX_VALUE, selectCurrentButton.maximumSize.getHeight().toInt())
        selectCurrentButton.preferredSize = Dimension(Integer.MAX_VALUE, selectCurrentButton.preferredSize.getHeight().toInt())
        selectCurrentButton.icon = IconLoader.getIcon("/icons/selectall.png", this.javaClass)

        selectCurrentButton.addActionListener {
            selectByOpen()

            selectCurrentListener()
        }

        innerPanel.add(selectCurrentButton)

        selectModifiedButton.toolTipText = message("mavenExecutor.selectModified.toolTip")
        selectModifiedButton.maximumSize = Dimension(Integer.MAX_VALUE, selectCurrentButton.maximumSize.getHeight().toInt())
        selectModifiedButton.preferredSize = Dimension(Integer.MAX_VALUE, selectCurrentButton.preferredSize.getHeight().toInt())
        selectModifiedButton.icon = IconLoader.getIcon("/icons/selectall.png", this.javaClass)

        selectModifiedButton.addActionListener {
            selectByModified()

            selectCurrentListener()
        }

        innerPanel.add(selectModifiedButton)

        panel.add(innerPanel)
    }

    val component
        get() : JComponent = panel

    private fun selectByModified() {
        val files = lastModifiedFilesService.getFiles()

        if(files.isEmpty()) {
            Notifications.Bus.notify(Notification("Maven Executor", "", message("mavenExecutor.notFoundModifiedFiles.warning"), NotificationType.INFORMATION))
        }
        else {
            selectByFiles(files)
        }
    }

    private fun selectByOpen() {
        val fileEditors = fileEditorManager.selectedEditors

        if(fileEditors.isEmpty()) {
            Notifications.Bus.notify(Notification("Maven Executor", "", message("mavenExecutor.notFoundOpenedFiles.warning"), NotificationType.INFORMATION))
        }
        else {
            fileEditors.map { it.file!! }.toList().let {
                selectByFiles(it)
            }
        }
    }

    private fun selectByFiles(files: Collection<VirtualFile>) {
        val projectsToBuild = ArrayList<ProjectToBuildBuilder>()

        files.forEach { file ->
            val currentProject = projectsHelper.manager.findContainingProject(file)

            currentProject?.let {
                val currentRootProject = projectsHelper.manager.findRootProject(currentProject)

                currentRootProject?.let {
                    val selectedArtifact = MavenArtifactFactory.from(currentProject.mavenId)
                    val rootArtifact = MavenArtifactFactory.from(currentRootProject.mavenId)

                    val projectToBuildBuilder = projectsToBuild
                            .firstOrNull { item -> item.mavenArtifact!!.equalsGroupAndArtifactId(rootArtifact) }
                            ?: ProjectToBuildBuilder(currentRootProject.displayName, rootArtifact, currentRootProject.directoryFile.path)

                    if (!projectsToBuild.contains(projectToBuildBuilder)) {
                        projectsToBuild.add(projectToBuildBuilder)
                    }

                    if (selectedArtifactIsNotRoot(selectedArtifact, rootArtifact)) {
                        projectToBuildBuilder.addArtifact(selectedArtifact)

                        projectsHelper.manager.findInheritors(currentProject).forEach {inheritorArtifact ->
                            val artifact = MavenArtifactFactory.from(inheritorArtifact.mavenId)

                            projectToBuildBuilder.addArtifact(artifact)
                        }
                    }
                }

            }

        }

        settingsService
                .currentSettings
                .projectsToBuild = projectsToBuild.map(ProjectToBuildBuilder::build).toMutableList()

    }

    private fun selectedArtifactIsNotRoot(selectedArtifact : MavenArtifact, rootArtifact : MavenArtifact) : Boolean = !rootArtifact.equalsGroupAndArtifactId(selectedArtifact)

}
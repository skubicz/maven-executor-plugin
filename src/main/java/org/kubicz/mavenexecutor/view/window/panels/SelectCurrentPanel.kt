package org.kubicz.mavenexecutor.view.window.panels

import com.intellij.openapi.fileEditor.FileEditorManager
import org.kubicz.mavenexecutor.model.MavenArtifact
import org.kubicz.mavenexecutor.model.settings.MavenArtifactFactory
import org.kubicz.mavenexecutor.model.settings.ProjectToBuildBuilder
import org.kubicz.mavenexecutor.view.window.ExecutionSettingsService
import org.jetbrains.idea.maven.project.MavenProjectsManager
import org.kubicz.mavenexecutor.view.MavenExecutorBundle
import org.kubicz.mavenexecutor.view.MavenExecutorBundle.Companion.message
import java.awt.Dimension
import java.util.*
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel

class SelectCurrentPanel(projectsManager: MavenProjectsManager, settingsService: ExecutionSettingsService,
                         fileEditorManager: FileEditorManager, selectCurrentListener: () -> Unit) {

    private var panel = JPanel()

    private val projectsManager = projectsManager

    private val settingsService = settingsService

    private val fileEditorManager = fileEditorManager

    private var selectCurrentButton: JButton = JButton(message("mavenExecutor.selectCurrent.label"))

    init {
        panel.layout = BoxLayout(panel, BoxLayout.PAGE_AXIS)
        selectCurrentButton.toolTipText = message("mavenExecutor.selectCurrent.toolTip")
        selectCurrentButton.maximumSize = Dimension(Integer.MAX_VALUE, selectCurrentButton.maximumSize.getHeight().toInt())
        selectCurrentButton.preferredSize = Dimension(Integer.MAX_VALUE, selectCurrentButton.preferredSize.getHeight().toInt())

        selectCurrentButton.addActionListener {
            actionListener()

            selectCurrentListener()
        }

        panel.add(selectCurrentButton)
    }

    val component
        get() : JComponent = panel

    private fun actionListener() {
        val projectsManager = projectsManager

        val fileEditors = fileEditorManager.selectedEditors

        val projectsToBuild = ArrayList<ProjectToBuildBuilder>()

        fileEditors.forEach { fileEditor ->
            fileEditor.file?.let { file ->
                val currentProject = projectsManager.findContainingProject(file)

                currentProject?.let {
                    val currentRootProject = projectsManager.findRootProject(currentProject)

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

                            projectsManager.findInheritors(currentProject).forEach {inheritorArtifact ->
                                val artifact = MavenArtifactFactory.from(inheritorArtifact.mavenId)

                                projectToBuildBuilder.addArtifact(artifact)
                            }
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
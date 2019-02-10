package org.kubicz.mavenexecutor.view.window

import com.intellij.ProjectTopics
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootEvent
import com.intellij.openapi.roots.ModuleRootListener
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.content.ContentFactory
import org.kubicz.mavenexecutor.view.window.panels.ConfigPanel
import org.kubicz.mavenexecutor.view.window.panels.FavoritePanel
import org.kubicz.mavenexecutor.view.window.panels.MavenProjectsTreePanel
import org.kubicz.mavenexecutor.view.window.panels.SelectCurrentPanel
import org.jetbrains.idea.maven.project.MavenProjectsManager
import java.awt.Dimension
import java.awt.GridBagLayout
import javax.swing.JPanel


class MavenExecutorToolWindow(private var project: Project) {

    private var toolWindow: ToolWindow? = null

    private val projectsManager = MavenProjectsManager.getInstance(project)

    private val toolWindowContent = SimpleToolWindowPanel(true, true)

    private var settingsService = ExecutionSettingsService.getInstance(project)

    private val configPanel = ConfigPanel(project, settingsService)

    private val projectsTreePanel = MavenProjectsTreePanel(projectsManager, settingsService, configPanel::updateRunButton)

    private val mainContent =  JPanel(GridBagLayout())

    private val selectCurrentPanel = SelectCurrentPanel(projectsManager, settingsService, FileEditorManager.getInstance(project), this::updateProjectTree)

    private val favoritePanel = FavoritePanel(settingsService, this::updateWithoutFavorite)


    fun createToolWindowContent(toolWindow: ToolWindow) {
        this.toolWindow = toolWindow

        createWindowToolbar()

        createWindowContent()

        project.messageBus.connect()
                .subscribe(ProjectTopics.PROJECT_ROOTS, object : ModuleRootListener {
                    override fun rootsChanged(event: ModuleRootEvent?) {
                        configPanel.updateProfile()
                        projectsTreePanel.update()
                    }
                })

        val contentFactory = ContentFactory.SERVICE.getInstance()
        val content = contentFactory.createContent(toolWindowContent, "", false)
        this.toolWindow!!.contentManager.addContent(content)
    }

    private fun createWindowToolbar() {
        val actionManager = ActionManager.getInstance()
        val actionToolbar = actionManager.createActionToolbar("MavenExecutorPanel", actionManager
                .getAction("MavenExecutor.ActionsToolbar") as DefaultActionGroup, true)

        toolWindowContent.setToolbar(actionToolbar.component)
    }

    private fun createWindowContent() {
        mainContent.maximumSize = Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE)

        mainContent.add(configPanel.component, bagConstraintsBuilder().fillHorizontal().gridx(0).gridy(0).weightx(1.0).gridwidth(2).build())
        mainContent.add(projectsTreePanel.component, bagConstraintsBuilder().fillBoth().weightx(1.0).gridx(0).gridy(1).build())
        mainContent.add(favoritePanel.component, bagConstraintsBuilder().fillVertical().weightx(0.0).weighty(1.0).gridx(1).gridy(1).build())
        mainContent.add(selectCurrentPanel.component, bagConstraintsBuilder().fillHorizontal().weightx(1.0).weighty(0.0).gridx(0).gridy(2).build())

        toolWindowContent.setContent(mainContent)
    }


    fun updateWithoutFavorite() {
        configPanel.update()
        updateProjectTree()
    }

    fun updateAll() {
        configPanel.update()
        updateProjectTree()
        updateFavorite()
    }

    fun updateProjectTree() {
        projectsTreePanel.updateTreeSelection()
    }


    fun updateFavorite() {
        favoritePanel.refresh()
    }

    private fun bagConstraintsBuilder(): GridBagConstraintsBuilder {
        return GridBagConstraintsBuilder()
    }

    companion object {

        fun getInstance(project: Project): MavenExecutorToolWindow {
            return ServiceManager.getService(project, MavenExecutorToolWindow::class.java)
        }
    }

}

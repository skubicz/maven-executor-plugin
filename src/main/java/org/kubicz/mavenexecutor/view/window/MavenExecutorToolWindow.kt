package org.kubicz.mavenexecutor.view.window

import com.intellij.ProjectTopics
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootEvent
import com.intellij.openapi.roots.ModuleRootListener
import com.intellij.openapi.wm.ToolWindow
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiTreeChangeAdapter
import com.intellij.psi.PsiTreeChangeEvent
import com.intellij.ui.content.ContentFactory
import org.kubicz.mavenexecutor.view.MavenProjectsHelper
import org.kubicz.mavenexecutor.view.window.panels.*
import java.awt.Dimension
import java.awt.GridBagLayout
import javax.swing.JPanel


class MavenExecutorToolWindow(private var project: Project) {

    private var toolWindow: ToolWindow? = null

    private val settingsService = ExecutionSettingsService.getInstance(project)

    private val projectsHelper = MavenProjectsHelper.getInstance(project)

    private val toolWindowContent = MavenExecutorToolWindowContent()

    private val configPanel = ConfigPanel(project, settingsService, projectsHelper)

    private val lastModifiedFilesService = LastModifiedFilesService.getInstance(project)

    private val projectsTreePanel = MavenProjectsTreePanel(project, projectsHelper, settingsService)

    private val projectSelectionPanel = ProjectSelectionPanel(project, settingsService, projectsHelper, projectsTreePanel, this::updateAllWithoutProjectSelection)

    private val mainContent =  JPanel(GridBagLayout())

    private val selectCurrentPanel = SelectCurrentPanel(projectsHelper, settingsService, FileEditorManager.getInstance(project), this::updateProjectTree, lastModifiedFilesService)

    private val favoritePanel = FavoritePanel(project, settingsService, this::updateWithoutFavorite)

    private val logoPanel = LogoPanel(project, settingsService)

    fun createToolWindowContent(toolWindow: ToolWindow) {
        this.toolWindow = toolWindow

        createWindowToolbar()

        createWindowContent()

        project.messageBus.connect()
                .subscribe(ProjectTopics.PROJECT_ROOTS, object : ModuleRootListener {
                    override fun rootsChanged(event: ModuleRootEvent) {
                        configPanel.updateProfile()
                        projectsTreePanel.update()
                    }
                })

        PsiManager.getInstance(project).addPsiTreeChangeListener(object : PsiTreeChangeAdapter() {
            override fun childrenChanged(event: PsiTreeChangeEvent) {
                event.file?.virtualFile?.let {
                    val mavenProject = projectsHelper.manager.findContainingProject(it)
                    if(mavenProject != null) {
                        lastModifiedFilesService.addFile(it)
                    }
                }
            }
        }, toolWindowContent)

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
        mainContent.add(projectSelectionPanel.component, bagConstraintsBuilder().fillHorizontal().gridx(0).gridy(1).weightx(1.0).gridwidth(1).build())
        mainContent.add(projectsTreePanel.component, bagConstraintsBuilder().fillBoth().weightx(1.0).weighty(0.0).gridx(0).gridy(2).build())
        mainContent.add(favoritePanel.component, bagConstraintsBuilder().fillVertical().weightx(0.0).weighty(1.0).gridx(1).gridy(1).gridheight(2).build())
        mainContent.add(selectCurrentPanel.component, bagConstraintsBuilder().fillHorizontal().weightx(1.0).weighty(0.0).gridx(0).gridy(3).build())
        mainContent.add(logoPanel.component, bagConstraintsBuilder().fillBoth().weightx(0.0).weighty(0.0).gridx(1).gridy(3).build())

        toolWindowContent.setContent(mainContent)
    }


    fun updateWithoutFavorite() {
        configPanel.update()
        projectSelectionPanel.update()
        logoPanel.refresh()
        updateProjectTree()
    }

    fun updateAllWithoutProjectSelection() {
        configPanel.update()
        projectsTreePanel.update()
        logoPanel.refresh()
        updateFavorite()
    }

    fun updateAll() {
        configPanel.update()
        projectsTreePanel.update()
        projectSelectionPanel.update()
        logoPanel.refresh()
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

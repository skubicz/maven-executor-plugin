package org.kubicz.mavenexecutor.view.window.panels

import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.util.EnvVariablesTable
import com.intellij.execution.util.EnvironmentVariable
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import com.intellij.util.ui.JBUI
import org.jetbrains.idea.maven.project.MavenProjectsManager
import org.kubicz.mavenexecutor.model.MavenGroupAndArtifactKey
import org.kubicz.mavenexecutor.model.settings.ProjectToBuild
import org.kubicz.mavenexecutor.view.MavenExecutorBundle.Companion.message
import org.kubicz.mavenexecutor.view.MavenProjectsHelper
import org.kubicz.mavenexecutor.view.components.ProjectComboBoxItem
import org.kubicz.mavenexecutor.view.window.ExecutionSettingsService
import org.kubicz.mavenexecutor.view.window.GridBagConstraintsBuilder
import org.kubicz.mavenexecutor.view.window.MavenResultCommandLineMode
import org.kubicz.mavenexecutor.view.window.actions.toolbar.MavenRunner
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.GridLayout
import javax.swing.*
import javax.swing.BoxLayout
import javax.swing.border.EmptyBorder

class OpenMavenRunSettingsPanel(private val project: Project) {

    private val panel = JPanel()
    private val settingsService = ExecutionSettingsService.getInstance(project)
    private val settings = settingsService.currentSettings
    private var mavenRunner = MavenRunner(settings, project)
    private val projectsHelper = MavenProjectsHelper.getInstance(project)
    private val runnerAndConfigSettings = mutableMapOf<MavenGroupAndArtifactKey, RunnerAndConfigurationSettings>()
    private var jvmOptionTextArea = JBTextArea()
    private val commandLineTextFiled = JTextField()
    private val simplyModeRadio = JRadioButton(message("mavenExecutor.mavenRunSettings.simplified.checkbox"))
    private val advanceModeRadio = JRadioButton(message("mavenExecutor.mavenRunSettings.realCommand.checkbox"))
    private val projectsComboBox = ComboBox<ProjectComboBoxItem>()
    private var alwaysBuildPomModules = JCheckBox(message("mavenExecutor.mavenRunSettings.alwaysBuildPomModules.checkbox"))

    val component
        get() : JComponent = panel

    fun create(): JComponent {
        initRunnerAndConfigSettings()

        val refresh: () -> Unit = {
            initRunnerAndConfigSettings()

            mavenRunner = MavenRunner(settingsService.currentSettings, project)
            setCommandLine(mavenRunner)
        }

        val leftPanel = JPanel(GridBagLayout())

        val rightPanel = JPanel(GridLayout(1, 1))
        val bottomPanel = JPanel()
        bottomPanel.layout = BoxLayout(bottomPanel, BoxLayout.PAGE_AXIS)

        val configPanel = ConfigPanel(project, settingsService, projectsHelper,true, refresh)
        leftPanel.add(configPanel.component, GridBagConstraintsBuilder().fillHorizontal().insetTop(0).anchor(GridBagConstraints.NORTH).weightx(1.0).weighty(1.0).gridx(0).gridy(0).build())

        alwaysBuildPomModules.isSelected = settingsService.currentSettings.alwaysBuildPomModules
        alwaysBuildPomModules.border = JBUI.Borders.empty(0, 7, 0, 0)
        alwaysBuildPomModules.toolTipText = message("mavenExecutor.mavenRunSettings.alwaysBuildPomModules.toolTip")
        alwaysBuildPomModules.addActionListener {
            settingsService.currentSettings.alwaysBuildPomModules = alwaysBuildPomModules.isSelected
        }
        leftPanel.add(alwaysBuildPomModules, GridBagConstraintsBuilder().fillHorizontal().insetTop(0).anchor(GridBagConstraints.SOUTH).weightx(1.0).weighty(1.0).gridx(0).gridy(1).build())

        val jvmLabelPanel = JPanel(GridBagLayout())
        jvmLabelPanel.add(JLabel(message("mavenExecutor.mavenRunSettings.mainJVMOptions.label")), GridBagConstraintsBuilder().fillHorizontal().insetTop(0).anchor(GridBagConstraints.NORTH).weightx(1.0).weighty(1.0).gridx(0).gridy(0).build())

        val jvmOptionPanel = createJvmOptionTextPanel()
        jvmLabelPanel.add(jvmOptionPanel, GridBagConstraintsBuilder().fillHorizontal().insetTop(0).anchor(GridBagConstraints.NORTH).weightx(1.0).weighty(1.0).gridx(0).gridy(1).build())

        leftPanel.add(jvmLabelPanel, GridBagConstraintsBuilder().fillHorizontal().insetTop(0).anchor(GridBagConstraints.SOUTH).weightx(1.0).weighty(1.0).gridx(0).gridy(2).build())
        leftPanel.border = JBUI.Borders.empty(0, 0, 0, 0)

        val envTable = EnvVariablesTable()
        envTable.tableView.preferredScrollableViewportSize = Dimension(450, 505)
        envTable.setValues(toEnvVariables(settingsService.currentSettings.environmentProperties))
        val envTablePanel = JPanel(GridLayout(1, 1))
        envTablePanel.add(envTable.component)
        rightPanel.add(envTablePanel)

        val resultCommandTitlePanel = createResultCommandTitlePanel()

        resultCommandTitlePanel.preferredSize = Dimension(950, 40)
        resultCommandTitlePanel.maximumSize = resultCommandTitlePanel.preferredSize
        resultCommandTitlePanel.border = JBUI.Borders.empty(10, 0, 0, 0)
        bottomPanel.add(resultCommandTitlePanel)
        val commandScrollPane = createResultCommandPanel()
        bottomPanel.add(commandScrollPane)

        setCommandLine(mavenRunner)

        val leftAndRightPanel = JPanel(GridLayout(1, 2))
        leftAndRightPanel.add(leftPanel)
        leftAndRightPanel.add(rightPanel)

        val bagPanel = JPanel()
        bagPanel.layout = BoxLayout(bagPanel, BoxLayout.PAGE_AXIS)
        bagPanel.add(leftAndRightPanel)
        bagPanel.add(bottomPanel)

        panel.add(bagPanel)

        return component
    }

    private fun createResultCommandTitlePanel(): JComponent {
        val projects = settingsService.currentRootProjectsAsMavenize(MavenProjectsManager.getInstance(project))
                .map{ ProjectComboBoxItem(it.displayName, it.mavenArtifact, it.projectDirectory) }

        projects.forEach {
            projectsComboBox.addItem(it)

            if (settingsService.resultCommandLineChosenProject.equalsGroupAndArtifactId(it.mavenArtifact)) {
                projectsComboBox.selectedItem = it
            }
        }

        projectsComboBox.addItemListener {
            setCommandLine(mavenRunner)
        }

        simplyModeRadio.border = JBUI.Borders.empty(0, 0, 0, 20)
        simplyModeRadio.isSelected = settingsService.resultCommandLineMode == MavenResultCommandLineMode.SIMPLE
        simplyModeRadio.addItemListener {
            setCommandLine(mavenRunner)
        }

        advanceModeRadio.border = EmptyBorder(0, 0, 0, 10)
        advanceModeRadio.isSelected = !simplyModeRadio.isSelected
        advanceModeRadio.addItemListener {
            setCommandLine(mavenRunner)
        }
        val commandLineRadioGroup = ButtonGroup()
        commandLineRadioGroup.add(simplyModeRadio)
        commandLineRadioGroup.add(advanceModeRadio)

        val resultCommandTitlePanel = JPanel()
        resultCommandTitlePanel.layout = BoxLayout(resultCommandTitlePanel, BoxLayout.LINE_AXIS)

        val radioPanel = JPanel()
        radioPanel.layout = BoxLayout(radioPanel, BoxLayout.LINE_AXIS)
        radioPanel.add(simplyModeRadio)
        radioPanel.add(advanceModeRadio)

        val resultLabel = JLabel(message("mavenExecutor.mavenRunSettings.resultCommand.label"))
        resultCommandTitlePanel.add(resultLabel)

        projectsComboBox.setMinimumAndPreferredWidth(300)
        projectsComboBox.maximumSize = Dimension(300, 30)
        resultCommandTitlePanel.add(projectsComboBox)
        resultCommandTitlePanel.add(JPanel())
        resultCommandTitlePanel.add(radioPanel)

        return resultCommandTitlePanel
    }

    private fun createResultCommandPanel(): JComponent {
        commandLineTextFiled.isEditable = false
        commandLineTextFiled.border = null
        commandLineTextFiled.background = null
        val commandScrollPane = JBScrollPane(commandLineTextFiled)
        commandScrollPane.preferredSize = Dimension(950, 50)
        commandScrollPane.horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
        commandScrollPane.verticalScrollBarPolicy = ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER
        commandScrollPane.border = JBUI.Borders.empty( 0 )

        val panel = JPanel()
        panel.maximumSize = Dimension(950, 50)
        panel.add(commandScrollPane)
        return panel
    }

    private fun createJvmOptionTextPanel(): JComponent {
        jvmOptionTextArea.columns = 20
        jvmOptionTextArea.rows = 11
        jvmOptionTextArea.scrollableTracksViewportWidth
        jvmOptionTextArea.text = settings.jvmOptions.joinToString("\n")
        jvmOptionTextArea.document.addDocumentListener(object : DocumentAdapter() {
            override fun textChanged(e: javax.swing.event.DocumentEvent) {
                settings.jvmOptions = jvmOptionTextArea.text.split("\n").filter { it.isNotBlank() }.toMutableList()

                mavenRunner = MavenRunner(settings, project)
                setCommandLine(mavenRunner)
            }
        })

        val jvmOptionScrollPane = JBScrollPane(jvmOptionTextArea)
        jvmOptionScrollPane.preferredSize = Dimension(465, 250)

        val panel = JPanel()
        panel.maximumSize = Dimension(480, 255)
        panel.preferredSize = Dimension(480, 255)
        panel.border = JBUI.Borders.empty(0, 0, 10, 12)
        panel.add(jvmOptionScrollPane)

        return panel
    }

    private fun setCommandLine(mavenRunner: MavenRunner) {
        try {
            initRunnerAndConfigSettings()

            val selectedMavenArtifact = (projectsComboBox.selectedItem as ProjectComboBoxItem).mavenArtifact
            val runnerSettings = runnerAndConfigSettings.get(selectedMavenArtifact.getGroupAndArtifactKey())!!

            settingsService.resultCommandLineChosenProject = selectedMavenArtifact
            if(simplyModeRadio.isSelected) {
                settingsService.resultCommandLineMode = MavenResultCommandLineMode.SIMPLE
                commandLineTextFiled.text = mavenRunner.simplifiedCommandLine(runnerSettings)
            }
            else {
                settingsService.resultCommandLineMode = MavenResultCommandLineMode.ADVANCE
                commandLineTextFiled.text = mavenRunner.commandLine(runnerSettings)
            }
        }
        catch (e: Exception) {
            settingsService.resultCommandLineMode = MavenResultCommandLineMode.SIMPLE
            commandLineTextFiled.text = "Maven Error: " + e.message
//            e.message?.let {
//                Notifications.Bus.notify(Notification("Maven Executor", "",  it, NotificationType.WARNING))
//            }
        }
    }

    private fun initRunnerAndConfigSettings() {
        val projects = settingsService.currentRootProjectsAsMavenize(MavenProjectsManager.getInstance(project))
                .map{ ProjectComboBoxItem(it.displayName, it.mavenArtifact, it.projectDirectory) }
                .toTypedArray()

        val projectToBuild = projects.filterNot { mavenRunner.runnerAndConfigurationSettingsList.contains(it.mavenArtifact.getGroupAndArtifactKey()) }
                .map { ProjectToBuild(it.displayName, it.mavenArtifact, it.projectDirectory.path) }

        runnerAndConfigSettings.clear()
        runnerAndConfigSettings.putAll(mavenRunner.runnerAndConfigurationSettingsList)
        runnerAndConfigSettings.putAll(mavenRunner.createRunConfigurations(projectToBuild))
    }

    private fun toEnvVariables(environmentProperties: MutableMap<String, String>): List<EnvironmentVariable> {
        return environmentProperties.entries.map { EnvironmentVariable(it.key, it.value, false) }.toList()
    }

}
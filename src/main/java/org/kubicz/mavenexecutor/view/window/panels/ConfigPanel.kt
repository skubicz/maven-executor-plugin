package org.kubicz.mavenexecutor.view.window.panels

import com.google.common.collect.Lists
import com.intellij.ide.ui.LafManagerListener
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileTypes.PlainTextFileType
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.EditorComboBoxRenderer
import com.intellij.ui.EditorTextField
import com.intellij.ui.StringComboboxEditor
import com.intellij.ui.components.fields.ExpandableTextField
import com.intellij.ui.components.fields.IntegerField
import com.intellij.util.ui.JBUI
import org.jetbrains.idea.maven.execution.MavenArgumentsCompletionProvider
import org.kubicz.mavenexecutor.model.settings.History
import org.kubicz.mavenexecutor.model.settings.VisibleSetting
import org.kubicz.mavenexecutor.view.MavenExecutorBundle.Companion.message
import org.kubicz.mavenexecutor.view.MavenProjectsHelper
import org.kubicz.mavenexecutor.view.components.CheckboxCombo
import org.kubicz.mavenexecutor.view.components.CheckboxCombo.CheckBoxItem
import org.kubicz.mavenexecutor.view.window.ExecutionSettingsService
import java.awt.Dimension
import java.awt.GridLayout
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import javax.swing.*
import javax.swing.border.EmptyBorder
import kotlin.math.ceil

class ConfigPanel(project: Project,
                  settingsService: ExecutionSettingsService,
                  private val projectsHelper: MavenProjectsHelper,
                  private val mainConfigMode: Boolean = false,
                  private var changeListener: () -> Unit = {}) {

    private val project = project;

    private var panel = JPanel()

    private var goalsSubPanel = JPanel()

    private var mutableSubPanel: JPanel? = null

    private var propertiesSubPanel = JPanel()

    private var optionalJvmOptionsSubPanel = JPanel()

    private var goals = AutoCompletionExpandableTextFieldWithHistory(project, MavenArgumentsCompletionProvider(project),
            settingsService.currentSettings.goalsAsText(), settingsService.goalsHistory)

    private var additionalParameters = ExpandableTextField()

    private var additionalParametersLabel = JLabel(message("mavenExecutor.additionalParameters.label"))

    private var additionalParametersSubPanel = JPanel()

    private var profilesSubPanel = JPanel()

    private var profilesLabel = JLabel(message("mavenExecutor.profiles.label"))

    private var profiles = createProfilesCheckboxCombo()

    private var offlineModeCheckBox = JCheckBox(message("mavenExecutor.offline.label"))

    private var skipTestCheckBox = JCheckBox(message("mavenExecutor.skipTest.label"))

    private var alwaysUpdateModeCheckBox = JCheckBox(message("mavenExecutor.alwaysUpdateMode.label"))

    private var threadsLabel = JLabel(message("mavenExecutor.threads.label"))

    private var threadsTextField = IntegerField(null, 0, 99)

    private var optionalJvmOptionsCheckBox = JCheckBox(message("mavenExecutor.optionalJvmOptions.label"))

    private var optionalJvmOptionsComboBox = ComboBox<String>()

    private val settingsService = settingsService

    private val setHistory: ComboBox<String>.(History) -> Unit = { history -> model = DefaultComboBoxModel(history.asArray())}

    private val initEditor: ComboBox<String>.(String) -> Unit = {
        val comboEditor = StringComboboxEditor(project, PlainTextFileType.INSTANCE, this)
        renderer = EditorComboBoxRenderer(editor)
        isLightWeightPopupEnabled = false
        isEditable = true
        editor = comboEditor
        isFocusable = true
        editor.item = it
    }

    fun createProfilesCheckboxCombo(): CheckboxCombo {
        return CheckboxCombo(project, object : CheckboxCombo.SelectionChangedListener {
            override fun selectionChanged(selectedItems: MutableList<String>) {
                settingsService.currentSettings.profiles = selectedItems

                changeListener()
            }
        })
    }

    val component
        get() : JComponent = panel

    init {
        ApplicationManager.getApplication().messageBus.connect()
            .subscribe(LafManagerListener.TOPIC, LafManagerListener { updateProfile() })

        createAll()
    }

    private fun createAll() {
        panel.layout = BoxLayout(panel, BoxLayout.PAGE_AXIS)
        mutableSubPanel = JPanel()

        createGoalsSubPanel()

        createMutableProperties()

        panel.add(goalsSubPanel)
        panel.add(mutableSubPanel)
    }


    private fun createMutableProperties() {
        updateMutableProperties()
    }

    private fun updateMutableProperties() {
        mutableSubPanel!!.removeAll()
        mutableSubPanel!!.layout = BoxLayout(mutableSubPanel, BoxLayout.PAGE_AXIS)

        createAdditionalParametersSubPanel()

        createPropertiesSubPanel()

        createProfilesSubPanel()

        createOptionalJvmOptionsSubPanel()
    }

    private fun createAdditionalParametersSubPanel() {
        additionalParametersSubPanel.removeAll()
        if (mainConfigMode || settingsService.getVisibleSettings().contains(VisibleSetting.ADDITIONAL_PARAMETERS)) {
            additionalParameters = ExpandableTextField()
            additionalParametersLabel = JLabel(message("mavenExecutor.additionalParameters.label"))
            additionalParametersSubPanel = JPanel()

            val groupLayout = GroupLayout(additionalParametersSubPanel)
            groupLayout.autoCreateGaps = false
            groupLayout.autoCreateContainerGaps = false
            additionalParametersSubPanel.layout = groupLayout
            additionalParametersSubPanel.size = Dimension(Integer.MAX_VALUE, 20)
            additionalParameters.minimumSize = Dimension(40, additionalParameters.height)
            additionalParameters.text = settingsService.currentSettings.additionalParameters

            additionalParameters.document.addDocumentListener(object : DocumentAdapter() {
                override fun textChanged(e: javax.swing.event.DocumentEvent) {
                    settingsService.currentSettings.additionalParameters = additionalParameters.text

                    changeListener()
                }
            })

            additionalParametersSubPanel.border = JBUI.Borders.empty(0, 7, 0, 7)
            additionalParametersLabel.border = JBUI.Borders.empty(0, 0, 0, 5)
            groupLayout.setHorizontalGroup(
                    groupLayout.createSequentialGroup()
                            .addComponent(additionalParametersLabel)
                            .addComponent(additionalParameters)
            )
            groupLayout.setVerticalGroup(
                    groupLayout.createSequentialGroup()
                            .addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                    .addComponent(additionalParametersLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, java.lang.Short.MAX_VALUE.toInt())
                                    .addComponent(additionalParameters, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, java.lang.Short.MAX_VALUE.toInt())
                            )
            )

            mutableSubPanel!!.add(additionalParametersSubPanel)
        }
    }

    private fun createProfilesSubPanel() {
        profilesSubPanel.removeAll()
        if (mainConfigMode || settingsService.getVisibleSettings().contains(VisibleSetting.PROFILES)) {
            profilesSubPanel = JPanel()
            profilesLabel = JLabel(message("mavenExecutor.profiles.label"))
            profiles = createProfilesCheckboxCombo()

            val groupLayout = GroupLayout(profilesSubPanel)
            groupLayout.autoCreateGaps = true
            groupLayout.autoCreateContainerGaps = true
            profilesSubPanel.layout = groupLayout
            profilesSubPanel.border = JBUI.Borders.empty(0, 0, 0, 3)

            updateProfile()

            groupLayout.setHorizontalGroup(
                    groupLayout.createSequentialGroup()
                            .addComponent(profilesLabel)
                            .addComponent(profiles)
            )
            groupLayout.setVerticalGroup(
                    groupLayout.createSequentialGroup()
                            .addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                    .addComponent(profilesLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, java.lang.Short.MAX_VALUE.toInt())
                                    .addComponent(profiles, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, java.lang.Short.MAX_VALUE.toInt())
                            )
            )

            mutableSubPanel!!.add(profilesSubPanel)
        }
    }

    private fun createGoalsSubPanel() {
        val label = JLabel(message("mavenExecutor.goals.label"))
        label.toolTipText = message("mavenExecutor.goals.toolTip")

        val groupLayout = GroupLayout(goalsSubPanel)
        groupLayout.autoCreateGaps = false
        groupLayout.autoCreateContainerGaps = false

        goalsSubPanel.layout = groupLayout

        goals.changedListener = { text ->
            settingsService.currentSettings.goalsFromText(text)

            changeListener()
        }

        label.border = JBUI.Borders.empty(0, 0, 0, 5)
        goalsSubPanel.border = JBUI.Borders.empty(0, 7, 0, 7)

        groupLayout.setHorizontalGroup(
                groupLayout.createSequentialGroup()
                        .addComponent(label)
                        .addComponent(goals)
        )
        groupLayout.setVerticalGroup(
                groupLayout.createSequentialGroup()
                        .addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(label, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, java.lang.Short.MAX_VALUE.toInt())
                                .addComponent(goals, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, java.lang.Short.MAX_VALUE.toInt())
                        )
        )
    }

    private fun createPropertiesSubPanel() {
        val visibleSettings = settingsService.getVisibleSettings()

        var rowCount = 2
        if (!mainConfigMode) {
            val oneColPropertiesCount = (if (visibleSettings.contains(VisibleSetting.PROFILES)) 1 else 0) +
                    (if (visibleSettings.contains(VisibleSetting.OPTIONAL_JVM_OPTIONS)) 1 else 0) +
                    (if (visibleSettings.contains(VisibleSetting.ADDITIONAL_PARAMETERS)) 1 else 0) +
                    (if (visibleSettings.contains(VisibleSetting.FAVORITE)) 1 else 0)
            val twoColPropertiesCount = visibleSettings.size - oneColPropertiesCount
            rowCount = ceil(twoColPropertiesCount / 2.0).toInt()
        }

        propertiesSubPanel.removeAll()
        propertiesSubPanel = JPanel()
        propertiesSubPanel.layout = GridLayout(rowCount, 2)
        propertiesSubPanel.border = EmptyBorder(0, 10, 0, 10)

        if (mainConfigMode || visibleSettings.contains(VisibleSetting.ALWAYS_UPDATE_SNAPSHOT)) {
            alwaysUpdateModeCheckBox = JCheckBox(message("mavenExecutor.alwaysUpdateMode.label"))
            alwaysUpdateModeCheckBox.isSelected = settingsService.currentSettings.isAlwaysUpdateSnapshot
            alwaysUpdateModeCheckBox.addActionListener {
                settingsService.currentSettings.isAlwaysUpdateSnapshot = alwaysUpdateModeCheckBox.isSelected

                changeListener()
            }
            val alwaysUpdateModeCheckBoxPanel = JPanel(GridLayout(1, 1))
            alwaysUpdateModeCheckBoxPanel.border = EmptyBorder(3, 0, 3, 0)
            alwaysUpdateModeCheckBoxPanel.add(alwaysUpdateModeCheckBox)
            propertiesSubPanel.add(alwaysUpdateModeCheckBoxPanel)
        }

        if (mainConfigMode || visibleSettings.contains(VisibleSetting.OFFLINE_MODE)) {
            offlineModeCheckBox = JCheckBox(message("mavenExecutor.offline.label"))
            offlineModeCheckBox.isSelected = settingsService.currentSettings.isOfflineMode
            offlineModeCheckBox.toolTipText = message("mavenExecutor.offline.toolTip")
            offlineModeCheckBox.addActionListener {
                settingsService.currentSettings.isOfflineMode = offlineModeCheckBox.isSelected

                changeListener()
            }
            val offlineModeCheckBoxPanel = JPanel(GridLayout(1, 1))
            offlineModeCheckBoxPanel.border = EmptyBorder(3, 0, 3, 0)
            offlineModeCheckBoxPanel.add(offlineModeCheckBox)
            propertiesSubPanel.add(offlineModeCheckBoxPanel)
        }

        if (mainConfigMode || visibleSettings.contains(VisibleSetting.SKIP_TESTS)) {
            skipTestCheckBox = JCheckBox(message("mavenExecutor.skipTest.label"))
            skipTestCheckBox.isSelected = settingsService.currentSettings.isSkipTests
            val skipTestCheckBoxPanel = JPanel(GridLayout(1, 1))
            skipTestCheckBoxPanel.border = EmptyBorder(3, 0, 3, 0)
            skipTestCheckBoxPanel.add(skipTestCheckBox)

            skipTestCheckBox.addActionListener {
                settingsService.currentSettings.isSkipTests = skipTestCheckBox.isSelected

                changeListener()
            }

            propertiesSubPanel.add(skipTestCheckBoxPanel)
        }

        if (mainConfigMode || visibleSettings.contains(VisibleSetting.THREAD_COUNT)) {
            threadsLabel = JLabel(message("mavenExecutor.threads.label"))
            threadsTextField = IntegerField(null, 0, 99)

            val threadPanel = JPanel(GridLayout(1, 2))
            threadPanel.add(threadsLabel)
            threadPanel.add(threadsTextField)
            propertiesSubPanel.add(threadPanel)

            threadsTextField.columns = 2
            threadsTextField.isCanBeEmpty = true
            settingsService.currentSettings.threadCount?.let {
                threadsTextField.value = it
            }
            threadsTextField.addCaretListener {
                try {
                    threadsTextField.validateContent()

                    settingsService.currentSettings.threadCount = threadsTextField.value
                } catch (e: ConfigurationException) {
                    settingsService.currentSettings.threadCount = null
                }
                changeListener()
            }
        }

        if (propertiesSubPanel.componentCount > 0) {
            mutableSubPanel!!.add(propertiesSubPanel)
        }

    }

    private fun createOptionalJvmOptionsSubPanel() {
        optionalJvmOptionsSubPanel.removeAll()
        if (mainConfigMode || settingsService.getVisibleSettings().contains(VisibleSetting.OPTIONAL_JVM_OPTIONS)) {
            optionalJvmOptionsCheckBox = JCheckBox(message("mavenExecutor.optionalJvmOptions.label"))
            optionalJvmOptionsComboBox = ComboBox<String>()
            optionalJvmOptionsSubPanel = JPanel()

            val optionalJvmOptionsLayout = GroupLayout(optionalJvmOptionsSubPanel)
            optionalJvmOptionsLayout.autoCreateGaps = true
            optionalJvmOptionsLayout.autoCreateContainerGaps = true
            optionalJvmOptionsSubPanel.layout = optionalJvmOptionsLayout

            optionalJvmOptionsCheckBox.toolTipText = message("mavenExecutor.optionalJvmOptions.toolTip")
            optionalJvmOptionsCheckBox.isSelected = settingsService.currentSettings.isUseOptionalJvmOptions
            optionalJvmOptionsCheckBox.addActionListener {
                optionalJvmOptionsComboBox.isEnabled = optionalJvmOptionsCheckBox.isSelected
                settingsService.currentSettings.isUseOptionalJvmOptions = optionalJvmOptionsCheckBox.isSelected

                changeListener()
            }

            optionalJvmOptionsComboBox.setHistory(settingsService.jvmOptionHistory)
            optionalJvmOptionsComboBox.initEditor(settingsService.currentSettings.optionalJvmOptionsAsText())
            optionalJvmOptionsComboBox.isEnabled = optionalJvmOptionsCheckBox.isSelected
            optionalJvmOptionsComboBox.setMinimumAndPreferredWidth(70)

            val optionalJvmOptionsEditor = optionalJvmOptionsComboBox.editor.editorComponent as EditorTextField

            optionalJvmOptionsEditor.addDocumentListener(object : DocumentListener {
                override fun documentChanged(event: DocumentEvent) {
                    settingsService.currentSettings.optionalJvmOptions = Lists.newArrayList(*optionalJvmOptionsComboBox.editor.item.toString().split("\\s".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())

                    changeListener()
                }
            })

            optionalJvmOptionsEditor.addFocusListener(object : FocusAdapter() {
                override fun focusLost(e: FocusEvent?) {
                    val current = settingsService.currentSettings.optionalJvmOptionsAsText()
                    settingsService.jvmOptionHistory.add(current)

                    // refresh comboBox values
                    optionalJvmOptionsComboBox.setHistory(settingsService.jvmOptionHistory)
                    optionalJvmOptionsComboBox.model.selectedItem = current
                }
            })

            optionalJvmOptionsLayout.setHorizontalGroup(
                    optionalJvmOptionsLayout.createSequentialGroup()
                            .addComponent(optionalJvmOptionsCheckBox)
                            .addComponent(optionalJvmOptionsComboBox)
            )
            optionalJvmOptionsLayout.setVerticalGroup(
                    optionalJvmOptionsLayout.createSequentialGroup()
                            .addGroup(optionalJvmOptionsLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                    .addComponent(optionalJvmOptionsCheckBox, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Int.MAX_VALUE)
                                    .addComponent(optionalJvmOptionsComboBox, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Int.MAX_VALUE)
                            )
            )

            val optionalJvmOptionsBagPanel = JPanel(GridLayout(1, 1))
            optionalJvmOptionsBagPanel.add(optionalJvmOptionsSubPanel)
            mutableSubPanel!!.add(optionalJvmOptionsBagPanel)
        }
    }

    fun update() {
        updateMutableProperties()

        updateAlwaysUpdateMode()
        updateGoals()
        updateOfflineOption()
        updateOptionalJvmOptions()
        updateProfile()
        updateThreads()
        updateSkipTestsOption()
        updateAdditionalParameters()
    }

    private fun updateThreads() {
        val threadCount = settingsService.currentSettings.threadCount

        threadsTextField.value = threadCount ?: 0
    }

    private fun updateAlwaysUpdateMode() {
        alwaysUpdateModeCheckBox.isSelected = settingsService.currentSettings.isAlwaysUpdateSnapshot
    }

    private fun updateSkipTestsOption() {
        skipTestCheckBox.isSelected = settingsService.currentSettings.isSkipTests
    }

    private fun updateOfflineOption() {
        offlineModeCheckBox.isSelected = settingsService.currentSettings.isOfflineMode
    }

    private fun updateGoals() {
        goals.setText(settingsService.currentSettings.goalsAsText())
    }

    private fun updateAdditionalParameters() {
        additionalParameters.text = settingsService.currentSettings.additionalParameters
    }

    private fun updateOptionalJvmOptions() {
        optionalJvmOptionsCheckBox.isSelected = settingsService.currentSettings.isUseOptionalJvmOptions
        optionalJvmOptionsComboBox.isEnabled = settingsService.currentSettings.isUseOptionalJvmOptions
        optionalJvmOptionsComboBox.editor.item = settingsService.currentSettings.optionalJvmOptionsAsText()
    }

    fun updateProfile() {
        profiles.setItems(projectsHelper.manager.availableProfiles.map {
            CheckBoxItem(it, settingsService.currentSettings.profiles.contains(it))
        })
    }

}

package org.kubicz.mavenexecutor.view.window.panels

import com.intellij.ide.ui.LafManagerListener
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader
import com.intellij.util.ui.JBUI
import org.kubicz.mavenexecutor.model.settings.VisibleSetting
import org.kubicz.mavenexecutor.view.MavenExecutorBundle.Companion.message
import org.kubicz.mavenexecutor.view.components.CustomButton
import org.kubicz.mavenexecutor.view.window.ExecutionSettingsService
import java.awt.Dimension
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*

class FavoritePanel(project: Project, private val settingsService: ExecutionSettingsService, changeSettingListener: () -> Unit) {

    private var panel = JPanel()

    private var changeModeButton = CustomButton("")

    private var defaultSettingsButton = CustomButton(message(fullOrSmall("mavenExecutor.default.label", "mavenExecutor.default.small.label")))

    private val isDefault: JButton.() -> Boolean = { name == "default" }


    private val init: CustomButton.(Boolean) -> Unit = {selected ->
        overrideBackground(selected)

        maximumSize = Dimension(Integer.MAX_VALUE, maximumSize.getHeight().toInt())

        addActionListener{
            val button = (it.source as JButton)

            if(button.isDefault()) {
                settingsService.loadDefaultSettings()
            }
            else {
                settingsService.loadSettings(button.name)
            }

            refreshSelection()

            changeSettingListener()

        }
    }

    val component
        get() : JComponent = panel

    init {
        ApplicationManager.getApplication().messageBus.connect()
            .subscribe(LafManagerListener.TOPIC, LafManagerListener { refresh() })

        if (settingsService.getVisibleSettings().contains(VisibleSetting.FAVORITE)) {
            initComponents()
        }
        else {
            panel.preferredSize = Dimension(1, panel.preferredSize.getHeight().toInt())
            panel.maximumSize = Dimension(1, panel.maximumSize.getHeight().toInt())
        }
    }

    fun refresh() {
        panel.removeAll()
        if (settingsService.getVisibleSettings().contains(VisibleSetting.FAVORITE)) {
            defaultSettingsButton =
                CustomButton(message(fullOrSmall("mavenExecutor.default.label", "mavenExecutor.default.small.label")))
            changeModeButton = CustomButton("")

            initComponents()

        }
        else {
            panel.preferredSize = Dimension(1, panel.preferredSize.getHeight().toInt())
            panel.maximumSize = Dimension(1, panel.maximumSize.getHeight().toInt())
            panel.border = JBUI.Borders.customLine(UIManager.getColor("Component.borderColor"), 0, 0, 0, 0)
        }
        panel.updateUI()
        panel.repaint()
    }

    private fun fullOrSmall(fullOption: String, smallOption: String): String {
        return if (settingsService.isFavoritePanelFullMode) fullOption else smallOption
    }

    private fun initComponents() {
        panel.layout = BoxLayout(panel, BoxLayout.PAGE_AXIS)
        panel.border = JBUI.Borders.customLine(UIManager.getColor("Component.borderColor"), 0, 1, 0, 0)

        if(settingsService.isFavoritePanelFullMode) {
            panel.preferredSize = Dimension(100, panel.preferredSize.getHeight().toInt())
            panel.maximumSize = Dimension(100, panel.maximumSize.getHeight().toInt())
        }
        else  {
            panel.preferredSize = Dimension(30, panel.preferredSize.getHeight().toInt())
            panel.maximumSize = Dimension(30, panel.maximumSize.getHeight().toInt())
        }

        changeModeButton.icon = IconLoader.getIcon(fullOrSmall("/icons/allRight.png", "/icons/allLeft.png"), this.javaClass)
        changeModeButton.maximumSize = Dimension(30, changeModeButton.maximumSize.getHeight().toInt())

        changeModeButton.addActionListener {
            settingsService.isFavoritePanelFullMode = !settingsService.isFavoritePanelFullMode

            refresh()
        }

        panel.add(changeModeButton)

        val separator = JSeparator()
        separator.preferredSize = Dimension(separator.preferredSize.getWidth().toInt(), 10)
        separator.maximumSize = Dimension(separator.maximumSize.getWidth().toInt(), 10)
        separator.minimumSize = Dimension(separator.minimumSize.getWidth().toInt(), 10)
        panel.add(separator)

        val currentSettingsLabel = settingsService.currentSettingsLabel

        val isDefaultSettingsSelected = settingsService.isDefaultSettings

        defaultSettingsButton.name = "default"
        defaultSettingsButton.init(isDefaultSettingsSelected)

        panel.add(defaultSettingsButton)

        val favoriteLabel = JLabel(message(fullOrSmall("mavenExecutor.favorite.label", "mavenExecutor.favorite.small.label")), null, SwingConstants.CENTER)
        favoriteLabel.maximumSize = Dimension(Integer.MAX_VALUE, favoriteLabel.maximumSize.getHeight().toInt())

        panel.add(favoriteLabel)

        settingsService.favoriteSettingsNames.forEach { settingName ->
            val button = CustomButton(fullOrSmall(settingName, settingName[0].toString()))
            button.name = settingName

            button.init(settingName == currentSettingsLabel)

            button.addMouseListener(object : MouseAdapter() {
                override fun mousePressed(e: MouseEvent?) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        showMenu(e?.source as JButton)
                    }
                }
            })

            panel.add(button)
        }

    }

    private fun refreshSelection() {
        panel.components.forEach {
            if (it is CustomButton) {
                if(it.isDefault()) {
                    it.overrideBackground(settingsService.isDefaultSettings)
                }
                else {
                    it.overrideBackground(settingsService.currentSettingsLabel == it.name)
                }
            }
        }
    }

    private fun showMenu(button: JButton) {
        val actionManager = ActionManager.getInstance()

        val menu = actionManager.createActionPopupMenu("MavenExecutorPanel", actionManager.getAction("MavenExecutor.FavoriteItemContextMenu") as DefaultActionGroup)
        menu.component.show(button, JBUI.scale(-10), button.height + JBUI.scale(2))
    }

}
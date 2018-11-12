package org.kubicz.mavenexecutor.view.window.panels

import com.intellij.ide.ui.LafManager
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.util.ui.JBUI
import org.kubicz.mavenexecutor.view.components.CustomButton
import org.kubicz.mavenexecutor.view.window.ExecutionSettingsService
import org.kubicz.mavenexecutor.view.MavenExecutorBundle.Companion.message
import java.awt.Dimension
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*

class FavoritePanel(settingsService: ExecutionSettingsService, changeSettingListener: () -> Unit) {

    private var panel = JPanel()

    private val settingsService = settingsService;

    private var defaultSettingsButton = CustomButton(message("mavenExecutor.default.label"))

    private val isDefault: JButton.() -> Boolean = {name == "default"}

    private val init: CustomButton.(Boolean) -> Unit = {selected ->
        overrideBackground(selected)

        maximumSize = Dimension(Integer.MAX_VALUE, maximumSize.getHeight().toInt())

        addActionListener{
            val button = (it.source as JButton)

            if(button.isDefault()) {
                settingsService.loadDefaultSettings()
            }
            else {
                settingsService.loadSettings(button.text)
            }

            refreshSelection()

            changeSettingListener()

        }
    }

    val component
        get() : JComponent = panel

    init {
        LafManager.getInstance().addLafManagerListener {
            refresh()
        }

        initComponents()
    }

    fun refresh() {
        panel.removeAll()
        defaultSettingsButton = CustomButton(message("mavenExecutor.default.label"))

        initComponents()

        panel.updateUI()
        panel.repaint()
    }

    private fun initComponents() {
        panel.layout = BoxLayout(panel, BoxLayout.PAGE_AXIS)

        val currentSettingsLabel = settingsService.currentSettingsLabel

        val isDefaultSettingsSelected = settingsService.isDefaultSettings

        defaultSettingsButton.name = "default"
        defaultSettingsButton.init(isDefaultSettingsSelected)

        panel.add(defaultSettingsButton)

        val favoriteLabel = JLabel(message("mavenExecutor.favorite.label"), null, SwingConstants.CENTER)
        favoriteLabel.maximumSize = Dimension(Integer.MAX_VALUE, favoriteLabel.maximumSize.getHeight().toInt())
        panel.add(favoriteLabel)

        settingsService.favoriteSettingsNames.forEach { settingName ->
            val button = CustomButton(settingName)

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
                    it.overrideBackground(settingsService.currentSettingsLabel == it.text)
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
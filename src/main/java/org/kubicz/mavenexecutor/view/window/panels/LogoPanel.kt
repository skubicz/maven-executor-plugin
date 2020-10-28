package org.kubicz.mavenexecutor.view.window.panels

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader
import com.intellij.util.ui.JBUI
import org.kubicz.mavenexecutor.model.settings.VisibleSetting
import org.kubicz.mavenexecutor.view.window.ExecutionSettingsService
import java.awt.Dimension
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.UIManager
import javax.swing.border.Border

class LogoPanel(project: Project, private val settingsService: ExecutionSettingsService) {

    private var panel = object: JPanel() {
        override fun updateUI() {
            super.updateUI()

            if (settingsService.getVisibleSettings().contains(VisibleSetting.FAVORITE)) {
                border = JBUI.Borders.customLine(UIManager.getColor("Component.borderColor"), 0, 1, 0, 0)
            }
            else {
                border = JBUI.Borders.empty()
            }
        }

    }

    val component
        get() : JComponent = panel

    init {
        refresh()
    }

    fun refresh() {
        panel.removeAll()
        if (settingsService.getVisibleSettings().contains(VisibleSetting.FAVORITE)) {
            val thumb = JLabel()
            panel.border = JBUI.Borders.customLine(UIManager.getColor("Component.borderColor"), 0, 1, 0, 0)
            thumb.icon = IconLoader.getIcon("/icons/executorLogo.svg", this.javaClass)
            panel.add(thumb)
        }
        else {
            panel.maximumSize = Dimension(1, panel.height)
            panel.preferredSize = Dimension(1, panel.height)
            panel.border = JBUI.Borders.empty()
        }
        panel.repaint()
        panel.updateUI()
    }

}
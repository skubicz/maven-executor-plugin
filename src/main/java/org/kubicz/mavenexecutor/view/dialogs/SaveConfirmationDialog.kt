package myToolWindow

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import org.kubicz.mavenexecutor.view.MavenExecutorBundle
import org.kubicz.mavenexecutor.view.MavenExecutorBundle.Companion.message
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextField

class SaveConfirmationDialog(project: Project?) : DialogWrapper(project) {

    private var contentPane: JPanel? = null

    private var settingsName: JTextField? = null

    init {
        isModal = true
        title = message("mavenExecutor.saveSettings.dialog.title")

        init()
    }

    override fun createCenterPanel(): JComponent? {
        return contentPane
    }

    fun getSettingsName(): String {
        return settingsName!!.text
    }

    fun setSettingsName(settingsName: String?) {
        this.settingsName!!.text = settingsName
    }

    override fun getPreferredFocusedComponent(): JComponent? {
        return settingsName
    }
}

package myToolWindow

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.DocumentAdapter
import org.kubicz.mavenexecutor.view.MavenExecutorBundle.Companion.message
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.event.DocumentEvent

class SaveConfirmationDialog(project: Project?) : DialogWrapper(project) {

    private var contentPane: JPanel? = null

    private var settingsName: JTextField? = null

    init {
        isModal = true
        title = message("mavenExecutor.saveSettings.dialog.title")

        init()

        canSave()

        settingsName?.document?.addDocumentListener(object : DocumentAdapter() {
            override fun textChanged(e: DocumentEvent) {
                canSave()
            }
        })
    }

    private fun canSave() {
        settingsName?.let {
            getButton(myOKAction)?.isEnabled = it.text.isNotBlank()
        }
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

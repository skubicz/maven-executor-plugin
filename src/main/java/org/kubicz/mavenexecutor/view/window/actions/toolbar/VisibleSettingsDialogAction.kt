package org.kubicz.mavenexecutor.view.window.actions.toolbar

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.awt.RelativePoint
import org.kubicz.mavenexecutor.model.settings.VisibleSetting
import org.kubicz.mavenexecutor.model.settings.VisibleSetting.*
import org.kubicz.mavenexecutor.view.MavenExecutorBundle.Companion.message
import org.kubicz.mavenexecutor.view.components.CustomCheckBoxList
import org.kubicz.mavenexecutor.view.window.ExecutionSettingsService
import org.kubicz.mavenexecutor.view.window.MavenExecutorToolWindow
import java.awt.Point
import javax.swing.JPanel


class VisibleSettingsDialogAction : AnAction("") {

    private var checkBoxList: CustomCheckBoxList? = null

    init {
    }

    override fun actionPerformed(event: AnActionEvent) {
        val settingsService = ExecutionSettingsService.getInstance(event.project!!)
        val visibleSettings = settingsService.getVisibleSettings()

        checkBoxList = CustomCheckBoxList()
        checkBoxList?.let {
            it.addItemWithValue(ADDITIONAL_PARAMETERS.name, message("mavenExecutor.additionalParameters.label"), ADDITIONAL_PARAMETERS.name, visibleSettings.contains(ADDITIONAL_PARAMETERS))
            it.addItemWithValue(THREAD_COUNT.name, message("mavenExecutor.threads.label"), THREAD_COUNT.name, visibleSettings.contains(THREAD_COUNT))
            it.addItemWithValue(OFFLINE_MODE.name, message("mavenExecutor.offline.label"), OFFLINE_MODE.name, visibleSettings.contains(OFFLINE_MODE))
            it.addItemWithValue(SKIP_TESTS.name, message("mavenExecutor.skipTest.label"), SKIP_TESTS.name, visibleSettings.contains(SKIP_TESTS))
            it.addItemWithValue(ALWAYS_UPDATE_SNAPSHOT.name, message("mavenExecutor.alwaysUpdateMode.label"), ALWAYS_UPDATE_SNAPSHOT.name, visibleSettings.contains(ALWAYS_UPDATE_SNAPSHOT))
            it.addItemWithValue(OPTIONAL_JVM_OPTIONS.name, message("mavenExecutor.optionalJvmOptions.label"), OPTIONAL_JVM_OPTIONS.name, visibleSettings.contains(OPTIONAL_JVM_OPTIONS))
            it.addItemWithValue(PROFILES.name, message("mavenExecutor.profiles.label"), PROFILES.name, visibleSettings.contains(PROFILES))
            it.addItemWithValue(FAVORITE.name, message("mavenExecutor.favorite.name"), FAVORITE.name, visibleSettings.contains(FAVORITE))
        }

        checkBoxList!!.setCheckBoxListListener { _, _ ->
            settingsService.clearVisibleSettings()
            settingsService.addVisibleSettings(checkBoxList!!.selectedItemNames.map(VisibleSetting::valueOf))

            val window = MavenExecutorToolWindow.getInstance(event.project!!)
            window.updateAll()
        }

        createExpandedPopup().show(RelativePoint(event.inputEvent.component, Point(0, event.inputEvent.component.size.height)))
    }

    private fun createExpandedPopup(): JBPopup {
        val panel = JPanel()

        panel.add(checkBoxList)
        panel.background = checkBoxList!!.background

        return JBPopupFactory.getInstance()
                .createComponentPopupBuilder(panel, null)
                .setMayBeParent(true)
                .setFocusable(true)
                .setResizable(true)
                .setRequestFocus(true)
                .setLocateByContent(true)
                .setShowBorder(true)
                .setCancelOnWindowDeactivation(false)
                .setCancelCallback {
                    true
                }.createPopup()

    }

}
package org.kubicz.mavenexecutor.view.window

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.Property
import org.kubicz.mavenexecutor.model.settings.History
import org.kubicz.mavenexecutor.model.settings.ExecutionSettings
import java.util.*

@State(name = "mavenExecutorSetting", storages = [Storage("mavenExecutorSetting.xml")])
class ExecutionSettingsService : PersistentStateComponent<ExecutionSettingsService> {

    @Property
    var defaultSettings = ExecutionSettings()

    @Property
    var currentSettingsLabel: String? = null

    @Property
    var goalsHistory: History = History()

    @Property
    var jvmOptionHistory: History = History()

    @Property
    private var favorite = HashMap<String, ExecutionSettings>()

    val favoriteSettings: List<ExecutionSettings>
        get() = favorite.values.toList()

    val favoriteSettingsNames: List<String>
        get() = favorite.keys.toList()

    val currentSettings: ExecutionSettings
        get() {
            return favorite.getOrDefault(currentSettingsLabel, defaultSettings)
        }

    val isDefaultSettings: Boolean
        get() = currentSettingsLabel == null


    init {
    }

    override fun getState(): ExecutionSettingsService? {
        return this
    }

    override fun loadState(state: ExecutionSettingsService) {
        XmlSerializerUtil.copyBean(state, this)
    }

    fun addSettings(settingsName: String, setting: ExecutionSettings) {
        favorite[settingsName] = setting
    }

    fun loadSettings(settingsName: String) {
        currentSettingsLabel = settingsName
    }

    fun loadDefaultSettings() {
        currentSettingsLabel = null
    }

    fun removeFavoriteSettings(settingsName: String) {
        favorite.remove(settingsName)
    }

    companion object {
        fun getInstance(project: Project): ExecutionSettingsService {
            return ServiceManager.getService(project, ExecutionSettingsService::class.java)
        }
    }

}

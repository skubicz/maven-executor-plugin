package org.kubicz.mavenexecutor.view.window

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.Property
import org.jetbrains.idea.maven.project.MavenProject
import org.jetbrains.idea.maven.project.MavenProjectsManager
import org.kubicz.mavenexecutor.model.MavenArtifact
import org.kubicz.mavenexecutor.model.settings.ExecutionSettings
import org.kubicz.mavenexecutor.model.settings.History
import org.kubicz.mavenexecutor.model.settings.VisibleSetting
import org.kubicz.mavenexecutor.model.tree.ProjectRootNode
import org.kubicz.mavenexecutor.view.MavenProjectsHelper
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
    var isFavoritePanelFullMode: Boolean = true

    @Property
    private var favorite = HashMap<String, ExecutionSettings>()

    @Property
    private var visibleSettings: MutableSet<VisibleSetting> = HashSet()

    @Property
    var visibleSettingsNotInitYet = true

    @Property
    var resultCommandLineMode: MavenResultCommandLineMode = MavenResultCommandLineMode.SIMPLE

    @Property
    var resultCommandLineChosenProject: MavenArtifact = MavenProjectsHelper.EMPTY_ARTIFACT

    fun clearVisibleSettings() {
        visibleSettings.clear()
    }

    fun addVisibleSettings(settings: List<VisibleSetting>) {
        visibleSettings.clear()
        visibleSettings.addAll(settings)
    }

    fun getVisibleSettings(): Set<VisibleSetting> {
        if (visibleSettingsNotInitYet) {
            visibleSettings.add(VisibleSetting.OFFLINE_MODE)
            visibleSettings.add(VisibleSetting.THREAD_COUNT)
            visibleSettings.add(VisibleSetting.OPTIONAL_JVM_OPTIONS)
            visibleSettings.add(VisibleSetting.PROFILES)
            visibleSettings.add(VisibleSetting.SKIP_TESTS)
            visibleSettings.add(VisibleSetting.ALWAYS_UPDATE_SNAPSHOT)
            visibleSettings.add(VisibleSetting.FAVORITE)

            visibleSettingsNotInitYet = false
        }
        return visibleSettings
    }

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

    fun currentRootProjects(manager: MavenProjectsManager): List<MavenProject> {
        val rootProjects = manager.rootProjects
        if (currentSettings.selectedProject == MavenProjectsHelper.EMPTY_ARTIFACT) {
            return rootProjects
        }

        return rootProjects.filter {
            currentSettings
                    .selectedProject.equalsGroupAndArtifactId(MavenArtifact(it.mavenId.groupId!!, it.mavenId.artifactId!!, ""))
        }
    }

    fun currentRootProjectsAsMavenize(manager: MavenProjectsManager): List<ProjectRootNode> {
        return currentRootProjects(manager).map { ProjectRootNode(it.displayName, MavenArtifact(it.mavenId.groupId!!, it.mavenId.artifactId!!, it.mavenId.version!!), it.directoryFile) }
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

        if (currentSettingsLabel == settingsName) {
            loadDefaultSettings()
        }
    }

    companion object {
        fun getInstance(project: Project): ExecutionSettingsService {
            return ServiceManager.getService(project, ExecutionSettingsService::class.java)
        }
    }

}

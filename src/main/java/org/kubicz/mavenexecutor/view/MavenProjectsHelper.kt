package org.kubicz.mavenexecutor.view

import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import org.jetbrains.idea.maven.project.MavenProject
import org.jetbrains.idea.maven.project.MavenProjectsManager
import org.kubicz.mavenexecutor.model.MavenArtifact
import org.kubicz.mavenexecutor.model.tree.ProjectRootNode
import org.kubicz.mavenexecutor.view.window.ExecutionSettingsService

class MavenProjectsHelper(project: Project) {

    private var settingsService = ExecutionSettingsService.getInstance(project)
    val manager = MavenProjectsManager.getInstance(project)!!

    fun currentRootProjects(): List<MavenProject> {
        val rootProjects = manager.rootProjects
        if (settingsService.currentSettings.selectedProject == EMPTY_ARTIFACT) {
            return rootProjects
        }

        return rootProjects.filter {
            settingsService.currentSettings
                    .selectedProject.equalsGroupAndArtifactId(MavenArtifact(it.mavenId.groupId!!, it.mavenId.artifactId!!, ""))
        }
    }

    fun currentRootProjectsAsMavenize(): List<ProjectRootNode> {
        return currentRootProjects().map { ProjectRootNode(it.displayName, MavenArtifact(it.mavenId.groupId!!, it.mavenId.artifactId!!, it.mavenId.version!!), it.directoryFile) }
    }

    companion object {
        val EMPTY_ARTIFACT = MavenArtifact("", "", "")

        fun getInstance(project: Project): MavenProjectsHelper {
            return ServiceManager.getService(project, MavenProjectsHelper::class.java)
        }
    }

}
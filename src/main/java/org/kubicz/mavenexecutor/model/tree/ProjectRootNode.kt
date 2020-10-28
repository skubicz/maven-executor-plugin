package org.kubicz.mavenexecutor.model.tree

import com.intellij.openapi.vfs.VirtualFile
import org.kubicz.mavenexecutor.model.MavenArtifact
import org.kubicz.mavenexecutor.model.Mavenize

class ProjectRootNode constructor(override val displayName: String,
                                          override val mavenArtifact: MavenArtifact,
                                          val projectDirectory: VirtualFile) : Mavenize {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ProjectRootNode

        if (displayName != other.displayName) return false
        if (mavenArtifact != other.mavenArtifact) return false
        if (projectDirectory != other.projectDirectory) return false

        return true
    }

    override fun hashCode(): Int {
        var result = displayName.hashCode()
        result = 31 * result + mavenArtifact.hashCode()
        result = 31 * result + projectDirectory.hashCode()
        return result
    }

    companion object {

        fun of(displayName: String, mavenArtifact: MavenArtifact, projectDirectory: VirtualFile): ProjectRootNode {
            return ProjectRootNode(displayName, mavenArtifact, projectDirectory)
        }
    }
}
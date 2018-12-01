package org.kubicz.mavenexecutor.model.tree

import org.kubicz.mavenexecutor.model.MavenArtifact

class ProjectModuleNode(override val displayName: String, override val mavenArtifact: MavenArtifact) : Mavenize {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ProjectModuleNode

        if (displayName != other.displayName) return false
        if (mavenArtifact != other.mavenArtifact) return false

        return true
    }

    override fun hashCode(): Int {
        var result = displayName.hashCode()
        result = 31 * result + mavenArtifact.hashCode()
        return result
    }
}
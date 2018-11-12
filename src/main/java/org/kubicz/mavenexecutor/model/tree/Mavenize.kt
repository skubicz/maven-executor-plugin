package org.kubicz.mavenexecutor.model.tree

import org.kubicz.mavenexecutor.model.MavenArtifact

interface Mavenize {

    val displayName: String

    val mavenArtifact: MavenArtifact

    fun equalsGroupAndArtifactId(mavenize: Mavenize?): Boolean {
        return if (mavenize == null) false else mavenArtifact.artifactId == mavenize.mavenArtifact.artifactId && mavenArtifact.groupId == mavenize.mavenArtifact.groupId
    }

}
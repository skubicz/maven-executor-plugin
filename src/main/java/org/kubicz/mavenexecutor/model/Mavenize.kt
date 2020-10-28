package org.kubicz.mavenexecutor.model

import org.kubicz.mavenexecutor.model.MavenArtifact

interface Mavenize {

    val displayName: String

    val mavenArtifact: MavenArtifact

}
package org.kubicz.mavenexecutor.view.components

import org.jetbrains.annotations.PropertyKey
import org.kubicz.mavenexecutor.model.MavenArtifact
import org.kubicz.mavenexecutor.model.Mavenize
import org.kubicz.mavenexecutor.view.MavenExecutorBundle

class MavenizeComboBoxItem(override val displayName: String, override var mavenArtifact: MavenArtifact): Mavenize {

    override fun toString(): String {
        return displayName
    }

}
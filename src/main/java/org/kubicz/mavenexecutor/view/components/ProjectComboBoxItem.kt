package org.kubicz.mavenexecutor.view.components

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.xmlb.annotations.Property
import org.kubicz.mavenexecutor.model.MavenArtifact
import org.kubicz.mavenexecutor.model.settings.ProjectToBuild

class ProjectComboBoxItem(val displayName: String, val mavenArtifact: MavenArtifact, val projectDirectory: VirtualFile) {

    override fun toString(): String {
        return displayName
    }

}
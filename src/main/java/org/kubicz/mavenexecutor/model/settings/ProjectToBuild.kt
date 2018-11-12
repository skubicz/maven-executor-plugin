package org.kubicz.mavenexecutor.model.settings

import com.intellij.util.xmlb.annotations.Property
import org.kubicz.mavenexecutor.model.MavenArtifact

class ProjectToBuild {

    @Property
    var displayName: String = ""

    @Property
    var mavenArtifact: MavenArtifact = MavenArtifact("", "", "")

    @Property
    var projectDictionary: String = ""

    @Property
    var selectedModules: MutableList<MavenArtifact> = ArrayList()

    private constructor()

    constructor(displayName: String, mavenArtifact: MavenArtifact, projectDictionary: String, selectedModules: MutableList<MavenArtifact>) {
        this.displayName = displayName
        this.mavenArtifact = mavenArtifact
        this.projectDictionary = projectDictionary
        this.selectedModules = selectedModules
    }

    constructor(displayName: String, mavenArtifact: MavenArtifact, projectDictionary: String) {
        this.displayName = displayName
        this.mavenArtifact = mavenArtifact
        this.projectDictionary = projectDictionary
        this.selectedModules = ArrayList()
    }

    fun buildEntireProject(): Boolean {
        return selectedModules.isEmpty()
    }

    fun selectedModulesAsText(): String {
        return selectedModules.map { it.groupIdAndArtifactIdAsText() }.joinToString(",")
    }

}
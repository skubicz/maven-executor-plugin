package org.kubicz.mavenexecutor.model.settings

import org.kubicz.mavenexecutor.model.MavenArtifact
import java.util.ArrayList

class ProjectToBuildBuilder {

    var displayName: String? = null

    var mavenArtifact: MavenArtifact? = null

    var projectDictionary: String? = null

    var selectedModules: MutableList<MavenArtifact> = ArrayList()

    constructor(displayName: String, mavenArtifact: MavenArtifact, projectDictionary: String) {
        this.displayName = displayName
        this.mavenArtifact = mavenArtifact
        this.projectDictionary = projectDictionary
        this.selectedModules = ArrayList()
    }

    fun displayName(displayName: String): ProjectToBuildBuilder {
        this.displayName = displayName
        return this
    }

    fun mavenArtifact(mavenArtifact: MavenArtifact): ProjectToBuildBuilder {
        this.mavenArtifact = mavenArtifact
        return this
    }

    fun projectDictionary(projectDictionary: String): ProjectToBuildBuilder {
        this.projectDictionary = projectDictionary
        return this
    }

    fun selectedModules(selectedModules: MutableList<MavenArtifact>): ProjectToBuildBuilder {
        this.selectedModules = selectedModules
        return this
    }

    fun addArtifact(mavenArtifact: MavenArtifact): ProjectToBuildBuilder {
        this.selectedModules.add(mavenArtifact)
        return this
    }

    fun build(): ProjectToBuild {
        return ProjectToBuild(displayName!!, mavenArtifact!!, projectDictionary!!, selectedModules)
    }

    override fun toString(): String {
        return "ProjectToBuild.ProjectToBuildBuilder(displayName=" + this.displayName + ", mavenArtifact=" + this.mavenArtifact + ", projectDictionary=" + this.projectDictionary + ", selectedModules=" + this.selectedModules + ")"
    }
}
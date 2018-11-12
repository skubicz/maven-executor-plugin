package org.kubicz.mavenexecutor.model

import com.intellij.util.xmlb.annotations.Property

class MavenArtifact {

    @Property
    var groupId = ""

    @Property
    var artifactId = ""

    @Property
    var version = ""

    private constructor()

    constructor(groupId: String, artifactId: String, version: String) {
        this.groupId = groupId
        this.artifactId = artifactId
        this.version = version
    }

    fun equalsGroupAndArtifactId(mavenize: MavenArtifact?): Boolean {
        return if (mavenize == null) false else artifactId == mavenize.artifactId && groupId == mavenize.groupId
    }

    fun groupIdAndArtifactIdAsText(): String {
        return "$groupId:$artifactId"
    }

}
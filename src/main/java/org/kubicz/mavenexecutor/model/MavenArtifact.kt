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

    fun getGroupAndArtifactKey() : MavenGroupAndArtifactKey {
        return MavenGroupAndArtifactKey(groupId, artifactId)
    }

    fun equalsGroupAndArtifactId(mavenize: MavenArtifact?): Boolean {
        return if (mavenize == null) false else artifactId == mavenize.artifactId && groupId == mavenize.groupId
    }

    fun groupIdAndArtifactIdAsText(): String {
        return "$groupId:$artifactId"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MavenArtifact

        if (groupId != other.groupId) return false
        if (artifactId != other.artifactId) return false
        if (version != other.version) return false

        return true
    }

    override fun hashCode(): Int {
        var result = groupId!!.hashCode()
        result = 31 * result + artifactId!!.hashCode()
        result = 31 * result + version!!.hashCode()
        return result
    }

}
package org.kubicz.mavenexecutor.model

import com.intellij.openapi.util.text.StringUtil
import com.intellij.util.xmlb.annotations.Property


class MavenGroupAndArtifactKey(groupId: String?, artifactId: String?): Comparable<MavenGroupAndArtifactKey> {
    @Property
    var groupId: String? = groupId

    @Property
    var artifactId: String? = artifactId

    constructor() : this(null, null) {
    }

    override fun compareTo(other: MavenGroupAndArtifactKey): Int {
        return toString().compareTo(other.toString());
    }

    override fun toString(): String {
        return StringUtil.notNullize(groupId) + ":" + StringUtil.notNullize(artifactId)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MavenGroupAndArtifactKey

        if (groupId != other.groupId) return false
        if (artifactId != other.artifactId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = groupId!!.hashCode()
        result = 31 * result + artifactId!!.hashCode()
        return result
    }

}
package org.kubicz.mavenexecutor.model.settings

import com.google.common.collect.Lists
import com.intellij.util.xmlb.annotations.Property
import org.kubicz.mavenexecutor.model.MavenArtifact
import org.kubicz.mavenexecutor.model.MavenGroupAndArtifactKey
import org.kubicz.mavenexecutor.view.MavenProjectsHelper
import java.util.HashSet


class ExecutionSettings {

    @Property
    var goals: MutableList<String> = ArrayList()

    @Property
    var profiles: MutableList<String> = ArrayList()

    @Property
    var jvmOptions: MutableList<String> = ArrayList()

    @Property
    var optionalJvmOptions: MutableList<String> = ArrayList()

    @Property
    var threadCount: Int? = null

    @Property
    var environmentProperties: MutableMap<String, String> = HashMap()

    @Property
    var isUseOptionalJvmOptions = false

    @Property
    var isOfflineMode = false

    @Property
    var isAlwaysUpdateSnapshot = false

    @Property
    var isSkipTests = false

    @Property
    var additionalParameters: String = ""

    @Property
    var projectsToBuild: MutableList<ProjectToBuild> = ArrayList()

    @Property
    var selectedProject = MavenProjectsHelper.EMPTY_ARTIFACT

    @Property
    var collapseModules: MutableSet<MavenGroupAndArtifactKey> = HashSet()

    @Property
    var alwaysBuildPomModules = true

    fun goalsAsText(): String {
        return goals.joinToString(" ")
    }

    fun optionalJvmOptionsAsText(): String {
        return optionalJvmOptions.joinToString(" ")
    }

    fun allJvmOptionsAsText(): String {
        var jvmOptions = jvmOptionsAsText()
        if (isUseOptionalJvmOptions && optionalJvmOptions.isNotEmpty()) {
            jvmOptions = jvmOptions + " " + optionalJvmOptionsAsText()
        }
        return jvmOptions
    }

    fun jvmOptionsAsText(): String {
        return jvmOptions.joinToString(" ")
    }

    fun goalsFromText(goalsText: String) {
        if (goalsText.isEmpty()) {
            goals.clear()
        } else {
            goals = Lists.newArrayList(*goalsText.trim().split("\\s".toRegex()).filter { it.isNotEmpty() }.toTypedArray())
        }
    }

}
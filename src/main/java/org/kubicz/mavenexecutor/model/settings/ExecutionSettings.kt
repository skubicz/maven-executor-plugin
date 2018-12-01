package org.kubicz.mavenexecutor.model.settings

import com.google.common.collect.Lists
import com.intellij.util.xmlb.annotations.Property
import java.io.Serializable


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
    var projectsToBuild: MutableList<ProjectToBuild> = ArrayList()

    fun goalsAsText(): String {
        return goals.joinToString(" ")
    }

    fun optionalJvmOptionsAsText(): String {
        return optionalJvmOptions.joinToString(" ")
    }

    fun jvmOptionsAsText(): String {
        return jvmOptions.joinToString(" ")
    }

    fun goalsFromText(goalsText: String) {
        if (goalsText.isEmpty()) {
            goals.clear()
        } else {
            goals = Lists.newArrayList(*goalsText.split("\\s".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
        }
    }

}
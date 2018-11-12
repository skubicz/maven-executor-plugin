package org.kubicz.mavenexecutor.view

import com.intellij.AbstractBundle
import org.jetbrains.annotations.PropertyKey

class MavenExecutorBundle private constructor() : AbstractBundle(PATH_TO_BUNDLE) {

    companion object {

        private const val PATH_TO_BUNDLE = "MavenExecutorBundle"

        private val ourInstance = MavenExecutorBundle()

        fun message(@PropertyKey(resourceBundle = "MavenExecutorBundle") key: String, vararg params: Any): String {
            return ourInstance.getMessage(key, *params)
        }

    }

}

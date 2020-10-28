package org.kubicz.mavenexecutor.runconfiguration

import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.JavaParameters
import com.intellij.openapi.project.Project
import org.kubicz.mavenexecutor.view.window.MavenAdditionalParameters
import org.jetbrains.idea.maven.execution.MavenRunConfiguration

class MavenExecutorRunConfiguration(project: Project, factory: ConfigurationFactory, name: String) : MavenRunConfiguration(project, factory, name) {

    var additionalParameters = MavenAdditionalParameters()

    @Throws(ExecutionException::class)
    override fun createJavaParameters(project: Project?): JavaParameters {
        val javaParameters = super.createJavaParameters(project)

        if (additionalParameters.projects.isNotEmpty()) {
            javaParameters.programParametersList.add("-pl", additionalParameters.projects)
        }

        if (additionalParameters.additionalParameters.isNotEmpty()) {
            javaParameters.programParametersList.addParametersString(additionalParameters.additionalParameters)
        }

        return javaParameters
    }
}
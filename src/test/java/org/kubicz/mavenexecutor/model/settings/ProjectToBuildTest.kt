package org.kubicz.mavenexecutor.model.settings

import org.junit.Test
import org.kubicz.mavenexecutor.model.MavenArtifact
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ProjectToBuildTest {

    @Test
    fun shouldBuildEntire() {
        val projects = projectToBuildWithoutModules()

        assertTrue { projects.buildEntireProject() }
    }

    @Test
    fun shouldNotBuildEntire() {
        val projects = projectToBuild()

        assertFalse { projects.buildEntireProject() }
    }

    @Test
    fun shouldReturnModulesAsTextWithComaSeparator() {
        val projects = projectToBuild()

        assertEquals(projects.selectedModulesAsText(), "group1:artifact1,group2:artifact2")
    }

    @Test
    fun shouldReturnModulesAsEmptyTextWhenNoModules() {
        val projects = projectToBuildWithoutModules()

        assertEquals(projects.selectedModulesAsText(), "")
    }

    private fun projectToBuildWithoutModules(): ProjectToBuild {
        val displayName = "Name"
        val mavenArtifact = MavenArtifact("group", "artifact", "version")
        val projectDictionary = "/dir"
        val selectedModules = mutableListOf<MavenArtifact>()

        return ProjectToBuild(displayName, mavenArtifact, projectDictionary, selectedModules)
    }

    private fun projectToBuild(): ProjectToBuild {
        val displayName = "Name"
        val mavenArtifact = MavenArtifact("group", "artifact", "version")
        val projectDictionary = "/dir"
        val selectedModules = arrayListOf(MavenArtifact("group1", "artifact1", "version1"), MavenArtifact("group2", "artifact2", "version2"))

        return ProjectToBuild(displayName, mavenArtifact, projectDictionary, selectedModules)
    }

}


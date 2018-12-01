package org.kubicz.mavenexecutor.model.settings

import org.junit.Test
import kotlin.test.assertEquals

class ExecutionSettingsTest {

    @Test
    fun shouldReturnAddGoalsFromText() {
        val settings = ExecutionSettings()

        settings.goalsFromText("clean install")

        assertEquals(settings.goalsAsText(), "clean install")
    }

    @Test
    fun shouldReturnGoalsAsTextWithSpaceSeparator() {
        val settings = ExecutionSettings()

        settings.goals.add("clean")
        settings.goals.add("install")

       assertEquals(settings.goalsAsText(), "clean install")
    }

    @Test
    fun shouldReturnEmptyTextWhenNoGoals() {
        val settings = ExecutionSettings()

        assertEquals(settings.goalsAsText(), "")
    }

    @Test
    fun shouldReturnJvmOptionsAsTextWithSpaceSeparator() {
        val settings = ExecutionSettings()

        settings.jvmOptions.add("-Dprop1=1")
        settings.jvmOptions.add("-Dprop1=1")

        assertEquals(settings.jvmOptionsAsText(), "-Dprop1=1 -Dprop1=1")
    }

    @Test
    fun shouldReturnEmptyTextWhenNoJvmOptions() {
        val settings = ExecutionSettings()

        assertEquals(settings.jvmOptionsAsText(), "")
    }

    @Test
    fun shouldReturnOptionalJvmOptionsAsTextWithSpaceSeparator() {
        val settings = ExecutionSettings()

        settings.optionalJvmOptions.add("-Dprop1=1")
        settings.optionalJvmOptions.add("-Dprop1=1")

        assertEquals(settings.optionalJvmOptionsAsText(), "-Dprop1=1 -Dprop1=1")
    }

    @Test
    fun shouldReturnEmptyTextWhenNoOptionalJvmOptions() {
        val settings = ExecutionSettings()

        assertEquals(settings.optionalJvmOptionsAsText(), "")
    }

}

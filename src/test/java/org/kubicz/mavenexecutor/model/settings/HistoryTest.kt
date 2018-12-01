package org.kubicz.mavenexecutor.model.settings

import org.junit.Test
import kotlin.test.assertEquals

class HistoryTest {

    @Test
    fun shouldNotAddEmptyString() {
        val history = History()

        history.add("")

        assertEquals(history.asArray().size, 0)
    }

    @Test
    fun shouldNotOverflow() {
        val history = History()

        val maxHistorySize = 20

        for (i in 1..maxHistorySize + 1) {
            history.add(i.toString())
        }

        assertEquals(history.asArray().size, maxHistorySize)
    }

    @Test
    fun shouldNotContainsDuplication() {
        val history = History()

        val maxHistorySize = 20

        for (i in 1..maxHistorySize) {
            history.add("TEST")
        }

        assertEquals(history.asArray().size, 1)
    }

    @Test
    fun shouldLastAddedElementBeOnTop() {
        val history = History()

        val maxHistorySize = 20

        for (i in 1..maxHistorySize) {
            history.add(i.toString())
        }

        val newElement = "10"
        history.add(newElement)

        assertEquals(history.asArray().first(), newElement)
    }

}
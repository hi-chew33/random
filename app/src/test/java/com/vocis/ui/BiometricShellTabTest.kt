package com.vocis.ui

import com.vocis.ui.screens.VocisTab
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class BiometricShellTabTest {

    @Test
    fun `test biometric shell tabs correspond to 5 core navigation screens`() {
        val dashboard = VocisTab.Dashboard
        val calls = VocisTab.Calls
        val voices = VocisTab.Voices
        val tools = VocisTab.Tools
        val settings = VocisTab.Settings

        assertEquals(0, dashboard.index)
        assertEquals("Shield", dashboard.label)

        assertEquals(1, calls.index)
        assertEquals("Calls", calls.label)

        assertEquals(2, voices.index)
        assertEquals("Voices", voices.label)

        assertEquals(3, tools.index)
        assertEquals("Tools", tools.label)

        assertEquals(4, settings.index)
        assertEquals("Settings", settings.label)
    }

    @Test
    fun `test all tab indices are unique`() {
        val tabs = listOf(
            VocisTab.Dashboard,
            VocisTab.Calls,
            VocisTab.Voices,
            VocisTab.Tools,
            VocisTab.Settings
        )

        val indices = tabs.map { it.index }
        assertEquals(5, indices.toSet().size)
    }
}

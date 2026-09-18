package com.vocis.ui

import android.content.Context
import android.view.WindowManager
import com.vocis.ui.overlay.ProtectionOverlayManager
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class ProtectionOverlayManagerTest {

    private lateinit var mockContext: Context
    private lateinit var mockWindowManager: WindowManager
    private lateinit var overlayManager: ProtectionOverlayManager

    @Before
    fun setUp() {
        mockContext = mock(Context::class.java)
        mockWindowManager = mock(WindowManager::class.java)
        `when`(mockContext.getSystemService(Context.WINDOW_SERVICE)).thenReturn(mockWindowManager)
        overlayManager = ProtectionOverlayManager(mockContext)
    }

    @Test
    fun `test overlay manager initializes with not showing state`() {
        assertNotNull(overlayManager)
        assertFalse("Overlay must not be showing initially", overlayManager.isShowing())
    }

    @Test
    fun `test hideOverlay is idempotent when not displayed`() {
        overlayManager.hideOverlay()
        assertFalse(overlayManager.isShowing())
    }

    @Test
    fun `test updateRisk does not crash when overlay is not displayed`() {
        overlayManager.updateRisk(85, "High Threat Level")
        assertFalse(overlayManager.isShowing())
    }
}

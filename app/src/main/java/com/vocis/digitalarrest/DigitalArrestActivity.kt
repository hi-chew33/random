package com.vocis.digitalarrest

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.vocis.VocisApplication
import com.vocis.ui.screens.DigitalArrestScreen
import com.vocis.ui.theme.VocisTheme

/**
 * Dedicated Activity for Digital Arrest incident guided remediation.
 * Supports keyguard display and screen wake-up during high-threat extortion calls.
 */
class DigitalArrestActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        configureWindowForLockscreen()

        val controller = (application as? VocisApplication)?.digitalArrestController
            ?: DigitalArrestController()

        setContent {
            VocisTheme {
                DigitalArrestScreen(
                    controller = controller,
                    onBack = { finish() }
                )
            }
        }
    }

    private fun configureWindowForLockscreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
            keyguardManager?.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}

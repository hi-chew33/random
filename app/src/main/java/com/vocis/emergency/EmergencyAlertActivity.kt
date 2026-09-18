package com.vocis.emergency

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.graphics.Color
import android.view.Gravity

class EmergencyAlertActivity : Activity() {

    companion object {
        const val EXTRA_SENDER = "extra_sender"
        const val EXTRA_MESSAGE = "extra_message"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        configureWindowForLockscreen()

        val sender = intent?.getStringExtra(EXTRA_SENDER) ?: "Family Contact"
        val message = intent?.getStringExtra(EXTRA_MESSAGE) ?: "Emergency VOCIS keyword trigger received."

        setContentView(createEmergencyLayout(sender, message))
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

    private fun createEmergencyLayout(sender: String, message: String): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#1A0000")) // Urgent Dark Crimson
            gravity = Gravity.CENTER
            setPadding(48, 64, 48, 64)

            val titleView = TextView(this@EmergencyAlertActivity).apply {
                text = "⚠️ EMERGENCY SOS ALERT"
                setTextColor(Color.parseColor("#FF3B30")) // Alert Red
                textSize = 24f
                gravity = Gravity.CENTER
                setPadding(0, 0, 0, 32)
            }
            addView(titleView)

            val senderView = TextView(this@EmergencyAlertActivity).apply {
                text = "Sender: $sender"
                setTextColor(Color.WHITE)
                textSize = 18f
                gravity = Gravity.CENTER
                setPadding(0, 0, 0, 16)
            }
            addView(senderView)

            val bodyView = TextView(this@EmergencyAlertActivity).apply {
                text = message
                setTextColor(Color.LTGRAY)
                textSize = 16f
                gravity = Gravity.CENTER
                setPadding(0, 0, 0, 48)
            }
            addView(bodyView)

            val dismissButton = Button(this@EmergencyAlertActivity).apply {
                text = "SILENCE & DISMISS ALARM"
                setBackgroundColor(Color.parseColor("#FF3B30"))
                setTextColor(Color.WHITE)
                textSize = 16f
                setPadding(32, 24, 32, 24)
                setOnClickListener {
                    EmergencyAlarmSystem.stopEmergencyAlarm(this@EmergencyAlertActivity)
                    finish()
                }
            }
            addView(dismissButton)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EmergencyAlarmSystem.stopEmergencyAlarm(this)
    }
}

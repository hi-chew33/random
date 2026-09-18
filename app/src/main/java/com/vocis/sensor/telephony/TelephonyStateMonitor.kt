package com.vocis.sensor.telephony

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.CallLog
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import com.vocis.VocisApplication
import com.vocis.sensor.normalizer.EventNormalizer
import com.vocis.vcd.service.LiveVerificationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver monitoring Android telephony phone state transitions.
 * Directly referenced from VOCIS ProtectionController and TelephonyStateMonitor.
 */
class TelephonyStateMonitor : BroadcastReceiver() {

    companion object {
        var lastIncomingNumber: String? = null
        var lastCallerName: String? = null
        var lastCallStartTimeMs: Long = 0L

        fun queryLatestCallLog(context: Context): Pair<String, String?>? {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
                return null
            }
            return try {
                val cursor = context.contentResolver.query(
                    CallLog.Calls.CONTENT_URI,
                    arrayOf(CallLog.Calls.NUMBER, CallLog.Calls.CACHED_NAME),
                    null,
                    null,
                    "${CallLog.Calls.DATE} DESC LIMIT 1"
                )
                cursor?.use { c ->
                    if (c.moveToFirst()) {
                        val numIdx = c.getColumnIndex(CallLog.Calls.NUMBER)
                        val nameIdx = c.getColumnIndex(CallLog.Calls.CACHED_NAME)
                        val num = if (numIdx >= 0) c.getString(numIdx) else null
                        val name = if (nameIdx >= 0) c.getString(nameIdx) else null
                        if (!num.isNullOrBlank()) Pair(num, name) else null
                    } else null
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent?.action != TelephonyManager.ACTION_PHONE_STATE_CHANGED) return
        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE) ?: return
        val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

        val app = try { VocisApplication.instance } catch (_: Exception) { null }

        if (!incomingNumber.isNullOrBlank()) {
            lastIncomingNumber = incomingNumber
        }

        when (state) {
            TelephonyManager.EXTRA_STATE_RINGING -> {
                val callerNumber = incomingNumber ?: lastIncomingNumber
                if (!callerNumber.isNullOrBlank()) {
                    val event = EventNormalizer.normalizeIncomingCall(callerNumber)
                    app?.interactionHub?.processEventAsync(event)

                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val identity = app?.callerIdentityResolver?.resolve(callerNumber)
                            if (identity != null && !identity.displayName.isNullOrBlank()) {
                                lastCallerName = identity.displayName
                            }
                        } catch (_: Exception) {}
                    }
                }

                // Show in-call protection HUD overlay
                app?.protectionOverlayManager?.showOverlay(
                    threatScore = 0,
                    status = "Screening Call..."
                )
            }

            TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                lastCallStartTimeMs = System.currentTimeMillis()
                val callerNum = incomingNumber ?: lastIncomingNumber ?: queryLatestCallLog(context)?.also {
                    lastIncomingNumber = it.first
                    if (lastCallerName.isNullOrBlank()) lastCallerName = it.second
                }?.first

                // Call answered: start live voice verification service and update overlay
                LiveVerificationService.start(context, callerNum, lastCallerName)
                app?.protectionOverlayManager?.showOverlay(
                    threatScore = 0,
                    status = "Active Call • VCD Screening"
                )
            }

            TelephonyManager.EXTRA_STATE_IDLE -> {
                // Call ended: stop verification and sync call logs
                if (lastIncomingNumber.isNullOrBlank()) {
                    queryLatestCallLog(context)?.let {
                        lastIncomingNumber = it.first
                        if (lastCallerName.isNullOrBlank()) lastCallerName = it.second
                    }
                }

                LiveVerificationService.stop(context)
                app?.interactionHub?.loadRealCallLogs(context)
                app?.protectionOverlayManager?.hideOverlay()
            }
        }
    }
}

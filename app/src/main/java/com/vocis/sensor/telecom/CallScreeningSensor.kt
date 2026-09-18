package com.vocis.sensor.telecom

import android.telecom.Call
import android.telecom.CallScreeningService
import com.vocis.sensor.normalizer.EventNormalizer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class CallScreeningSensor : CallScreeningService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        const val SCREENING_TIMEOUT_MS = 1800L // Hard budget <= 2000ms fail-open
    }

    override fun onScreenCall(callDetails: Call.Details) {
        val handleUri = callDetails.handle?.toString()
        val presentation = 1 // default ALLOWED

        serviceScope.launch {
            val event = EventNormalizer.normalizeIncomingCall(handleUri, presentation)
            
            // Execute within strict fail-open timeout budget
            val decision = withTimeoutOrNull(SCREENING_TIMEOUT_MS) {
                // Future integration hook: query Role B Policy Engine when present
                // Fail-open default: ALLOW call
                false
            } ?: false // Timeout fallback: false (fail-open)

            val response = if (decision) {
                CallResponse.Builder()
                    .setDisallowCall(true)
                    .setRejectCall(true)
                    .setSilenceCall(true)
                    .setSkipCallLog(false)
                    .build()
            } else {
                CallResponse.Builder()
                    .setDisallowCall(false)
                    .setRejectCall(false)
                    .setSilenceCall(false)
                    .setSkipCallLog(false)
                    .build()
            }

            respondToCall(callDetails, response)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}

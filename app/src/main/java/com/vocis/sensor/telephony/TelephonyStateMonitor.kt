package com.vocis.sensor.telephony

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager

import com.vocis.intelligence.hub.InteractionHub
import com.vocis.sensor.normalizer.EventNormalizer

class TelephonyStateMonitor : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent?.action != TelephonyManager.ACTION_PHONE_STATE_CHANGED) return
        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE) ?: return
        val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

        if (state == TelephonyManager.EXTRA_STATE_RINGING && !incomingNumber.isNullOrBlank()) {
            val event = EventNormalizer.normalizeIncomingCall(incomingNumber)
            InteractionHub.getInstance(context).processEventAsync(event)
        }
    }
}

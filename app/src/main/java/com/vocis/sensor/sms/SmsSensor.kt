package com.vocis.sensor.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.vocis.intelligence.hub.InteractionHub
import com.vocis.sensor.normalizer.EventNormalizer

class SmsSensor : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent?.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return
        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        val sender = messages[0].displayOriginatingAddress
        val fullBody = buildString {
            for (sms in messages) {
                append(sms.displayMessageBody)
            }
        }

        val timestamp = messages[0].timestampMillis
        val event = EventNormalizer.normalizeSms(sender, fullBody, timestamp)
        InteractionHub.getInstance(context).processEventAsync(event)
    }
}

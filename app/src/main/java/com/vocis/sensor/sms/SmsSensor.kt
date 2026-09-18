package com.vocis.sensor.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.vocis.sensor.normalizer.EventNormalizer

class SmsSensor : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
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
            // Dispatched to InteractionHub and Emergency keyword matcher when Role B / Role D connect it
        }
    }
}

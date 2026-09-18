package com.vocis.sensor.packageevent

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.vocis.sensor.normalizer.EventNormalizer

class PackageEventMonitor : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_PACKAGE_ADDED) {
            val packageName = intent.data?.schemeSpecificPart
            val event = EventNormalizer.normalizePackageAdded(packageName)
            // Dispatched to InteractionHub when Role B connects it
        }
    }
}

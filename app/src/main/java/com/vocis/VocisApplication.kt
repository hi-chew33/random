package com.vocis

import android.app.Application
import com.vocis.core.data.database.AppDatabase
import com.vocis.core.data.database.VcdDatabase

class VocisApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize dual databases
        AppDatabase.getInstance(this)
        VcdDatabase.getInstance(this)
    }
}

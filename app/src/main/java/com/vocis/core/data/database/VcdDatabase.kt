package com.vocis.core.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.vocis.core.data.dao.ContactVoiceprintDao
import com.vocis.core.data.dao.VcdCallHistoryDao
import com.vocis.core.data.entity.ContactVoiceprintEntity
import com.vocis.core.data.entity.VcdCallHistoryEntity

@Database(
    entities = [
        ContactVoiceprintEntity::class,
        VcdCallHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class VcdDatabase : RoomDatabase() {

    abstract fun contactVoiceprintDao(): ContactVoiceprintDao
    abstract fun vcdCallHistoryDao(): VcdCallHistoryDao

    companion object {
        private const val DB_NAME = "vcd.db"

        @Volatile
        private var instance: VcdDatabase? = null

        fun getInstance(context: Context): VcdDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    VcdDatabase::class.java,
                    DB_NAME
                ).fallbackToDestructiveMigration().build().also { instance = it }
            }
        }
    }
}

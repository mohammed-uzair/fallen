package com.example.uzair.fallen.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.uzair.fallen.database.dao.DeviceEventsDao
import com.example.uzair.fallen.database.model.DeviceEvents

/**
 * Singleton database object. Note that for a real app, you should probably use a Dependency
 * Injection framework or Service Locator to create the singleton database.
 */
private const val DATABASE_NAME = "DeviceEvents.db"

@Database(
    entities = [DeviceEvents::class],
    version = 1
)
abstract class DeviceEventsDatabase : RoomDatabase() {
    abstract fun deviceEventsDao(): DeviceEventsDao

    companion object {
        private var instance: DeviceEventsDatabase? = null
        @Synchronized
        fun get(context: Context): DeviceEventsDatabase {
            if (instance == null) {
                instance = Room.databaseBuilder(
                    context.applicationContext,
                    DeviceEventsDatabase::class.java,
                    DATABASE_NAME
                ).build()
            }
            return instance!!
        }
    }
}
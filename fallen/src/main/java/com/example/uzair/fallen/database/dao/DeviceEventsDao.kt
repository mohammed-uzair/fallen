package com.example.uzair.fallen.database.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.uzair.fallen.database.model.DeviceEvent

/**
 * This is the dao(Data access Object) file, where we write all the abstract logic of data fetching
 * from the local database
 */
@Dao
interface DeviceEventsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addDeviceEvent(deviceEvent: DeviceEvent) : Long

    @Query("SELECT * FROM DeviceEvent")
    fun getAllDeviceEvents(): DataSource.Factory<Int, DeviceEvent>

    @Query("SELECT * FROM DeviceEvent WHERE eventType = :eventType")
    fun getAllDeviceEventsWithEventType(eventType: String): DataSource.Factory<Int, DeviceEvent>
}
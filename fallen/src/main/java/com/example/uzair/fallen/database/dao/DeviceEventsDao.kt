package com.example.uzair.fallen.database.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.uzair.fallen.database.model.DeviceEvents

@Dao
interface DeviceEventsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addDeviceEvent(deviceEvent: DeviceEvents)

    @Query("SELECT * FROM DeviceEvents")
    fun getAllDeviceEvents(): DataSource.Factory<Int, DeviceEvents>

    @Query("SELECT * FROM DeviceEvents WHERE eventType = :eventType")
    fun getAllDeviceEventsWithEventType(eventType: String): DataSource.Factory<Int, DeviceEvents>
}
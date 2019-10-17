package com.example.uzair.fallen.repository

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.example.uzair.fallen.database.model.DeviceEvent

/**
 * Interface for the device events repository
 */
interface IDeviceEventsRepository {
    fun saveDeviceEvent(deviceEvent: DeviceEvent)

    fun getAllDeviceEvents(): LiveData<PagedList<DeviceEvent>>

    fun getAllFallDeviceEvents(): LiveData<PagedList<DeviceEvent>>

    fun getAllShakeDeviceEvents(): LiveData<PagedList<DeviceEvent>>
}
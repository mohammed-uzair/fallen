package com.example.uzair.fallen.repository

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.example.uzair.fallen.database.model.DeviceEvents

/**
 * Interface for the device events repository
 */
interface IDeviceEventsRepository {
    fun getAllDeviceEvents(): LiveData<PagedList<DeviceEvents>>

    fun getAllShakeDeviceEvents(): LiveData<PagedList<DeviceEvents>>

    fun getAllFallDeviceEvents(): LiveData<PagedList<DeviceEvents>>
}
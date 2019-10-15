package com.example.uzair.fallen.database.implementation

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.paging.Config
import androidx.paging.PagedList
import androidx.paging.toLiveData
import com.example.uzair.fallen.database.DeviceEventsDatabase
import com.example.uzair.fallen.database.model.DeviceEvents
import com.example.uzair.fallen.repository.IDeviceEventsRepository

class DeviceEventsDaoImplementation(context: Context) : IDeviceEventsRepository {
    private val dao = DeviceEventsDatabase.get(context).deviceEventsDao()

    override fun getAllDeviceEvents(): LiveData<PagedList<DeviceEvents>> {
        return dao.getAllDeviceEvents()
            .toLiveData(Config(pageSize = 10, enablePlaceholders = true, maxSize = 100))
    }

    override fun getAllShakeDeviceEvents(): LiveData<PagedList<DeviceEvents>> {
        return dao.getAllDeviceEventsWithEventType("Shake")
            .toLiveData(Config(pageSize = 10, enablePlaceholders = true, maxSize = 100))
    }

    override fun getAllFallDeviceEvents(): LiveData<PagedList<DeviceEvents>> {
        return dao.getAllDeviceEventsWithEventType("Fall")
            .toLiveData(Config(pageSize = 10, enablePlaceholders = true, maxSize = 100))
    }
}
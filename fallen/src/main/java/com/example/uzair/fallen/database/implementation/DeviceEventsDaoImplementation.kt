package com.example.uzair.fallen.database.implementation

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.paging.Config
import androidx.paging.PagedList
import androidx.paging.toLiveData
import com.example.uzair.fallen.database.DeviceEventsDatabase
import com.example.uzair.fallen.database.model.DeviceEvent
import com.example.uzair.fallen.repository.IDeviceEventsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class DeviceEventsDaoImplementation(context: Context) : IDeviceEventsRepository {
    private val dao = DeviceEventsDatabase.get(context).deviceEventsDao()

    override fun getAllDeviceEvents(): LiveData<PagedList<DeviceEvent>> {
        return dao.getAllDeviceEvents()
            .toLiveData(Config(pageSize = 10, enablePlaceholders = true, maxSize = 100))
    }

    override fun getAllShakeDeviceEvents(): LiveData<PagedList<DeviceEvent>> {
        return dao.getAllDeviceEventsWithEventType("Shake")
            .toLiveData(Config(pageSize = 10, enablePlaceholders = true, maxSize = 100))
    }

    override fun getAllFallDeviceEvents(): LiveData<PagedList<DeviceEvent>> {
        return dao.getAllDeviceEventsWithEventType("Fall")
            .toLiveData(Config(pageSize = 10, enablePlaceholders = true, maxSize = 100))
    }

    override fun saveDeviceEvent(deviceEvent: DeviceEvent) {
        CoroutineScope(IO).launch {
            dao.addDeviceEvent(deviceEvent)
        }
    }
}
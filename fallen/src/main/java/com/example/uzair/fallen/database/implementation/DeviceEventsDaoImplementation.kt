package com.example.uzair.fallen.database.implementation

import android.content.Context
import androidx.paging.Config
import androidx.paging.toLiveData
import com.example.uzair.fallen.database.DeviceEventsDatabase
import com.example.uzair.fallen.database.model.DeviceEvent
import com.example.uzair.fallen.repository.IDeviceEventsRepository
import com.example.uzair.fallen.util.DeviceEventType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

/**
 * This is the implementation class for the dao, here we write any other logic to be passed to the
 * final query statements
 */
class DeviceEventsDaoImplementation(context: Context) : IDeviceEventsRepository {
    private val dao = DeviceEventsDatabase.get(context).deviceEventsDao()

    override fun saveDeviceEvent(deviceEvent: DeviceEvent) {
        CoroutineScope(IO).launch {
            dao.addDeviceEvent(deviceEvent)
        }
    }

    override fun getAllDeviceEvents() =
        dao.getAllDeviceEvents()
            .toLiveData(Config(pageSize = 10, enablePlaceholders = true, maxSize = 100))

    override fun getAllFallDeviceEvents() =
        dao.getAllDeviceEventsWithEventType(DeviceEventType.FALL.name)
            .toLiveData(Config(pageSize = 10, enablePlaceholders = true, maxSize = 100))

    override fun getAllShakeDeviceEvents() =
        dao.getAllDeviceEventsWithEventType(DeviceEventType.SHAKE.name)
            .toLiveData(Config(pageSize = 10, enablePlaceholders = true, maxSize = 100))
}
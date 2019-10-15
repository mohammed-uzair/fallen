package com.example.uzair.fallen.repository

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.example.uzair.fallen.database.implementation.DeviceEventsDaoImplementation
import com.example.uzair.fallen.database.model.DeviceEvents

/**
 * This class is the intermediate between the data sources and the data requesting logic.
 * <p>
 * When ever a data is requested, this class is responsible to decide where to fetch the data
 * from (eg: local cache, database, web service).
 * <p>
 * This class abstracts the data fetch logic from other modules, thus other modules
 * should only ask data from this class and they should not care where the data is received from
 * (eg: local cache, database, web service).
 * <p>
 * For this application I have only implemented the data source as a local db,
 * but at later point we can also add a web service or a local cache without disturbing any other
 * modules.
 */
class DeviceEventsRepository(private val context: Application) : IDeviceEventsRepository {
    private lateinit var deviceEventsDataSource: IDeviceEventsRepository

    override fun getAllDeviceEvents(): LiveData<PagedList<DeviceEvents>> {
        setDataSource()
        return deviceEventsDataSource.getAllDeviceEvents()
    }

    override fun getAllShakeDeviceEvents(): LiveData<PagedList<DeviceEvents>> {
        setDataSource()
        return deviceEventsDataSource.getAllShakeDeviceEvents()
    }

    override fun getAllFallDeviceEvents(): LiveData<PagedList<DeviceEvents>> {
        setDataSource()
        return deviceEventsDataSource.getAllFallDeviceEvents()
    }

    private fun setDataSource() {
        /*Write a logic here to find where to get the data from,
        for now in this case its only local database.*/

        /*If the request is raised for the second time within a short interval(<5 seconds),
        return the data from the cached version*/

        //We are getting the data from the local data base
        deviceEventsDataSource = DeviceEventsDaoImplementation(context)
    }
}
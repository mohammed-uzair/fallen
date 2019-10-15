package com.example.uzair.iamfalling.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.uzair.fallen.repository.DeviceEventsRepository

/**
 * A simple ViewModel that provides a paged list of all device events.
 */
class DeviceEventsViewModel(val app: Application) : AndroidViewModel(app) {
    /*In real projects, consider using dependency injection, and get the singleton
    * object for the repository*/
    private val repository = DeviceEventsRepository(app)

    /**
     * We use -ktx Kotlin extension functions here, otherwise you would use LivePagedListBuilder(),
     * and PagedList.Config.Builder()
     */
    val allPosts = repository.getAllDeviceEvents()
}
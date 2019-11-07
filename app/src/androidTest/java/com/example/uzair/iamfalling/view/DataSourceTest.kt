package com.example.uzair.iamfalling.view

import android.app.Application
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import com.example.uzair.fallen.database.model.DeviceEvent
import com.example.uzair.fallen.repository.DeviceEventsRepository
import com.example.uzair.fallen.util.DeviceEventType
import com.example.uzair.iamfalling.R
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

class DataSourceTest {
    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(HomeActivity::class.java)

    private val application by lazy {
        InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application
    }

    private val repository by lazy { DeviceEventsRepository(application) }

    @Test
    fun testDataSavedInDataSource() {
        //Create a fake device event
        val deviceEvent = DeviceEvent(
            0,
            DeviceEventType.FALL.value(),
            "02 Aug 18 10:20:12",
            0.0
        )

        //Add this fake event into data source
        repository.saveDeviceEvent(deviceEvent)
    }

    @Test
    fun testDataRetriedFromDataSource() {
        //Create a fake device event
        val deviceEvent = DeviceEvent(
            0,
            DeviceEventType.FALL.value(),
            "02 Aug 18 10:20:12",
            0.0
        )

        //Add this fake event into data source
        repository.saveDeviceEvent(deviceEvent)

        //Test if we could get any data back from data source
        val allPosts = repository.getAllDeviceEvents()

        mActivityTestRule.runOnUiThread {
            allPosts.observe(getCurrentVisibleFragment() as Fragment, Observer { list ->
                Assert.assertNotNull(list)
                Assert.assertEquals(true, list.size > 0)
            })
        }
    }

    private fun getCurrentVisibleFragment() =
        mActivityTestRule.activity.supportFragmentManager.findFragmentById(R.id.root_home_activity)
}
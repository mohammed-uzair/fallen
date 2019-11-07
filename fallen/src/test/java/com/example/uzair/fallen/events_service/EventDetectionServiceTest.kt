package com.example.uzair.fallen.events_service

import org.junit.Assert
import org.junit.Before
import org.junit.Test

class EventDetectionServiceTest {
    private var fallen = EventDetectionService()

    @Before
    fun setup() {
        fallen.changeMinimumFallTime()

        fallen.changeAccelerometerSensorDelay()
    }

    @Test
    fun testIfDeviceFell() {
        //Give a fall to the device accelerometer axis readings
        fallen.testDeviceFall(-1F, -1F, -1F)

        //Bring back the readings to the normal
        Assert.assertEquals(
            true, fallen.testDeviceFall(
                -4.9609985F,
                4.3224487F,
                -10.31781F
            )
        )
    }

    @Test
    fun testIfDeviceDidNotFall() {
        Assert.assertEquals(false, fallen.testDeviceFall(-2F, -5F, -13F))
    }

    @Test
    fun testIfDeviceWasShaken() {
        //Give a hard shake to the device accelerometer axis readings
        fallen.testDeviceShaken(
            -40.579544F, -5.352524F, 8.846573F
        )

        //Bring back the readings to the normal
        Assert.assertEquals(
            true, fallen.testDeviceShaken(
                -4.9609985F,
                4.3224487F,
                -10.31781F
            )
        )
    }

    @Test
    fun testIfDeviceWasNotShaken() {
        Assert.assertEquals(false, fallen.testDeviceShaken(0F, 0F, 0F))
    }
}
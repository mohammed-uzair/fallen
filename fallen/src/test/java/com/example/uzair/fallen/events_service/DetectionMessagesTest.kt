package com.example.uzair.fallen.events_service

import junit.framework.Assert.assertTrue
import org.junit.Test

class DetectionMessagesTest {
    @Test
    fun testIfDetectionMessagesGiveRandomMessage() {
        DetectionMessages.fallDetectionMessages = arrayOf(
            "Random Message 1",
            "Random Message 2",
            "Random Message 3"
        )

        DetectionMessages.getFallDetectionMessage()?.let {
            assertTrue(it.contains("Random Message "))
        }
    }
}
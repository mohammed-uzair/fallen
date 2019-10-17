package com.example.uzair.fallen.events_service

import java.util.concurrent.ThreadLocalRandom

/**
 * A simple model class to hold the event messages and return a single random message when asked
 */
class DetectionMessages {
    companion object {
        var fallDetectionMessages: Array<String>? = null
            set(value) {
                field = null
                field = value
            }

        var frequentFallDetectionMessages: Array<String>? = null
            set(value) {
                field = null
                field = value
            }

        var shakeDetectionMessages: Array<String>? = null
            set(value) {
                field = null
                field = value
            }

        fun getFallDetectionMessage() = fallDetectionMessages?.let {
            it[ThreadLocalRandom.current().nextInt(0, it.size)]
        }

        fun getFrequentFallDetectionMessage() = frequentFallDetectionMessages?.let {
            it[ThreadLocalRandom.current().nextInt(0, it.size)]
        }

        fun getShakeDetectionMessage() = shakeDetectionMessages?.let {
            it[ThreadLocalRandom.current().nextInt(0, it.size)]
        }
    }
}
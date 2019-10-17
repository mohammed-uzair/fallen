package com.example.uzair.fallen.util

/**
 * Enum to differentiate between the event type
 */
enum class DeviceEventType(private val value: String) {
    FALL("Fall"),
    SHAKE("Shake");

    fun value(): String = value
}
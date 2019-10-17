package com.example.uzair.fallen.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A simple data class to store the device events
 */
@Entity
data class DeviceEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val eventType: String,
    val eventOccurredTime: String,
    val eventDuration: Double
)
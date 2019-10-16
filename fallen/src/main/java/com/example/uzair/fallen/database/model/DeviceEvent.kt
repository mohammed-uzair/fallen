package com.example.uzair.fallen.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DeviceEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val eventType: String,
    val eventComment: String,
    val eventDuration: Double
)
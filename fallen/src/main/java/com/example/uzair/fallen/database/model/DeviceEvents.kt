package com.example.uzair.fallen.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DeviceEvents(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val eventType: String,
    val eventComment: String,
    val eventDuration: Double
)
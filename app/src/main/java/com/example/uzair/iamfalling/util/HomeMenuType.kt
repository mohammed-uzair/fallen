package com.example.uzair.iamfalling.util

enum class HomeMenuType(private val value: Int) {
    ONLY_FALLS(0),
    ONLY_SHAKES(1),
    FALLS_AND_SHAKES(2),
    ALL(3),
    ALL_EVENTS(4),
    STOP_SERVICE(5);

    fun value(): Int = value
}
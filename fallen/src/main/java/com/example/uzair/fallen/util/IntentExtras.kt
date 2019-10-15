package com.example.uzair.fallen.util

enum class IntentExtras constructor(private val value: String) {
    SERVICE_NAME("ServiceName"),
    SERVICE_DESCRIPTION("ServiceDescription");

    override fun toString(): String = value
}
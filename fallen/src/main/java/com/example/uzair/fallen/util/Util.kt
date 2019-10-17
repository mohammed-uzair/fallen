package com.example.uzair.fallen.util

import java.text.SimpleDateFormat
import java.util.*

/**
 * Function to get current date in default format
 * {@link Constants}
 *
 * @return Formatted date string
 */
fun getCurrentDateAndTime(): String {
    val dateFormat = SimpleDateFormat(FORMAT_DDMMYYHHMMSS, Locale.ENGLISH)
    val calendar = Calendar.getInstance(Locale.ENGLISH)
    return dateFormat.format(calendar.time)
}
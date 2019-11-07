package com.example.uzair.fallen.util

import java.text.SimpleDateFormat
import java.util.*


object Util {
    /**
     * Function to get current date in default format
     * {@link Constants}
     *
     * @return Formatted date string
     */
    fun getCurrentDateAndTime(): String {
        val dateFormat = SimpleDateFormat(FORMAT_DDMMYYHHMMSS, Locale.GERMANY)
        val calendar = Calendar.getInstance(Locale.GERMANY)
        return dateFormat.format(calendar.time)
    }

    /**
     * This method will convert the timestamp value to the
     * date and time.
     *
     * Returns the converted timestamp to datetime in String
     */
    fun convertTimestampToDateTime(timestamp: String): String {
        return try {
            val dateFormat = SimpleDateFormat(FORMAT_DDMMYYHHMMSS, Locale.GERMANY)
            val convertedDateTime = Date(timestamp.toLong() * 1000)
            dateFormat.format(convertedDateTime)
        } catch (e: Exception) {
            e.toString()
        }
    }
}
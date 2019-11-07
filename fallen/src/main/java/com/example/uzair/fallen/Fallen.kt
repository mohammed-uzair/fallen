package com.example.uzair.fallen

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.uzair.fallen.events_service.DetectionMessages
import com.example.uzair.fallen.events_service.EventDetectionService
import com.example.uzair.fallen.util.IntentExtras

class Fallen(private val application: Application) {
    companion object {
        private val TAG = Fallen::class.java.simpleName
        var SHOULD_DETECT_FALL = false
        var SHOULD_DETECT_FREQUENT_FALL = false
        var SHOULD_DETECT_SHAKE = false

        //Foreground static notification
        const val NOTIFICATION_CHANNEL_SENSOR_LISTENER = "NOTIFICATION_CHANNEL_SENSOR_LISTENER"

        //High priority notifications
        const val NOTIFICATION_CHANNEL_FALL = "NOTIFICATION_CHANNEL_FALL"

        //Low priority notifications
        const val NOTIFICATION_CHANNEL_OTHER = "NOTIFICATION_CHANNEL_OTHER"
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //Foreground static notification
            val foregroundNotificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_SENSOR_LISTENER,
                application.resources.getString(R.string.notification_channel_sensor_listener_name),
                NotificationManager.IMPORTANCE_HIGH
            )
            foregroundNotificationChannel.description =
                application.resources.getString(R.string.notification_channel_sensor_listener_description)

            val fallNotificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_FALL,
                application.resources.getString(R.string.notification_channel_fall_detection_name),
                NotificationManager.IMPORTANCE_HIGH
            )
            fallNotificationChannel.description =
                application.resources.getString(R.string.notification_channel_fall_detection_description)

            val otherNotificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_OTHER,
                application.resources.getString(R.string.notification_channel_other_detection_name),
                NotificationManager.IMPORTANCE_LOW
            )
            otherNotificationChannel.description =
                application.resources.getString(R.string.notification_channel_other_detection_description)

            val manager = application.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(foregroundNotificationChannel)
            manager?.createNotificationChannel(fallNotificationChannel)
            manager?.createNotificationChannel(otherNotificationChannel)
        }
    }

    fun startFallen(
        detectFalls: Boolean = false,
        detectShakes: Boolean = false,
        detectFrequentFalls: Boolean = false,
        serviceName: String = application.getString(R.string.library_name),
        serviceDescription: String = application.getString(R.string.serviceDescription)
    ) {
        val intent = Intent(application, EventDetectionService::class.java)
        intent.putExtra(IntentExtras.SERVICE_NAME.name, serviceName)
        intent.putExtra(IntentExtras.SERVICE_DESCRIPTION.name, serviceDescription)

        SHOULD_DETECT_FALL = detectFalls
        SHOULD_DETECT_FREQUENT_FALL = detectFrequentFalls
        SHOULD_DETECT_SHAKE = detectShakes

        ContextCompat.startForegroundService(application, intent)
    }

    fun stopFallen() {
        val intent = Intent(application, EventDetectionService::class.java)
        application.stopService(intent)
    }

    fun setFallDetectionMessages(fallDetectionMessages: Array<String>) {
        DetectionMessages.fallDetectionMessages = fallDetectionMessages
    }

    fun setFrequentFallDetectionMessages(frequentFallDetectionMessages: Array<String>) {
        DetectionMessages.frequentFallDetectionMessages = frequentFallDetectionMessages
    }

    fun setShakeDetectionMessages(shakeDetectionMessages: Array<String>) {
        DetectionMessages.shakeDetectionMessages = shakeDetectionMessages
    }
}
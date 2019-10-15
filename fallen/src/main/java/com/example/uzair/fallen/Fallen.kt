package com.example.uzair.fallen

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.uzair.fallen.events_service.DetectionMessages
import com.example.uzair.fallen.events_service.FallDetectionService
import com.example.uzair.fallen.util.IntentExtras

class Fallen(private val application: Application) {
    companion object {
        private val TAG = this::class.java.simpleName
        public val DETECT_FALL = true
        public val DETECT_SHAKES = false

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

    fun startEventDetectionService(
        serviceName: String = application.resources.getString(R.string.app_name),
        serviceDescription: String = application.resources.getString(R.string.serviceDescription)
    ) {
        val intent = Intent(application, FallDetectionService::class.java)
        intent.putExtra(IntentExtras.SERVICE_NAME.name, serviceName)
        intent.putExtra(IntentExtras.SERVICE_DESCRIPTION.name, serviceDescription)

        ContextCompat.startForegroundService(application, intent)
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
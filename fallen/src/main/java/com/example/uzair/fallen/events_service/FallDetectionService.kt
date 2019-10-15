package com.example.uzair.fallen.events_service

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.PowerManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.uzair.fallen.Fallen.Companion.DETECT_SHAKES
import com.example.uzair.fallen.Fallen.Companion.NOTIFICATION_CHANNEL_FALL
import com.example.uzair.fallen.Fallen.Companion.NOTIFICATION_CHANNEL_SENSOR_LISTENER
import com.example.uzair.fallen.R
import com.example.uzair.fallen.util.IntentExtras
import kotlin.math.pow
import kotlin.math.sqrt

class FallDetectionService : IntentService("FallDetectionService"), SensorEventListener {
    private lateinit var wakeLock: PowerManager.WakeLock
    private lateinit var sensorManager: SensorManager

    private var deviceLastShakenTime: Long = 0

    //test 3 https://stackoverflow.com/a/2318356/11928533
    private var finalSensorAcceleration = 0F
    private var currentSensorAcceleration = SensorManager.GRAVITY_EARTH
    private var previousSensorAcceleration = SensorManager.GRAVITY_EARTH
    private var currentDeviceTime = 0L

    companion object {
        private const val TAG = "FallDetectionService"
        private const val SENSOR_LISTENER_NOTIFICATION_ID = 1
        private const val FALL_DETECTION_NOTIFICATION_ID = 2
        private const val OTHER_DETECTION_NOTIFICATION_ID = 3

        private const val ACCELEROMETER_SENSOR_DELAY = 1200
        private const val DEVICE_SHAKE_ACCELERATION_SPEED = 20
    }

    override fun setIntentRedelivery(enabled: Boolean) {
        super.setIntentRedelivery(true)
    }

    override fun onHandleIntent(intent: Intent?) {
        Log.d(TAG, "On Handle Intent Called")

        val input = intent?.getStringExtra("inputExtra")
    }

    override fun onCreate() {
        Log.d(TAG, "Free Fall Service Started")

        val powerManager: PowerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "FreeFall:WakeLock"
        )

        wakeLock.acquire(10000)
        Log.d(TAG, "Wake lock acquired")

        //Set the sensor listener
        setSensor()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val serviceName = intent?.getStringExtra(IntentExtras.SERVICE_NAME.name)
        val serviceDescription = intent?.getStringExtra(IntentExtras.SERVICE_DESCRIPTION.name)

//        val myApp = Intent(this, HomeActivity::class.java)
//        val pendingIntent = PendingIntent.getActivity(
//            this,
//            101, myApp, 0
//        )

        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_SENSOR_LISTENER)
            .setContentTitle(serviceName)
            .setContentText(serviceDescription)
            .setSmallIcon(R.drawable.ic_service)
//            .setContentIntent(pendingIntent)
            .build()

        startForeground(SENSOR_LISTENER_NOTIFICATION_ID, notification)

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        wakeLock.release()
        Log.d(TAG, "Wake lock released")

        sensorManager.unregisterListener(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true) //true will remove notification
        }

        Log.d(TAG, "Free Fall Service Destroyed")
    }

    private fun setSensor() {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        sensorManager.registerListener(
            this, sensorManager.getDefaultSensor(
                Sensor.TYPE_ACCELEROMETER
            ),
            ACCELEROMETER_SENSOR_DELAY
        )
    }

    //region Sensor changes
    override fun onSensorChanged(event: SensorEvent?) {
        Log.d(TAG, "onSensorChanged called")
        val sensor = event?.sensor?.type
        if (sensor?.and(Sensor.TYPE_ACCELEROMETER) == 1) {
            val xAxisValue = event.values[0]
            val yAxisValue = event.values[1]
            val zAxisValue = event.values[2]

            val rootSquare = sqrt(xAxisValue.pow(2) + yAxisValue.pow(2) + zAxisValue.pow(2))
            Log.d(TAG, "Sensor Computed Value : $rootSquare")

            //Fall detection
            if (rootSquare < 2.0) {
                Toast.makeText(this, "Fall detected Value : $rootSquare", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Fall sensor Value : $rootSquare")

                //Show a notification
                var message = DetectionMessages.getFallDetectionMessage()
                if (message == null) {
                    message = application.getString(R.string.notification_fall_detected_message)
                }
                val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_FALL)
                    .setSmallIcon(R.drawable.ic_service)
                    .setContentTitle(application.getString(R.string.notification_fall_name))
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .build()

                NotificationManagerCompat.from(this).notify(FALL_DETECTION_NOTIFICATION_ID, notification)
            }

            //Shake detection
            if (DETECT_SHAKES) {
                currentDeviceTime = System.currentTimeMillis()
                if (isSensorsTimeDifferencesGreaterThanThreshold()) {
                    previousSensorAcceleration = currentSensorAcceleration

                    currentSensorAcceleration =
                        sqrt(
                            (xAxisValue * xAxisValue)
                                    + (yAxisValue * yAxisValue)
                                    + (zAxisValue * zAxisValue)
                        )

                    val differenceOfAcceleration =
                        currentSensorAcceleration - previousSensorAcceleration
                    finalSensorAcceleration =
                        finalSensorAcceleration * 0.9f + differenceOfAcceleration

                    if (finalSensorAcceleration > DEVICE_SHAKE_ACCELERATION_SPEED) {
                        deviceLastShakenTime = currentDeviceTime

                        Toast.makeText(
                            applicationContext,
                            "Device has shaken with acceleration = $finalSensorAcceleration",
                            Toast.LENGTH_SHORT
                        ).show()

                        Log.d(
                            "Shaken",
                            "Device has shaken with acceleration = $finalSensorAcceleration"
                        )
                    }
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    private fun isSensorsTimeDifferencesGreaterThanThreshold() =
        currentDeviceTime - deviceLastShakenTime > ACCELEROMETER_SENSOR_DELAY
    //endregion
}
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
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.uzair.fallen.Fallen
import com.example.uzair.fallen.Fallen.Companion.DETECT_FALL
import com.example.uzair.fallen.Fallen.Companion.DETECT_FREQUENT_FALLS
import com.example.uzair.fallen.Fallen.Companion.DETECT_SHAKES
import com.example.uzair.fallen.Fallen.Companion.NOTIFICATION_CHANNEL_SENSOR_LISTENER
import com.example.uzair.fallen.R
import com.example.uzair.fallen.database.model.DeviceEvent
import com.example.uzair.fallen.repository.DeviceEventsRepository
import com.example.uzair.fallen.repository.IDeviceEventsRepository
import com.example.uzair.fallen.util.IntentExtras
import kotlin.math.pow
import kotlin.math.sqrt

class FallDetectionService : IntentService("FallDetectionService"), SensorEventListener {
    private lateinit var wakeLock: PowerManager.WakeLock
    private lateinit var sensorManager: SensorManager

    private lateinit var deviceEventRepository: IDeviceEventsRepository

    private var deviceFallStartTime: Long = 0
    private var deviceFallEndTime: Long = 0
    private var previousDeviceFellTime: Long = 0
    private var deviceLastShakenTime: Long = 0
    private var finalSensorAcceleration = 0F
    private var currentSensorAcceleration = SensorManager.GRAVITY_EARTH
    private var previousSensorAcceleration = SensorManager.GRAVITY_EARTH
    private var currentDeviceTime = 0L
    private var frequentFallTimeOut = 1000 // 1 second

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

        deviceEventRepository = DeviceEventsRepository(application)

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
            if (DETECT_FALL) {
                if (rootSquare < 2.0) {
                    if (deviceFallStartTime == 0L) {
                        deviceFallStartTime = System.currentTimeMillis()
                    }
                } else if (deviceFallStartTime > 0) {
                    deviceFallEndTime = System.currentTimeMillis()

                    val fallDuration: Double =
                        (deviceFallEndTime - deviceFallStartTime) / 1000.00

                    deviceFallDetected()

                    //Save device event
                    val deviceEvent = DeviceEvent(0, "Fall", "That was a fall", fallDuration)
                    saveToDataSource(deviceEvent)

                    previousDeviceFellTime = deviceFallEndTime
                    deviceFallEndTime = 0L
                    deviceFallStartTime = 0L
                }
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

                        showShakeNotification()

                        //Save device event
                        val deviceEvent = DeviceEvent(0, "Shaken", "You made a good shake", 0.0)
                        saveToDataSource(deviceEvent)
                    }
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    private fun deviceFallDetected() {
        if (DETECT_FREQUENT_FALLS && isAFrequentFall()) {
            showFrequentFallNotification()
        } else {
            showFallNotification()
        }
    }

    private fun isAFrequentFall() =
        (System.currentTimeMillis() - previousDeviceFellTime) < frequentFallTimeOut

    private fun showFallNotification() {
        //Notify user
        Log.d(TAG, "Fall detected")

        //Show a notification
        var message = DetectionMessages.getFallDetectionMessage()
        if (message == null) {
            message = application.getString(R.string.notification_fall_detected_message)
        }
        val notification = NotificationCompat.Builder(
            this,
            Fallen.NOTIFICATION_CHANNEL_FALL
        )
            .setSmallIcon(R.drawable.ic_service)
            .setContentTitle(application.getString(R.string.notification_fall_name))
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .build()

        NotificationManagerCompat.from(this)
            .notify(FALL_DETECTION_NOTIFICATION_ID, notification)
    }

    private fun showFrequentFallNotification() {
        //Notify user
        Log.d(TAG, "Frequent fall detected")

        //Show a notification
        var message = DetectionMessages.getFrequentFallDetectionMessage()
        if (message == null) {
            message = application.getString(R.string.notification_frequent_fall_detected_message)
        }
        val notification = NotificationCompat.Builder(
            this,
            Fallen.NOTIFICATION_CHANNEL_OTHER
        )
            .setSmallIcon(R.drawable.ic_service)
            .setContentTitle(application.getString(R.string.notification_frequent_fall_name))
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .build()

        NotificationManagerCompat.from(this)
            .notify(OTHER_DETECTION_NOTIFICATION_ID, notification)
    }

    private fun showShakeNotification() {
        //Notify user
        Log.d(TAG, "Shake detected")

        //Show a notification
        var message = DetectionMessages.getShakeDetectionMessage()
        if (message == null) {
            message = application.getString(R.string.notification_shake_detected_message)
        }
        val notification = NotificationCompat.Builder(
            this,
            Fallen.NOTIFICATION_CHANNEL_FALL
        )
            .setSmallIcon(R.drawable.ic_service)
            .setContentTitle(application.getString(R.string.notification_shaken_name))
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .build()

        NotificationManagerCompat.from(this)
            .notify(OTHER_DETECTION_NOTIFICATION_ID, notification)
    }

    private fun isSensorsTimeDifferencesGreaterThanThreshold() =
        currentDeviceTime - deviceLastShakenTime > ACCELEROMETER_SENSOR_DELAY

    /**
     * Save the data to the data source
     */
    private fun saveToDataSource(deviceEvent: DeviceEvent) {
        deviceEventRepository.saveDeviceEvent(deviceEvent)
    }
    //endregion
}
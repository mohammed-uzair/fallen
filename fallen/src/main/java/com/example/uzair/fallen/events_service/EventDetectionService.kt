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
import com.example.uzair.fallen.util.DeviceEventType
import com.example.uzair.fallen.util.IntentExtras
import com.example.uzair.fallen.util.getCurrentDateAndTime
import kotlin.math.pow
import kotlin.math.sqrt

class EventDetectionService : IntentService(EventDetectionService::class.java.simpleName),
    SensorEventListener {
    private lateinit var wakeLock: PowerManager.WakeLock
    private lateinit var sensorManager: SensorManager

    private lateinit var deviceEventRepository: IDeviceEventsRepository

    private var deviceFallStartTime = 0L
    private var deviceFallEndTime = 0L

    private var deviceShakeStartTime = 0L
    private var deviceShakeEndTime = 0L

    private var previousDeviceFellTime = 0L
    private var deviceLastShakenTime = 0L
    private var currentDeviceTime = 0L
    private var deviceAccelerationWithoutGravity = 0F
    private var previousDeviceAccelerationWithGravity = SensorManager.GRAVITY_EARTH
    private var currentDeviceAccelerationWithGravity = SensorManager.GRAVITY_EARTH
    private var previousXAxisValue = 0F
    private var previousYAxisValue = 0F

    companion object {
        private val TAG = this::class.java.simpleName
        private const val SENSOR_LISTENER_NOTIFICATION_ID = 1
        private const val FALL_DETECTION_NOTIFICATION_ID = 2
        private const val OTHER_DETECTION_NOTIFICATION_ID = 3

        private const val ACCELEROMETER_SENSOR_DELAY = 1200
        private const val DEVICE_SHAKE_ACCELERATION_SPEED = 20
        private const val FALL_THRESHOLD = 2.0
        private const val FREQUENT_FALL_TIMEOUT = 5000 // 5 second
        private const val MINIMUM_FALL_TIME = 0.080
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
            Log.d(TAG, "onSensorChanged accelerometer called")
            val xAxisValue = event.values[0]
            val yAxisValue = event.values[1]
            val zAxisValue = event.values[2]

            Log.d(TAG, "Sensor Computed Value : X = $xAxisValue Y = $yAxisValue Z = $zAxisValue")

            val rootSquare = sqrt(xAxisValue.pow(2) + yAxisValue.pow(2) + zAxisValue.pow(2))
            Log.d(TAG, "Square root = $rootSquare")

            //Fall detection
            if (DETECT_FALL) {
                if (rootSquare < FALL_THRESHOLD) {
                    if (deviceFallStartTime == 0L) {
                        //Begin fall
                        deviceFallStartTime = System.currentTimeMillis()
                        Log.d(TAG, "Fall started at $deviceFallStartTime")

                        previousXAxisValue = xAxisValue
                        previousYAxisValue = yAxisValue
                    }
                }
                //End fall
                else if (deviceFallStartTime > 0) {
                    deviceFallEndTime = System.currentTimeMillis()

                    Log.d(TAG, "Fall ended at $deviceFallEndTime")
                    val fallDuration: Double =
                        (deviceFallEndTime - deviceFallStartTime) / 1000.00

                    Log.d(TAG, "Fall duration is $fallDuration")

                    if (fallDuration > MINIMUM_FALL_TIME)
                        deviceFallDetected(fallDuration)

                    previousDeviceFellTime = deviceFallEndTime
                    deviceFallEndTime = 0L
                    deviceFallStartTime = 0L
                }
            }

            //Shake detection
            currentDeviceTime = System.currentTimeMillis()
            if (DETECT_SHAKES) {
                if (isSensorsTimeDifferencesGreaterThanShakeThreshold()) {
                    previousDeviceAccelerationWithGravity = currentDeviceAccelerationWithGravity

                    currentDeviceAccelerationWithGravity =
                        sqrt(
                            (xAxisValue * xAxisValue)
                                    + (yAxisValue * yAxisValue)
                                    + (zAxisValue * zAxisValue)
                        )

                    val differenceOfAcceleration =
                        currentDeviceAccelerationWithGravity - previousDeviceAccelerationWithGravity

                    deviceAccelerationWithoutGravity =
                        deviceAccelerationWithoutGravity * 0.9f + differenceOfAcceleration

                    Log.d(TAG, "Device acceleration is $deviceAccelerationWithoutGravity")

                    //Has good amount of acceleration
                    if (deviceAccelerationWithoutGravity > DEVICE_SHAKE_ACCELERATION_SPEED) {
                        if (deviceShakeStartTime == 0L) {
                            //Begin shake
                            deviceShakeStartTime = System.currentTimeMillis()
                            Log.d(TAG, "Shake started at $deviceShakeStartTime")

                            previousXAxisValue = xAxisValue
                            previousYAxisValue = yAxisValue
                        }
                    } else if (deviceShakeStartTime > 0) {
                        deviceShakeEndTime = System.currentTimeMillis()

                        Log.d(TAG, "Shake ended at $deviceShakeEndTime")

                        if (deviceIsShaken(xAxisValue, yAxisValue)) {
                            deviceLastShakenTime = currentDeviceTime

                            Log.d(TAG, "Shake Detected at $currentDeviceTime")

                            showDeviceShakenNotification()

                            //Save device event
                            val deviceEvent =
                                DeviceEvent(
                                    0,
                                    DeviceEventType.SHAKE.name,
                                    getCurrentDateAndTime(),
                                    0.0
                                )
                            saveToDataSource(deviceEvent)
                        }

                        deviceShakeStartTime = 0L
                        deviceShakeEndTime = 0L
                        previousXAxisValue = 0F
                        previousYAxisValue = 0F
                    }
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    private fun deviceIsShaken(xAxisValue: Float, yAxisValue: Float): Boolean {
        val differenceInXAxis = xAxisValue - previousXAxisValue
        val differenceInYAxis = yAxisValue - previousYAxisValue

        Log.d(
            TAG,
            "Sensor shake axis differences Value : X = $differenceInXAxis Y = $differenceInYAxis"
        )

        return (
                //Has no huge single axis variation from its previous readings
                (differenceInXAxis > -20 && differenceInXAxis < 0) &&
                        (differenceInYAxis > -2 && differenceInYAxis < 2)
                )
    }

    private fun deviceFallDetected(fallDuration: Double) {
        if (DETECT_FREQUENT_FALLS && isAFrequentFall()) {
            showFrequentFallNotification()
        } else {
            showFallNotification()

            //Save device event
            val deviceEvent =
                DeviceEvent(
                    0,
                    DeviceEventType.FALL.name,
                    getCurrentDateAndTime(),
                    fallDuration
                )
            saveToDataSource(deviceEvent)
        }
    }

    private fun isAFrequentFall(): Boolean {
        val timeSinceDeviceFellLast = System.currentTimeMillis() - previousDeviceFellTime
        return ((timeSinceDeviceFellLast > MINIMUM_FALL_TIME) && (timeSinceDeviceFellLast < FREQUENT_FALL_TIMEOUT))
    }

    private fun showFallNotification() {
        //Notify user
        Log.d(TAG, "Fall detected")

        //Show a notification
        var message = DetectionMessages.getFallDetectionMessage()
        if (message == null) {
            message = getString(R.string.notification_fall_detected_message)
        }
        val notification = NotificationCompat.Builder(
            this,
            Fallen.NOTIFICATION_CHANNEL_FALL
        )
            .setSmallIcon(R.drawable.ic_fall)
            .setContentTitle(getString(R.string.notification_fall_name))
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
            message = getString(R.string.notification_frequent_fall_detected_message)
        }
        val notification = NotificationCompat.Builder(
            this,
            Fallen.NOTIFICATION_CHANNEL_OTHER
        )
            .setSmallIcon(R.drawable.ic_fall)
            .setContentTitle(getString(R.string.notification_frequent_fall_name))
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .build()

        NotificationManagerCompat.from(this)
            .notify(OTHER_DETECTION_NOTIFICATION_ID, notification)
    }

    private fun showDeviceShakenNotification() {
        //Notify user
        Log.d(TAG, "Shake detected")

        //Show a notification
        var message = DetectionMessages.getShakeDetectionMessage()
        if (message == null) {
            message = getString(R.string.notification_shake_detected_message)
        }
        val notification = NotificationCompat.Builder(
            this,
            Fallen.NOTIFICATION_CHANNEL_OTHER
        )
            .setSmallIcon(R.drawable.ic_shake)
            .setContentTitle(getString(R.string.notification_shaken_name))
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .build()

        NotificationManagerCompat.from(this)
            .notify(OTHER_DETECTION_NOTIFICATION_ID, notification)
    }

    private fun isSensorsTimeDifferencesGreaterThanShakeThreshold() =
        currentDeviceTime - deviceLastShakenTime > ACCELEROMETER_SENSOR_DELAY

    /**
     * Save the data to the data source
     */
    private fun saveToDataSource(deviceEvent: DeviceEvent) {
        deviceEventRepository.saveDeviceEvent(deviceEvent)
    }
//endregion
}
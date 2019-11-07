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
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.uzair.fallen.Fallen
import com.example.uzair.fallen.Fallen.Companion.NOTIFICATION_CHANNEL_SENSOR_LISTENER
import com.example.uzair.fallen.Fallen.Companion.SHOULD_DETECT_FALL
import com.example.uzair.fallen.Fallen.Companion.SHOULD_DETECT_FREQUENT_FALL
import com.example.uzair.fallen.Fallen.Companion.SHOULD_DETECT_SHAKE
import com.example.uzair.fallen.R
import com.example.uzair.fallen.database.model.DeviceEvent
import com.example.uzair.fallen.repository.DeviceEventsRepository
import com.example.uzair.fallen.repository.IDeviceEventsRepository
import com.example.uzair.fallen.util.DeviceEventType
import com.example.uzair.fallen.util.IntentExtras
import com.example.uzair.fallen.util.Log
import com.example.uzair.fallen.util.Util
import kotlin.math.pow
import kotlin.math.sqrt

class EventDetectionService : IntentService(EventDetectionService::class.java.simpleName),
    SensorEventListener {
    companion object {
        private val TAG = EventDetectionService::class.java.simpleName
        private const val WAKE_LOCK_TAG = "FreeFall:WakeLock"
        private const val SENSOR_LISTENER_NOTIFICATION_ID = 1
        private const val FALL_DETECTION_NOTIFICATION_ID = 2
        private const val OTHER_DETECTION_NOTIFICATION_ID = 3

        private const val DEVICE_SHAKE_ACCELERATION_SPEED = 20
        private const val FALL_THRESHOLD = 2.0
        private const val FREQUENT_FALL_TIMEOUT = 5000 // 5 second
        private var ACCELEROMETER_SENSOR_DELAY = 1200//Changing to var for testing
        private var MINIMUM_FALL_TIME =
            0.080//Every fall should be greater than this time, changing to var for testing
    }

    //region Global Variables
    private val deviceEventRepository: IDeviceEventsRepository by lazy {
        DeviceEventsRepository(
            application
        )
    }

    private lateinit var wakeLock: PowerManager.WakeLock
    private val sensorManager: SensorManager by lazy {
        getSystemService(SENSOR_SERVICE) as SensorManager
    }

    //Fall variables
    private var deviceFallStartTime = 0L
    private var deviceFallEndTime = 0L
    private var devicePreviousFellTime = 0L
    private var deviceFallDuration = 0.0

    //Shake variables
    private var deviceShakenStartTime = 0L
    private var deviceShakenEndTime = 0L
    private var devicePreviousShakenTime = 0L

    private var currentDeviceTime = 0L

    private var deviceAccelerationWithoutGravity = 0F
    private var deviceCurrentAccelerationWithGravity = SensorManager.GRAVITY_EARTH
    private var devicePreviousAccelerationWithGravity = SensorManager.GRAVITY_EARTH

    private var previousXAxisValue = 0F
    private var previousYAxisValue = 0F
    //endregion

    override fun onHandleIntent(intent: Intent?) {
        Log.d(TAG, "onHandleIntent Called")
    }

    //region Service implemented methods
    /**
     * Called only once when the service is started.
     *
     * Do all the one time processes that should happen when the service is started.
     */
    override fun onCreate() {
        Log.d(TAG, "Free Fall Service Started")

        val powerManager: PowerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            WAKE_LOCK_TAG
        )

        wakeLock.acquire(10000)
        Log.d(TAG, "Wake lock acquired")

        //Set the sensor listener
        registerAccelerometerSensorToSensorManager()
    }

    /**
     * Called every time a new intent is sent to this service.
     *
     * Do any new task that should be performed on every new intent received.
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val serviceName = intent?.getStringExtra(IntentExtras.SERVICE_NAME.name)
        val serviceDescription = intent?.getStringExtra(IntentExtras.SERVICE_DESCRIPTION.name)

        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_SENSOR_LISTENER)
            .setContentTitle(serviceName)
            .setContentText(serviceDescription)
            .setSmallIcon(R.drawable.ic_service)
            .build()

        startForeground(SENSOR_LISTENER_NOTIFICATION_ID, notification)

        return START_NOT_STICKY
    }

    /**
     * Called which this service is destroyed.
     *
     * Clear/release all the resources here
     */
    override fun onDestroy() {
        wakeLock.release()
        Log.d(TAG, "Wake lock released")

        sensorManager.unregisterListener(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true) //true will remove notification
        }

        Log.d(TAG, "Free Fall Service Destroyed")
    }
    //endregion

    /**
     * Registers the accelerometer sensor to the sensor manager
     */
    private fun registerAccelerometerSensorToSensorManager() {
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

            currentDeviceTime = System.currentTimeMillis()

            //Sensor axis values
            val xAxisValue = event.values[0]
            val yAxisValue = event.values[1]
            val zAxisValue = event.values[2]

            Log.d(TAG, "Sensor Computed Value : X = $xAxisValue Y = $yAxisValue Z = $zAxisValue")

            //Fall detection
            if (SHOULD_DETECT_FALL && checkIfDeviceFell(xAxisValue, yAxisValue, zAxisValue)) {
                deviceFallDetected(deviceFallDuration)
            }

            //Shake detection
            if (SHOULD_DETECT_SHAKE && checkIfDeviceShaken(xAxisValue, yAxisValue, zAxisValue)) {
                //Save device event
                val deviceEvent =
                    DeviceEvent(
                        0,
                        DeviceEventType.SHAKE.name,
                        Util.getCurrentDateAndTime(),
                        0.0
                    )

                saveToDataSource(deviceEvent)
                showDeviceShakenNotification()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d(TAG, "onAccuracyChanged Called")
    }
    //endregion

    //region Check methods for device shake and fall
    /**
     * This method will check if the device fell, if provided with the
     * accelerometer sensor's axis values.
     */
    private fun checkIfDeviceFell(
        xAxisValue: Float,
        yAxisValue: Float,
        zAxisValue: Float
    ): Boolean {
        val rootSquare = sqrt(xAxisValue.pow(2) + yAxisValue.pow(2) + zAxisValue.pow(2))
        Log.d(TAG, "Square root = $rootSquare")

        var didDeviceFell = false

        if (rootSquare < FALL_THRESHOLD) {
            if (deviceFallStartTime == 0L) {
                //Begin fall
                deviceFallStartTime = System.currentTimeMillis()
                Log.d(TAG, "Fall started at $deviceFallStartTime")
            }
        }
        //End fall
        else if (deviceFallStartTime > 0) {
            deviceFallEndTime = System.currentTimeMillis()

            Log.d(TAG, "Fall ended at $deviceFallEndTime")
            deviceFallDuration =
                (deviceFallEndTime - deviceFallStartTime) / 1000.00

            Log.d(TAG, "Fall duration is $deviceFallDuration")

            if (deviceFallDuration > MINIMUM_FALL_TIME)
                didDeviceFell = true

            devicePreviousFellTime = deviceFallEndTime
            deviceFallEndTime = 0L
            deviceFallStartTime = 0L
            deviceFallDuration = 0.0
        }

        return didDeviceFell
    }

    /**
     * This method will check if the device was shaken, if provided with the
     * accelerometer sensor's axis values.
     */
    private fun checkIfDeviceShaken(
        xAxisValue: Float,
        yAxisValue: Float,
        zAxisValue: Float
    ): Boolean {
        var wasDeviceShaken = false

        if (isSensorsTimeDifferencesGreaterThanShakeThreshold()) {
            devicePreviousAccelerationWithGravity = deviceCurrentAccelerationWithGravity

            deviceCurrentAccelerationWithGravity =
                sqrt(
                    (xAxisValue * xAxisValue)
                            + (yAxisValue * yAxisValue)
                            + (zAxisValue * zAxisValue)
                )

            val differenceOfAcceleration =
                deviceCurrentAccelerationWithGravity - devicePreviousAccelerationWithGravity

            deviceAccelerationWithoutGravity =
                deviceAccelerationWithoutGravity * 0.9f + differenceOfAcceleration

            Log.d(TAG, "Device acceleration is $deviceAccelerationWithoutGravity")

            //Has good amount of acceleration
            if (deviceAccelerationWithoutGravity > DEVICE_SHAKE_ACCELERATION_SPEED) {
                if (deviceShakenStartTime == 0L) {
                    //Begin shake
                    deviceShakenStartTime = System.currentTimeMillis()
                    Log.d(TAG, "Shake started at $deviceShakenStartTime")

                    previousXAxisValue = xAxisValue
                    previousYAxisValue = yAxisValue
                }
            } else if (deviceShakenStartTime > 0) {
                deviceShakenEndTime = System.currentTimeMillis()

                Log.d(TAG, "Shake ended at $deviceShakenEndTime")

                devicePreviousShakenTime = currentDeviceTime

                Log.d(TAG, "Shake Detected at $currentDeviceTime")

                wasDeviceShaken = true

                deviceShakenStartTime = 0L
                deviceShakenEndTime = 0L
                previousXAxisValue = 0F
                previousYAxisValue = 0F
            }
        }

        return wasDeviceShaken
    }
    //endregion

    /**
     * Fires the notification methods of either the frequent fall or a normal fall
     */
    private fun deviceFallDetected(fallDuration: Double) {
        if (SHOULD_DETECT_FREQUENT_FALL && isAFrequentFall()) {
            showFrequentFallNotification()
        } else {
            showFallNotification()

            //Save device event
            val deviceEvent =
                DeviceEvent(
                    0,
                    DeviceEventType.FALL.name,
                    Util.getCurrentDateAndTime(),
                    fallDuration
                )
            saveToDataSource(deviceEvent)
        }
    }

    /**
     * This method checks if the device fell again within the frequent fall threshold.
     *
     * If a fall happens within the frequent fall time, in this case 5seconds
     * which cant be realistic as a person cannot fall again within 5seconds, a
     * frequent fall will be fired to the user.
     *
     * returns true if a frequent fall detected, else false
     */
    private fun isAFrequentFall(): Boolean {
        val timeSinceDeviceFellLast = System.currentTimeMillis() - devicePreviousFellTime
        return ((timeSinceDeviceFellLast > MINIMUM_FALL_TIME) && (timeSinceDeviceFellLast < FREQUENT_FALL_TIMEOUT))
    }

    //region Show notifications
    /**
     * Notifies user that the device fell
     */
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

    /**
     * Notifies user about the frequent fall
     */
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

    /**
     * Notifies user that the device was shaken
     */
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
    //endregion

    private fun isSensorsTimeDifferencesGreaterThanShakeThreshold() =
        currentDeviceTime - devicePreviousShakenTime > ACCELEROMETER_SENSOR_DELAY

    /**
     * Save the data to the data source
     */
    private fun saveToDataSource(deviceEvent: DeviceEvent) =
        deviceEventRepository.saveDeviceEvent(deviceEvent)

    //region Public test methods
    /**
     * This method will change the minimum fall time for testing
     */
    fun changeMinimumFallTime() {
        MINIMUM_FALL_TIME = -1.0
    }

    /**
     * This method will change the accelerometer sensor delay time for testing
     */
    fun changeAccelerometerSensorDelay() {
        ACCELEROMETER_SENSOR_DELAY = -1
    }

    /**
     * Method to test if the device fell
     */
    fun testDeviceFall(
        xAxisValue: Float,
        yAxisValue: Float,
        zAxisValue: Float
    ) = checkIfDeviceFell(xAxisValue, yAxisValue, zAxisValue)

    /**
     * Method to test if the device was shaken
     */
    fun testDeviceShaken(
        xAxisValue: Float,
        yAxisValue: Float,
        zAxisValue: Float
    ) = checkIfDeviceShaken(xAxisValue, yAxisValue, zAxisValue)
    //endregion
}
package com.example.oscsensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

class SensorRepository(context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var oscManager: OscManager? = null
    private val activeSensors = mutableSetOf<Int>()
    private val firstEventLoggedSensors = mutableSetOf<Int>()
    private var samplingRateMicroseconds = SensorManager.SENSOR_DELAY_NORMAL
    private var addressPrefix = DEFAULT_ADDRESS_PREFIX

    fun setOscManager(manager: OscManager) {
        this.oscManager = manager
    }

    fun setAddressPrefix(prefix: String) {
        val normalized = prefix.trim().trim('/').ifEmpty { DEFAULT_ADDRESS_PREFIX }
        addressPrefix = normalized
        logDebug("Address prefix set to $addressPrefix")
    }

    fun getAvailableSensors(): List<Sensor> {
        return sensorManager.getSensorList(Sensor.TYPE_ALL)
    }

    fun startSensor(sensorType: Int) {
        val sensor = sensorManager.getDefaultSensor(sensorType)
        if (sensor != null && !activeSensors.contains(sensorType)) {
            val isRegistered = sensorManager.registerListener(this, sensor, samplingRateMicroseconds)
            if (!isRegistered) {
                val fallbackRate = getFallbackSensorDelay(samplingRateMicroseconds)
                val fallbackRegistered = sensorManager.registerListener(this, sensor, fallbackRate)
                logWarn(
                    "registerListener failed for ${sensor.name} at ${samplingRateMicroseconds}us. " +
                        "Fallback rate=$fallbackRate result=$fallbackRegistered"
                )
                if (!fallbackRegistered) {
                    return
                }
            } else {
                logDebug("registerListener success for ${sensor.name} at ${samplingRateMicroseconds}us")
            }
            activeSensors.add(sensorType)
        }
    }

    fun stopSensor(sensorType: Int) {
        val sensor = sensorManager.getDefaultSensor(sensorType)
        if (sensor != null && activeSensors.contains(sensorType)) {
            sensorManager.unregisterListener(this, sensor)
            activeSensors.remove(sensorType)
            firstEventLoggedSensors.remove(sensorType)
        }
    }

    fun stopAll() {
        sensorManager.unregisterListener(this)
        activeSensors.clear()
        firstEventLoggedSensors.clear()
    }

    fun setSamplingRate(rateMicroseconds: Int) {
        this.samplingRateMicroseconds = rateMicroseconds
        logDebug("Sampling rate updated to ${samplingRateMicroseconds}us")
        // Re-register sensors with new rate
        val currentSensors = activeSensors.toList()
        stopAll()
        currentSensors.forEach { startSensor(it) }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (!firstEventLoggedSensors.contains(it.sensor.type)) {
                firstEventLoggedSensors.add(it.sensor.type)
                logDebug("First sensor event received from ${it.sensor.name}")
            }
            val address = "/$addressPrefix/${it.sensor.stringType.replace(".", "/")}"
            val args = it.values.toList()
            oscManager?.send(address, args)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }

    private fun getFallbackSensorDelay(rateMicroseconds: Int): Int {
        return when {
            rateMicroseconds >= 1_000_000 -> SensorManager.SENSOR_DELAY_NORMAL
            rateMicroseconds >= 200_000 -> SensorManager.SENSOR_DELAY_GAME
            else -> SensorManager.SENSOR_DELAY_FASTEST
        }
    }

    private fun logDebug(message: String) {
        if (BuildConfig.ENABLE_VERBOSE_LOGGING) {
            Log.d(TAG, message)
        }
    }

    private fun logWarn(message: String) {
        if (BuildConfig.ENABLE_VERBOSE_LOGGING) {
            Log.w(TAG, message)
        }
    }

    companion object {
        private const val TAG = "SensorRepository"
        private const val DEFAULT_ADDRESS_PREFIX = "zigsim"
    }
}

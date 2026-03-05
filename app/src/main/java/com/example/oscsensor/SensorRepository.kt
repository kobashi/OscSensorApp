package com.example.oscsensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class SensorRepository(context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var oscManager: OscManager? = null
    private val activeSensors = mutableSetOf<Int>()
    private var samplingRateMicroseconds = SensorManager.SENSOR_DELAY_NORMAL

    fun setOscManager(manager: OscManager) {
        this.oscManager = manager
    }

    fun getAvailableSensors(): List<Sensor> {
        return sensorManager.getSensorList(Sensor.TYPE_ALL)
    }

    fun startSensor(sensorType: Int) {
        val sensor = sensorManager.getDefaultSensor(sensorType)
        if (sensor != null && !activeSensors.contains(sensorType)) {
            sensorManager.registerListener(this, sensor, samplingRateMicroseconds)
            activeSensors.add(sensorType)
        }
    }

    fun stopSensor(sensorType: Int) {
        val sensor = sensorManager.getDefaultSensor(sensorType)
        if (sensor != null && activeSensors.contains(sensorType)) {
            sensorManager.unregisterListener(this, sensor)
            activeSensors.remove(sensorType)
        }
    }

    fun stopAll() {
        sensorManager.unregisterListener(this)
        activeSensors.clear()
    }

    fun setSamplingRate(rateMicroseconds: Int) {
        this.samplingRateMicroseconds = rateMicroseconds
        // Re-register sensors with new rate
        val currentSensors = activeSensors.toList()
        stopAll()
        currentSensors.forEach { startSensor(it) }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            val address = "/zigsim/${it.sensor.stringType.replace(".", "/")}"
            val args = it.values.toList()
            oscManager?.send(address, args)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }
}

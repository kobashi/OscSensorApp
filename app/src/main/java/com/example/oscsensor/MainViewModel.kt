package com.example.oscsensor

import android.app.Application
import android.hardware.Sensor
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val oscManager = OscManager()
    private val sensorRepository = SensorRepository(application)

    private val _isRunning = MutableLiveData(false)
    val isRunning: LiveData<Boolean> = _isRunning

    private val _availableSensors = MutableLiveData<List<Sensor>>()
    val availableSensors: LiveData<List<Sensor>> = _availableSensors

    init {
        sensorRepository.setOscManager(oscManager)
        _availableSensors.value = sensorRepository.getAvailableSensors()
    }

    fun toggleStartStop(ip: String, port: String, selectedSensors: Set<Int>, rate: Int) {
        if (_isRunning.value == true) {
            stop()
        } else {
            start(ip, port, selectedSensors, rate)
        }
    }

    private fun start(ip: String, portStr: String, selectedSensors: Set<Int>, rate: Int) {
        try {
            val port = portStr.toInt()
            logDebug("Start requested: ip=$ip port=$port rate=${rate}us sensors=${selectedSensors.size}")
            if (selectedSensors.isEmpty()) {
                logWarn("No sensors selected. No OSC data will be sent.")
            }
            oscManager.connect(ip, port)
            sensorRepository.setSamplingRate(rate)
            selectedSensors.forEach { sensorType ->
                sensorRepository.startSensor(sensorType)
            }
            _isRunning.value = true
        } catch (e: Exception) {
            logError("Failed to start OSC streaming", e)
            e.printStackTrace()
        }
    }

    private fun stop() {
        sensorRepository.stopAll()
        oscManager.close()
        _isRunning.value = false
    }

    override fun onCleared() {
        super.onCleared()
        stop()
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

    private fun logError(message: String, throwable: Throwable) {
        if (BuildConfig.ENABLE_VERBOSE_LOGGING) {
            Log.e(TAG, message, throwable)
        }
    }

    companion object {
        private const val TAG = "MainViewModel"
    }
}

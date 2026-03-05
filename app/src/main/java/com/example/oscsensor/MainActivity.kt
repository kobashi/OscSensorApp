package com.example.oscsensor

import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.CheckBox
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.oscsensor.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private val selectedSensors = mutableSetOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        viewModel.availableSensors.observe(this) { sensors ->
            binding.llSensors.removeAllViews()
            // Filter for common sensors to avoid clutter
            val commonTypes = setOf(
                Sensor.TYPE_ACCELEROMETER,
                Sensor.TYPE_GYROSCOPE,
                Sensor.TYPE_MAGNETIC_FIELD,
                Sensor.TYPE_GRAVITY,
                Sensor.TYPE_LINEAR_ACCELERATION,
                Sensor.TYPE_ROTATION_VECTOR,
                Sensor.TYPE_LIGHT,
                Sensor.TYPE_PRESSURE,
                Sensor.TYPE_PROXIMITY
            )
            
            sensors.filter { commonTypes.contains(it.type) }.distinctBy { it.type }.forEach { sensor ->
                val checkBox = CheckBox(this)
                checkBox.text = sensor.name
                checkBox.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        selectedSensors.add(sensor.type)
                    } else {
                        selectedSensors.remove(sensor.type)
                    }
                }
                binding.llSensors.addView(checkBox)
            }
        }

        viewModel.isRunning.observe(this) { isRunning ->
            if (isRunning) {
                binding.btnStartStop.text = "Stop"
                binding.tvStatus.text = "Running"
                binding.tvStatus.setTextColor(Color.GREEN)
                disableInputs()
            } else {
                binding.btnStartStop.text = "Start"
                binding.tvStatus.text = "Stopped"
                binding.tvStatus.setTextColor(Color.RED)
                enableInputs()
            }
        }
    }

    private fun setupListeners() {
        binding.btnStartStop.setOnClickListener {
            val ip = binding.etIpAddress.text.toString()
            val port = binding.etPort.text.toString()
            
            val rate = when (binding.rgRate.checkedRadioButtonId) {
                R.id.rbGame -> SensorManager.SENSOR_DELAY_GAME
                R.id.rbFastest -> SensorManager.SENSOR_DELAY_FASTEST
                else -> SensorManager.SENSOR_DELAY_NORMAL
            }

            viewModel.toggleStartStop(ip, port, selectedSensors, rate)
        }
    }

    private fun disableInputs() {
        binding.etIpAddress.isEnabled = false
        binding.etPort.isEnabled = false
        binding.rgRate.isEnabled = false
        for (i in 0 until binding.llSensors.childCount) {
            binding.llSensors.getChildAt(i).isEnabled = false
        }
    }

    private fun enableInputs() {
        binding.etIpAddress.isEnabled = true
        binding.etPort.isEnabled = true
        binding.rgRate.isEnabled = true
        for (i in 0 until binding.llSensors.childCount) {
            binding.llSensors.getChildAt(i).isEnabled = true
        }
    }
}

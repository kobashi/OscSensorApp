package com.example.oscsensor

import android.graphics.Color
import android.hardware.Sensor
import android.os.Bundle
import android.widget.CheckBox
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.example.oscsensor.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private val selectedSensors = mutableSetOf<Int>()
    private val settingsPrefs by lazy { getSharedPreferences(PREFS_NAME, MODE_PRIVATE) }

    companion object {
        private const val MICROS_PER_SECOND = 1_000_000
        private const val RATE_1_PER_SEC_US = MICROS_PER_SECOND
        private const val RATE_5_PER_SEC_US = MICROS_PER_SECOND / 5
        private const val RATE_10_PER_SEC_US = MICROS_PER_SECOND / 10
        private const val RATE_20_PER_SEC_US = MICROS_PER_SECOND / 20
        private const val RATE_30_PER_SEC_US = MICROS_PER_SECOND / 30
        private const val RATE_60_PER_SEC_US = MICROS_PER_SECOND / 60

        private const val PREFS_NAME = "connection_settings"
        private const val KEY_IP_ADDRESS = "ip_address"
        private const val KEY_PORT = "port"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        restoreConnectionSettings()
        setupObservers()
        setupListeners()
    }

    private fun restoreConnectionSettings() {
        val defaultIp = binding.etIpAddress.text?.toString().orEmpty()
        val defaultPort = binding.etPort.text?.toString().orEmpty()

        val savedIp = settingsPrefs.getString(KEY_IP_ADDRESS, defaultIp).orEmpty()
        val savedPort = settingsPrefs.getString(KEY_PORT, defaultPort).orEmpty()

        binding.etIpAddress.setText(savedIp)
        binding.etPort.setText(savedPort)
    }

    private fun saveConnectionSettings() {
        settingsPrefs.edit()
            .putString(KEY_IP_ADDRESS, binding.etIpAddress.text?.toString().orEmpty())
            .putString(KEY_PORT, binding.etPort.text?.toString().orEmpty())
            .apply()
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
        binding.etIpAddress.doAfterTextChanged {
            saveConnectionSettings()
        }

        binding.etPort.doAfterTextChanged {
            saveConnectionSettings()
        }

        binding.btnStartStop.setOnClickListener {
            val ip = binding.etIpAddress.text.toString()
            val port = binding.etPort.text.toString()

            // registerListener() uses microseconds when passing explicit delay values.
            val samplingPeriodUs = when (binding.rgRate.checkedRadioButtonId) {
                R.id.rbGame -> RATE_5_PER_SEC_US
                R.id.rbFastest -> RATE_10_PER_SEC_US
                R.id.rb20 -> RATE_20_PER_SEC_US
                R.id.rb30 -> RATE_30_PER_SEC_US
                R.id.rb60 -> RATE_60_PER_SEC_US
                else -> RATE_1_PER_SEC_US
            }

            viewModel.toggleStartStop(ip, port, selectedSensors, samplingPeriodUs)
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

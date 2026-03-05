# OscSensorApp

Android sensors to OSC sender app.

## Overview
OscSensorApp sends selected device sensor values as OSC messages to a target IP and port.
The app is intended for quick sensor streaming to external tools on the same network.

## Features
- Select target `IP Address` and `Port`
- Set OSC address prefix from UI (default: `zigsim`)
- Select sampling rate from quick toggles:
  - `1/s`, `5/s`, `10/s`, `20/s`, `30/s`, `60/s`
- Select which sensors to stream
- Start/Stop streaming with one button
- Connection settings are persisted across app restarts
  - IP address
  - Port
  - OSC prefix

## OSC Message Format
Message address format:
- `/<prefix>/<sensor.stringType with dots replaced by slashes>`

Example:
- Prefix: `zigsim`
- Sensor stringType: `android.sensor.accelerometer`
- OSC address: `/zigsim/android/sensor/accelerometer`

Arguments:
- Sensor values are sent as the raw float value list from Android `SensorEvent.values`

## Requirements
- Android Studio (latest stable recommended)
- Android SDK (compileSdk 34)
- Android device with sensors
- OSC receiver app/tool

## Build
From project root:

```bash
# debug build
./gradlew assembleDebug

# release build
./gradlew assembleRelease
```

Windows:

```powershell
gradlew.bat assembleDebug
gradlew.bat assembleRelease
```

## Run
1. Install and launch the app on device.
2. Enter receiver `IP Address` and `Port`.
3. Set `OSC Prefix` (or keep default `zigsim`).
4. Select sampling rate and sensors.
5. Tap `Start`.
6. Verify OSC messages in your receiver.

## Build Types
- `debug`
  - verbose internal logs enabled (`BuildConfig.ENABLE_VERBOSE_LOGGING = true`)
- `release`
  - verbose logs disabled (`BuildConfig.ENABLE_VERBOSE_LOGGING = false`)

## Troubleshooting
- No OSC received:
  - Check receiver IP/port and firewall settings
  - Confirm both devices are on same network
  - Ensure at least one sensor is selected
- Prefix mismatch:
  - Verify receiver expects your configured `OSC Prefix`
- Settings reset:
  - Reinstalling app clears persisted settings (expected behavior)

## Project Structure
- `app/src/main/java/com/example/oscsensor/MainActivity.kt`
  - UI, input persistence, start/stop actions
- `app/src/main/java/com/example/oscsensor/MainViewModel.kt`
  - Start/stop orchestration
- `app/src/main/java/com/example/oscsensor/SensorRepository.kt`
  - Sensor registration and OSC address generation
- `app/src/main/java/com/example/oscsensor/OscManager.kt`
  - OSC socket and send handling
- `app/src/main/res/layout/activity_main.xml`
  - Main screen UI

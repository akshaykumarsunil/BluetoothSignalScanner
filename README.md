# Bluetooth Signal Scanner (KMP)

A Kotlin Multiplatform (KMP) library targeting **Android** and **iOS** for scanning and decoding Bluetooth Low Energy (BLE) advertisements.

The library captures BLE advertisements over a configurable scan window (default: 12 seconds) and parses received payloads into strongly-typed, human-readable signal models.

---

## Features

- **Multi-Format Advertisement Parsing**:
  - **Bluetooth SIG Standard (GAP)**: Decodes flags, discovery modes, appearance categories (sensors, watches, thermometers, etc.), 16-bit Company Identifiers, and 128-bit Service UUIDs into human-readable strings.
  - **Eddystone**: Decodes **UID** (Namespace & Instance ID), **URL** (with scheme decoding & suffix decompression), and **TLM** (battery voltage, beacon temperature, advertisement count, and uptime).
  - **AltBeacon**: Decodes beacon identifier (Beacon ID), reference RSSI at 1 meter, and reserved byte.
- **Configurable Scan Duration**: Specify scan duration in seconds (defaults to 12s).
- **Clean Architecture & SOLID Design**: Fully separated Domain, Parser, and Platform Scanner layers with 0 magic numbers.
- **Modern Platform Support**:
  - Android 10+ (API 29+)
  - iOS 18+ (via Apple CoreBluetooth)

---

## Project Structure

```
BluetoothSignalScanner/
├── library/
│   ├── src/
│   │   ├── commonMain/        # Shared domain models (BleSignal) & advertisement parsers
│   │   ├── commonTest/        # Unit tests for protocol decoders & byte parsers
│   │   ├── androidMain/       # Android BluetoothLeScanner implementation
│   │   └── iosMain/           # iOS CBCentralManager implementation
│   └── build.gradle.kts
├── scripts/
│   └── build-artifacts.sh     # Single-command build script for both platforms
├── .github/
│   └── workflows/
│       └── build-artifacts.yml# CI/CD workflow (runs on macos-14)
└── README.md
```

---

## Building the Artifacts

Both the Android `.aar` and iOS `.xcframework` can be compiled together from a macOS environment:

```bash
# Using the build script
./scripts/build-artifacts.sh

# Or directly via Gradle
./gradlew :library:bundleAndroidMainAar :library:assembleBleSignalScannerReleaseXCFramework
```

### Artifact Outputs

| Platform | Output Artifact Location |
| :--- | :--- |
| **Android** | `library/build/outputs/aar/library.aar` |
| **iOS** | `library/build/XCFrameworks/release/BleSignalScanner.xcframework` |

---

## Native Usage

### Android (Kotlin)

```kotlin
import com.kytrox.blescanner.scanner.BleScannerFactory

val scanner = BleScannerFactory(context).create()

// Runs scan for 12 seconds (suspend function)
val signals = scanner.scan(durationSeconds = 12)

signals.forEach { signal ->
    when (signal) {
        is BleSignal.Eddystone.Url -> println("Eddystone URL: ${signal.url}")
        is BleSignal.Eddystone.Tlm -> println("Temperature: ${signal.temperatureCelsius}°C")
        is BleSignal.AltBeacon -> println("AltBeacon ID: ${signal.beaconId}")
        is BleSignal.Sig -> println("SIG Device: ${signal.deviceName} (${signal.companyName})")
        else -> println("Signal: ${signal.deviceAddress}")
    }
}
```

### iOS (Swift)

```swift
import BleSignalScanner

let scanner = BleScannerFactory().create()

// Asynchronous scan call
Task {
    let signals = try await scanner.scan(durationSeconds: 12)
    for signal in signals {
        print("Scanned device: \(signal.deviceAddress), RSSI: \(signal.rssi)")
    }
}
```

---

## Using in a Flutter Application

Because Flutter applications are written in Dart, this native KMP library is integrated via a **Flutter Plugin** using Flutter's `MethodChannel`.

```
┌────────────────────────────────────────────────────────┐
│                   Flutter App (Dart)                   │
└───────────────────────────┬────────────────────────────┘
                            │ MethodChannel
         ┌──────────────────┴──────────────────┐
         ▼                                     ▼
┌─────────────────────────┐         ┌────────────────────┐
│ Flutter Android Plugin  │         │ Flutter iOS Plugin │
│   (Consumes .aar)       │         │ (Consumes .xcfwk)  │
└─────────────────────────┘         └────────────────────┘
```

### Step 1: Create a Flutter Plugin Wrapper

Generate a Flutter plugin package:

```bash
flutter create --template=plugin --platforms=android,ios ble_scanner_flutter
```

---

### Step 2: Configure Android Side

1. Copy `library-release.aar` to `ble_scanner_flutter/android/libs/library-release.aar`.
2. In `ble_scanner_flutter/android/build.gradle`:

```groovy
dependencies {
    implementation fileTree(dir: 'libs', include: ['*.aar'])
    implementation "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1"
    implementation "org.jetbrains.kotlinx:kotlinx-datetime:0.6.2"
}
```

3. In `ble_scanner_flutter/android/src/main/kotlin/.../BleScannerFlutterPlugin.kt`:

```kotlin
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.kytrox.blescanner.scanner.BleScannerFactory
import com.kytrox.blescanner.domain.BleSignal

class BleScannerFlutterPlugin: FlutterPlugin, MethodCallHandler {
    private lateinit var channel: MethodChannel
    private lateinit var context: android.content.Context
    private val scope = CoroutineScope(Dispatchers.Main)

    override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(binding.binaryMessenger, "com.kytrox.blescanner")
        channel.setMethodCallHandler(this)
        context = binding.applicationContext
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        if (call.method == "scanSignals") {
            val duration = call.argument<Int>("durationSeconds") ?: 12
            scope.launch {
                try {
                    val scanner = BleScannerFactory(context).create()
                    val signals = scanner.scan(duration)
                    val serialized = signals.map { mapSignalToMap(it) }
                    result.success(serialized)
                } catch (e: Exception) {
                    result.error("SCAN_ERROR", e.message, null)
                }
            }
        } else {
            result.notImplemented()
        }
    }

    private fun mapSignalToMap(signal: BleSignal): Map<String, Any?> = mapOf(
        "deviceAddress" to signal.deviceAddress,
        "rssi" to signal.rssi,
        "type" to when (signal) {
            is BleSignal.Eddystone.Url -> "EddystoneUrl"
            is BleSignal.Eddystone.Uid -> "EddystoneUid"
            is BleSignal.Eddystone.Tlm -> "EddystoneTlm"
            is BleSignal.AltBeacon -> "AltBeacon"
            is BleSignal.Sig -> "SIG"
            else -> "Unknown"
        }
    )

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }
}
```

---

### Step 3: Configure iOS Side

1. Copy `BleSignalScanner.xcframework` to `ble_scanner_flutter/ios/Frameworks/BleSignalScanner.xcframework`.
2. In `ble_scanner_flutter/ios/ble_scanner_flutter.podspec`:

```ruby
Pod::Spec.new do |s|
  s.name             = 'ble_scanner_flutter'
  s.version          = '0.0.1'
  s.summary          = 'Flutter plugin for BleSignalScanner'
  s.source_files     = 'Classes/**/*'
  s.dependency 'Flutter'
  s.platform         = :ios, '18.0'
  s.vendored_frameworks = 'Frameworks/BleSignalScanner.xcframework'
end
```

3. In `ble_scanner_flutter/ios/Classes/BleScannerFlutterPlugin.swift`:

```swift
import Flutter
import UIKit
import BleSignalScanner

public class BleScannerFlutterPlugin: NSObject, FlutterPlugin {
    public static func register(with registrar: FlutterPluginRegistrar) {
        let channel = FlutterMethodChannel(name: "com.kytrox.blescanner", binaryMessenger: registrar.messenger())
        let instance = BleScannerFlutterPlugin()
        registrar.addMethodCallDelegate(instance, channel: channel)
    }

    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        if call.method == "scanSignals" {
            let args = call.arguments as? [String: Any]
            let duration = args?["durationSeconds"] as? Int32 ?? 12
            
            Task {
                do {
                    let scanner = BleScannerFactory().create()
                    let signals = try await scanner.scan(durationSeconds: duration)
                    let mapped = signals.map { signal in
                        [
                            "deviceAddress": signal.deviceAddress,
                            "rssi": signal.rssi
                        ]
                    }
                    result(mapped)
                } catch {
                    result(FlutterError(code: "SCAN_ERROR", message: error.localizedDescription, details: nil))
                }
            }
        } else {
            result(FlutterMethodNotImplemented)
        }
    }
}
```

---

### Step 4: Dart Client Usage

In your Flutter app (`lib/main.dart`):

```dart
import 'package:flutter/services.dart';

class BleScannerClient {
  static const MethodChannel _channel = MethodChannel('com.kytrox.blescanner');

  static Future<List<Map<String, dynamic>>> scanSignals({int durationSeconds = 12}) async {
    final List<dynamic>? results = await _channel.invokeMethod(
      'scanSignals',
      {'durationSeconds': durationSeconds},
    );
    
    if (results == null) return [];
    return results.map((e) => Map<String, dynamic>.from(e as Map)).toList();
  }
}

// Example invocation in UI:
void onScanButtonPressed() async {
  final signals = await BleScannerClient.scanSignals(durationSeconds: 12);
  for (var signal in signals) {
    print('Found device: ${signal['deviceAddress']} RSSI: ${signal['rssi']}');
  }
}
```

---

## CI/CD Pipeline

A GitHub Actions workflow is located at [`.github/workflows/build-artifacts.yml`](.github/workflows/build-artifacts.yml).

- **Environment**: macOS runner (`macos-14`)
- **Triggers**: Push to `main`, pull requests, and manual triggers (`workflow_dispatch`).
- **Jobs**:
  1. Sets up JDK 17 and Android SDK.
  2. Runs unit tests (`:library:testReleaseUnitTest`).
  3. Executes [`scripts/build-artifacts.sh`](scripts/build-artifacts.sh).
  4. Automatically uploads the resulting `.aar` and `.xcframework` as downloadable artifacts on GitHub.

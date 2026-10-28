# Publishing and CI

Contents:
- Choosing a distribution route
- Maven (Kotlin, KMP and Android consumers)
- XCFramework + Swift Package Manager (Swift consumers)
- GitHub Actions example (Latest Stable JDK + Multi-Artifact CI)
- Versioning and release checklist

## Choosing a distribution route

| Consumer | Route |
|---|---|
| Android app (Kotlin/Gradle) | Direct `:library` module dependency (in-repo) or Maven artifact (`.aar`) |
| KMP app (Android + iOS) | Maven artifact, same coordinates. Gradle picks the right target. |
| Native iOS app (Swift, Xcode) | Direct `XCFramework` symlink + build phase (in-repo) or Swift Package Manager binary target |
| Flutter app (Android + iOS) | Encapsulated `:library` Gradle subproject on Android + Podspec `s.script_phase` & `s.vendored_frameworks` symlink on iOS |

Many repositories build and publish both the KMP library artifacts (`.aar` and `.xcframework`) and the consumer app artifacts (`.apk` and `.app`).

## Maven

The Kotlin Gradle plugin creates one publication per target plus a root publication that carries the metadata. Consumers depend on the root coordinates only. With the Android-KMP plugin (`com.android.kotlin.multiplatform.library`) there are no Android-specific publishing steps beyond the standard KMP setup (see kotlinlang.org/docs/multiplatform/multiplatform-publish-lib-setup.html).

Two common routes:

1. **`maven-publish` plus `signing`**, configured by hand. Most control, most boilerplate.
2. **A publishing plugin** such as `com.vanniktech.maven.publish`, which handles POM, signing and Central upload:

```kotlin
plugins {
    alias(libs.plugins.mavenPublish)   // add the latest stable version to the catalog
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
    coordinates("com.example", "mylib", "0.1.0")
    pom {
        name.set("MyLib")
        description.set("What the library does.")
        url.set("https://github.com/example/mylib")
        licenses { license { name.set("Apache-2.0"); url.set("https://www.apache.org/licenses/LICENSE-2.0") } }
        developers { developer { id.set("you"); name.set("Your Name") } }
        scm { url.set("https://github.com/example/mylib") }
    }
}
```

Secrets (signing keys, Central credentials) go in CI secrets or `~/.gradle/gradle.properties`, never in the repo.

Publishing the Apple targets requires macOS. Publish from a macOS runner so the iOS klibs are included, otherwise the published library will be missing iOS.

## XCFramework + Swift Package Manager

1. Build (on macOS): `./gradlew :library:assembleMyLibReleaseXCFramework`. Use `./gradlew tasks --group=build` to see the exact task name. Output is in `library/build/XCFrameworks/release/MyLib.xcframework`.
2. Zip it: `cd library/build/XCFrameworks/release && zip -r MyLib.xcframework.zip MyLib.xcframework`.
3. Checksum: `swift package compute-checksum MyLib.xcframework.zip`.
4. Upload the zip to a stable direct-download URL, for example a GitHub Release asset.
5. Write `Package.swift` in a Git repository (ideally separate from the Kotlin code, so the Swift and Kotlin versions can differ), using the latest stable `swift-tools-version` and target platform version:

```swift
// swift-tools-version:<latest-stable-swift-tools-version>
import PackageDescription

let package = Package(
    name: "MyLib",
    platforms: [.iOS(/* <minimum supported iOS version> */)],
    products: [.library(name: "MyLib", targets: ["MyLib"])],
    targets: [
        .binaryTarget(
            name: "MyLib",
            url: "https://github.com/example/mylib/releases/download/0.1.0/MyLib.xcframework.zip",
            checksum: "<output of compute-checksum>"
        )
    ]
)
```

6. Validate: `swift package reset && swift package show-dependencies --format json`.
7. Push and tag the repository with the semantic version. Swift Package Manager versions come from Git tags.

## GitHub Actions example (Latest Stable JDK + Multi-Artifact CI)

Always use the **latest stable JDK version** (matching `jvmToolchain` in Gradle) and the **latest stable major versions** of all GitHub Actions:

```yaml
name: Build Multiplatform Artifacts & Flutter App

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]
  workflow_dispatch:

jobs:
  build:
    name: Build KMP & Flutter Artifacts
    runs-on: macos-latest

    steps:
      - name: Checkout repository
        uses: actions/checkout@<latest-stable-major-version>

      - name: Set up latest stable JDK
        uses: actions/setup-java@<latest-stable-major-version>
        with:
          distribution: 'temurin'
          java-version: '<latest-stable-jdk-version>'

      - name: Set up Android SDK
        uses: android-actions/setup-android@<latest-stable-major-version>
        with:
          packages: 'platform-tools'

      - name: Set up Flutter
        uses: subosito/flutter-action@<latest-stable-major-version>
        with:
          channel: 'stable'

      - name: Run KMP Library Unit Tests
        run: ./gradlew :library:allTests

      - name: Analyze & Test Consumer App
        working-directory: BluetoothSignalScannerApp
        run: |
          flutter pub get
          flutter analyze
          flutter test

      - name: Build All 4 Artifacts (KMP AAR, KMP XCFramework, Flutter APK, Flutter iOS App)
        run: ./scripts/build-artifacts.sh

      - name: Upload 1/4 — KMP Android AAR
        if: github.ref == 'refs/heads/main'
        uses: actions/upload-artifact@<latest-stable-major-version>
        with:
          name: blescanner-kmp-android-aar
          path: library/build/outputs/aar/*.aar
          if-no-files-found: error

      - name: Upload 2/4 — KMP iOS XCFramework
        if: github.ref == 'refs/heads/main'
        uses: actions/upload-artifact@<latest-stable-major-version>
        with:
          name: blescanner-kmp-ios-xcframework
          path: library/build/XCFrameworks/release/BleSignalScanner.xcframework
          if-no-files-found: error

      - name: Upload 3/4 — Flutter Android APK
        if: github.ref == 'refs/heads/main'
        uses: actions/upload-artifact@<latest-stable-major-version>
        with:
          name: blescanner-flutter-android-apk
          path: BluetoothSignalScannerApp/build/app/outputs/flutter-apk/app-release.apk
          if-no-files-found: error

      - name: Upload 4/4 — Flutter iOS App (.app)
        if: github.ref == 'refs/heads/main'
        uses: actions/upload-artifact@<latest-stable-major-version>
        with:
          name: blescanner-flutter-ios-app
          path: BluetoothSignalScannerApp/build/ios/iphoneos/Runner.app
          if-no-files-found: error
```

Notes:
- Both the KMP library and any sibling consumer app must compile under the **same latest stable JDK version** and **latest stable Gradle version**.
- The Xcode version on the macOS runner must be compatible with the chosen latest stable Kotlin version.
- Optionally cache `~/.konan` (Kotlin/Native toolchain) using the latest stable `actions/cache` version to speed up repeated builds.

## Versioning and release checklist

1. Version numbers follow semver. A change to the public API that breaks Swift or Kotlin callers is a major bump.
2. Run the API/ABI check and read the diff.
3. Update the changelog.
4. Run all tests on macOS (Android + iOS + consumer app tests).
5. Build or publish all release artifacts (`.aar`, `.xcframework`, `.apk`, `.app`).
6. Update `Package.swift` (URL and checksum) if distributing via SwiftPM, then tag the release.

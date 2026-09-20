# Publishing and CI

Contents:
- Choosing a distribution route
- Maven (Kotlin, KMP and Android consumers)
- XCFramework + Swift Package Manager (Swift consumers)
- GitHub Actions example
- Versioning and release checklist

## Choosing a distribution route

| Consumer | Route |
|---|---|
| Android app (Kotlin/Gradle) | Maven artifact (the Android target publishes an AAR) |
| KMP app (Android + iOS) | Maven artifact, same coordinates. Gradle picks the right target. |
| Native iOS app (Swift, Xcode) | XCFramework in a Swift package |

Many libraries need both: Maven for Android/KMP consumers and an XCFramework for Swift-only apps.

## Maven

The Kotlin Gradle plugin creates one publication per target plus a root publication that carries the metadata. Consumers depend on the root coordinates only. With the Android-KMP plugin there are no Android-specific publishing steps beyond the standard KMP setup (see kotlinlang.org/docs/multiplatform/multiplatform-publish-lib-setup.html).

Two common routes:

1. **`maven-publish` plus `signing`**, configured by hand. Most control, most boilerplate.
2. **A publishing plugin** such as `com.vanniktech.maven.publish`, which handles POM, signing and Central upload:

```kotlin
plugins {
    alias(libs.plugins.mavenPublish)   // add to the catalog after looking up the version
}

mavenPublishing {
    publishToMavenCentral()            // check the plugin docs: this API has changed between versions
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

1. Build (on macOS): `./gradlew :library:assembleMyLibXCFramework`. Use `./gradlew tasks --group=build` to see the exact task name. Output is in `library/build/XCFrameworks/release/MyLib.xcframework`.
2. Zip it: `cd library/build/XCFrameworks/release && zip -r MyLib.xcframework.zip MyLib.xcframework`.
3. Checksum: `swift package compute-checksum MyLib.xcframework.zip`.
4. Upload the zip to a stable direct-download URL, for example a GitHub Release asset.
5. Write `Package.swift` in a Git repository (ideally separate from the Kotlin code, so the Swift and Kotlin versions can differ):

```swift
// swift-tools-version:5.9
import PackageDescription

let package = Package(
    name: "MyLib",
    platforms: [.iOS(.v15)],
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

Newer Kotlin releases can generate a `Package.swift` next to the XCFramework when the project consumes SwiftPM dependencies. Check the current docs (kotlinlang.org/docs/multiplatform/multiplatform-spm-export.html) before relying on it, and set `.iOS(...)` to match your real minimum version.

## GitHub Actions example

Two jobs. Android checks run on Linux (cheaper); iOS work needs macOS. Check for newer major versions of each action before using.

```yaml
name: CI
on:
  push: { branches: [main] }
  pull_request:

jobs:
  android:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { distribution: temurin, java-version: 17 }   # match your Gradle/AGP requirement
      - uses: gradle/actions/setup-gradle@v4
      - run: ./gradlew :library:testAndroidHostTest          # confirm the task name with ./gradlew tasks

  ios:
    runs-on: macos-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { distribution: temurin, java-version: 17 }
      - uses: gradle/actions/setup-gradle@v4
      - uses: actions/cache@v4
        with:
          path: ~/.konan
          key: konan-${{ runner.os }}-${{ hashFiles('gradle/libs.versions.toml') }}
      - run: ./gradlew :library:iosSimulatorArm64Test
      - run: ./gradlew :library:assembleMyLibXCFramework
```

Notes:
- macOS runners are slower and more expensive than Linux ones. Keep the iOS job lean and run it on pull requests only if the budget allows.
- The Xcode version on the runner must be compatible with your Kotlin version. If builds start failing after a runner image update, check the Kotlin/Xcode compatibility table.
- Cache `~/.konan` (Kotlin/Native toolchain) to avoid a large download on every run.

## Versioning and release checklist

1. Version numbers follow semver. A change to the public API that breaks Swift or Kotlin callers is a major bump.
2. Run the API/ABI check and read the diff.
3. Update the changelog.
4. Run all tests on macOS (Android + iOS).
5. Publish to Maven from macOS. Build, zip and upload the XCFramework.
6. Update `Package.swift` (URL and checksum), then tag the release.
7. Confirm from a clean sample project: one Gradle dependency, one Swift package.

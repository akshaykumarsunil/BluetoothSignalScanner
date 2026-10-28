# Build configuration templates

Templates for an Android + iOS KMP library using the Android-KMP library plugin.
**Always resolve and use the latest stable build versions** for the JDK, Gradle wrapper, Kotlin, AGP, SKIE, and all libraries/plugins, and verify their compatibility tables before use (see rule 3 in [SKILL.md](../SKILL.md)).

Contents:
- Gradle wrapper (`gradle-wrapper.properties`)
- Version catalog (`gradle/libs.versions.toml`)
- `settings.gradle.kts`
- Root `build.gradle.kts`
- `gradle.properties`
- Library module `build.gradle.kts`
- Notes on the Android block & JDK alignment
- Optional: SKIE
- Sibling Consumer App Integration (Flutter / Android / iOS)

## Gradle wrapper: `gradle/wrapper/gradle-wrapper.properties`

```properties
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-<latest-stable-gradle-version>-bin.zip
networkTimeout=10000
validateDistributionUrl=true
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
```

## Version catalog: `gradle/libs.versions.toml`

```toml
[versions]
agp = "<latest stable AGP version>"
kotlin = "<latest stable Kotlin version>"
coroutines = "<latest stable kotlinx-coroutines version>"
serialization = "<latest stable kotlinx-serialization version>"
datetime = "<latest stable kotlinx-datetime version>"
ktor = "<latest stable Ktor version>"
skie = "<latest stable SKIE version compatible with Kotlin>"

[libraries]
kotlin-test = { module = "org.jetbrains.kotlin:kotlin-test", version.ref = "kotlin" }
kotlinx-coroutines-core = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-core", version.ref = "coroutines" }
kotlinx-coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "coroutines" }
kotlinx-serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "serialization" }
kotlinx-datetime = { module = "org.jetbrains.kotlinx:kotlinx-datetime", version.ref = "datetime" }
ktor-client-core = { module = "io.ktor:ktor-client-core", version.ref = "ktor" }
ktor-client-content-negotiation = { module = "io.ktor:ktor-client-content-negotiation", version.ref = "ktor" }
ktor-serialization-kotlinx-json = { module = "io.ktor:ktor-serialization-kotlinx-json", version.ref = "ktor" }
ktor-client-okhttp = { module = "io.ktor:ktor-client-okhttp", version.ref = "ktor" }
ktor-client-darwin = { module = "io.ktor:ktor-client-darwin", version.ref = "ktor" }
ktor-client-mock = { module = "io.ktor:ktor-client-mock", version.ref = "ktor" }

[plugins]
androidKmpLibrary = { id = "com.android.kotlin.multiplatform.library", version.ref = "agp" }
kotlinMultiplatform = { id = "org.jetbrains.kotlin.multiplatform", version.ref = "kotlin" }
kotlinSerialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
skie = { id = "co.touchlab.skie", version.ref = "skie" }
```

Only include the libraries the module really needs. Remove the Ktor and serialization lines for a pure-logic or hardware-scanner library.

## `settings.gradle.kts`

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "my-lib"
include(":library")
```

## Root `build.gradle.kts`

```kotlin
plugins {
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.androidKmpLibrary) apply false
    alias(libs.plugins.skie) apply false
}
```

## `gradle.properties`

```properties
org.gradle.jvmargs=-Xmx4g -Dfile.encoding=UTF-8
kotlin.daemon.jvmargs=-Xmx3g
org.gradle.caching=true
org.gradle.configuration-cache=true
kotlin.code.style=official
```

Add `android.useAndroidX=true` only if the module depends on AndroidX libraries and the build asks for it.

## Library module: `library/build.gradle.kts`

```kotlin
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKmpLibrary)
    alias(libs.plugins.skie)
}

group = "com.example"
version = "0.1.0"

kotlin {
    explicitApi()
    jvmToolchain(/* <latest stable JDK version> */)

    // ---- Android ----
    android {
        namespace = "com.example.mylib"
        compileSdk = /* <latest stable Android API level> */
        minSdk = /* <minimum supported Android API level> */

        // Opt in: the new plugin disables tests by default.
        withHostTest { }
        // withDeviceTest { instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner" }
    }

    // ---- iOS (device + Apple Silicon simulator) ----
    val frameworkName = "MyLib"          // what Swift code will `import`
    val xcf = XCFramework(frameworkName)

    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = frameworkName
            isStatic = true                                   // recommended for SwiftPM
            binaryOption("bundleId", "com.example.mylib")     // unique CFBundleIdentifier
            xcf.add(this)
            // export(project(":other-module"))               // only if its types are in your public API
        }
    }

    // `iosMain` / `iosTest` are created automatically (default hierarchy template).
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            // Use api(...) instead of implementation(...) for anything that appears in the public API.
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
```

## Notes on the Android block & JDK alignment

- Inside `kotlin { }` the block is called `android { }` for modern AGP releases (`com.android.kotlin.multiplatform.library`). Older AGP versions used `androidLibrary { }`, which is deprecated.
- Always configure the **latest stable JDK version** via `kotlin { jvmToolchain(...) }` across both the KMP library and any sibling consumer applications so both projects compile with the exact same Java toolchain.
- There is no top-level `android { }` extension, no build types and no product flavours in the KMP module. The plugin produces a single variant.
- Source set names are `androidMain`, `androidHostTest` and `androidDeviceTest`. Move any old `src/main`, `src/test` and `src/androidTest` content there.
- Opt-in features:
  - Android resources: `androidResources { enable = true }` (files go in `src/androidMain/res`).
  - Java sources: `withJava()`.
  - Consumer keep rules: `optimization { consumerKeepRules.apply { publish = true; file("consumer-rules.pro") } }`.
- Not supported: BuildConfig (use a generator such as BuildKonfig), data binding and view binding, `externalNativeBuild`. If native C/C++ or variants are essential, put them in a separate `com.android.library` module and depend on it from `androidMain`.

## Optional: SKIE (nicer Swift API)

```kotlin
plugins {
    alias(libs.plugins.skie)
}

skie {
    // Defaults are fine to start. Tune features later if needed.
}
```

Before upgrading SKIE or Kotlin:
1. Confirm the latest stable Kotlin version and latest stable SKIE version are compatible.
2. Note that SKIE generates `.apinotes` that restore Swift keywords stripped with trailing underscores in Objective-C headers (e.g. `namespace_` in Objective-C is accessed as `.namespace` in Swift).

## Sibling Consumer App Integration (Flutter / Android / iOS)

When a sibling Flutter or Android app resides in the same repository and consumes `:library` directly via module encapsulation:
1. **Share the Version Catalog**: Reference `../../gradle/libs.versions.toml` in the consumer app's `settings.gradle.kts` (`versionCatalogs { create("libs") { from(files("../../gradle/libs.versions.toml")) } }`) so Kotlin, Coroutines, Datetime, and other shared libraries use the exact same latest stable versions.
2. **Align Gradle & JDK Toolchain**:
   - Upgrade the consumer app's `gradle-wrapper.properties` to match the KMP project's latest stable Gradle distribution.
   - Set `kotlin { jvmToolchain(<latest-stable-jdk>) }` in both `:app` and `:library` so neither project falls back to an older Java version.
   - If the consumer framework's Android Gradle plugin bundles a D8 dexer with a lower maximum bytecode class version than the latest JDK toolchain, keep `jvmToolchain(<latest-stable-jdk>)` and set `compilerOptions { jvmTarget.set(...) }` / `compileOptions` to the highest bytecode target supported by that D8 dexer.
3. **Dart / Flutter Package Versions**:
   - Always resolve and use the latest stable versions of all `pubspec.yaml` dependencies and lint packages compatible with the installed Flutter/Dart SDK.

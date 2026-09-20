# Build configuration templates

Templates for an Android + iOS KMP library using the Android-KMP library plugin.
**Every version below is a placeholder.** Look up current stable versions and check the compatibility tables before use (see rule 3 in SKILL.md).

Contents:
- Version catalog
- `settings.gradle.kts`
- Root `build.gradle.kts`
- `gradle.properties`
- Library module `build.gradle.kts`
- Notes on the Android block
- Optional: SKIE

## Version catalog: `gradle/libs.versions.toml`

```toml
[versions]
kotlin = "<latest stable Kotlin>"
agp = "<latest stable AGP, must be 8.10 or newer for this plugin>"
coroutines = "<latest>"
serialization = "<latest>"
datetime = "<latest>"
ktor = "<latest>"
# skie = "<latest SKIE that supports the chosen Kotlin version>"

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
kotlinMultiplatform = { id = "org.jetbrains.kotlin.multiplatform", version.ref = "kotlin" }
androidKmpLibrary = { id = "com.android.kotlin.multiplatform.library", version.ref = "agp" }
kotlinSerialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
# skie = { id = "co.touchlab.skie", version.ref = "skie" }
```

Only include the libraries the library really needs. Remove the Ktor and serialization lines for a pure-logic library.

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
    alias(libs.plugins.kotlinSerialization) apply false
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
    alias(libs.plugins.kotlinSerialization)   // remove if unused
    // alias(libs.plugins.skie)               // optional, see below
}

group = "com.example"
version = "0.1.0"

kotlin {
    explicitApi()

    // ---- Android ----
    android {
        namespace = "com.example.mylib"
        compileSdk = 36        // use the latest stable API level
        minSdk = 24

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
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            // Use api(...) instead of implementation(...) for anything that appears in the public API.
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.ktor.client.mock)
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}
```

## Notes on the Android block

- Inside `kotlin { }` the block is called `android { }` for AGP 8.12 and newer. Older AGP versions (below 8.12) use `androidLibrary { }`. `androidLibrary { }` is deprecated in newer AGP.
- There is no top-level `android { }` extension, no build types and no product flavours. The plugin produces a single variant.
- Source set names are `androidMain`, `androidHostTest` and `androidDeviceTest`. Move any old `src/main`, `src/test` and `src/androidTest` content there.
- Opt-in features:
  - Android resources: `androidResources { enable = true }` (files go in `src/androidMain/res`).
  - Java sources: `withJava()`.
  - Consumer keep rules: `optimization { consumerKeepRules.apply { publish = true; file("consumer-rules.pro") } }`.
- Not supported: BuildConfig (use a generator such as BuildKonfig), data binding and view binding, `externalNativeBuild`. If native C/C++ or variants are essential, put them in a separate `com.android.library` module and depend on it from `androidMain`.
- Set the JVM target with `kotlin { jvmToolchain(17) }` (or a newer LTS) for a consistent baseline, or per target inside `android { compilerOptions { jvmTarget.set(...) } }`.
- To add a tooling-only dependency (such as Compose preview tooling) without publishing it, use `dependencies { "androidRuntimeClasspath"(...) }`.

If the exact DSL differs from the above, trust the current docs: developer.android.com/kotlin/multiplatform/plugin.

## Optional: SKIE (nicer Swift API)

```kotlin
plugins {
    alias(libs.plugins.skie)
}

skie {
    // Defaults are fine to start. Tune features later if needed.
}
```

Before adding SKIE:
1. Read skie.touchlab.co/intro and confirm the chosen Kotlin version is supported.
2. If not, either pick a Kotlin version SKIE supports or skip SKIE and use the wrapper approach in `ios-interop.md`.

JetBrains also publishes agent skills for related build tasks, for example an AGP 9 migration skill in the `Kotlin/kotlin-agent-skills` GitHub repository. They can be used alongside this skill.

import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKmpLibrary)
    alias(libs.plugins.skie)
}

group = "com.kytrox"
version = "0.1.0"

kotlin {
    explicitApi()
    jvmToolchain(17)

    // ---- Android ----
    android {
        namespace = "com.kytrox.blescanner"
        compileSdk = 36
        minSdk = 29

        withHostTest { }
    }

    // ---- iOS (device + Apple Silicon simulator) ----
    val frameworkName = "BleSignalScanner"
    val xcf = XCFramework(frameworkName)

    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = frameworkName
            isStatic = true
            binaryOption("bundleId", "com.kytrox.blescanner")
            xcf.add(this)
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

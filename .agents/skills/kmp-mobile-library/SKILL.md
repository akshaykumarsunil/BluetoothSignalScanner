---
name: kmp-mobile-library
description: Create, structure, build and publish Kotlin Multiplatform (KMP) libraries that target Android and iOS only, with no JVM, desktop, web, Wasm or Windows targets. Use this skill whenever the user wants to share Kotlin code between an Android app and an iOS app, start or extend a KMP/KMM library, shared module or SDK, expose Kotlin APIs to Swift, produce an XCFramework or Swift Package from Kotlin, publish a KMP library to Maven, set up expect/actual, or fix KMP Gradle, Kotlin/Native or Swift-interop problems, even if the user never says "Kotlin Multiplatform". Not for Compose Multiplatform UI apps or non-mobile targets.
---

# Kotlin Multiplatform library for Android + iOS

Build a reusable **library** (not an app) whose shared code runs on Android and iOS. Everything here assumes those two targets and nothing else. That keeps the build simple and the public API predictable.

## Ground rules

1. **Targets: Android and iOS only.** Declare `android`, `iosArm64` and `iosSimulatorArm64`. Do not add `jvm()`, desktop, `js`, `wasmJs`, `mingwX64`, `linux*` or `macos*`, even "just for tests". If the user asks for one, say it is outside this skill and confirm before adding it. Leave out `iosX64` (Intel simulator) unless the user needs it: Apple Silicon is the norm and Intel-Mac support is being phased out. Check the current Kotlin target-support docs if unsure.
2. **Use the Android-KMP library plugin**, `com.android.kotlin.multiplatform.library`, with a `kotlin { android { ... } }` block. Do not use `com.android.library` plus `androidTarget()` in new code. On AGP 9+ that route needs an opt-in to deprecated APIs and is expected to be removed in AGP 10. Never apply `com.android.application` or `org.jetbrains.kotlin.android` in the library module.
3. **Look up versions, never recall them.** Kotlin, AGP, Gradle, SKIE, Ktor and coroutines change often, and a wrong pin gives confusing build errors. Check kotlinlang.org/docs/releases.html, developer.android.com/build/releases/gradle-plugin and Maven Central / the Gradle plugin portal. Also check the compatibility tables (Kotlin, Gradle, AGP, JDK, Xcode). If you cannot browse, leave clearly marked placeholders and tell the user which versions to fill in.
4. **Logic lives in `commonMain`.** Use `androidMain` / `iosMain` only where a platform API is genuinely needed, behind an interface or `expect/actual`.
5. **Small, deliberate public API.** Turn on `explicitApi()` and keep everything `internal` unless consumers need it. Swift sees Kotlin through an Objective-C bridge, so every public type has a cost.
6. **Be honest about verification.** iOS targets only compile on macOS with Xcode. If you could not run a step (iOS build, tests, publish), say so in the final summary instead of implying it works.

## Workflow

### 1. Clarify (ask only what is missing)

If the user or the existing repo already answers a question, do not ask it again.

- What does the library do, and roughly what is the public API?
- Who consumes it: native Swift + Kotlin apps, or KMP apps? This decides how to distribute it (step 7).
- Minimum Android SDK and minimum iOS version. Defaults if the user has no preference: minSdk 24, iOS 15.
- Group ID, base package, and library name (the library name also becomes the iOS framework name).
- Capabilities needed: networking, storage, crypto, serialization, and so on.
- Is a Mac available for iOS builds and CI?

For an existing project, read it first: settings, version catalog, module build files, plugins, package names. Match its conventions. Do not migrate build plugins unless asked, but do mention deprecated ones (for example `com.android.library` in a KMP module).

### 2. Scaffold

```
my-lib/
├── settings.gradle.kts
├── build.gradle.kts
├── gradle.properties
├── gradle/libs.versions.toml
├── library/                          # the KMP module
│   ├── build.gradle.kts
│   └── src/
│       ├── commonMain/kotlin/
│       ├── commonTest/kotlin/
│       ├── androidMain/kotlin/
│       ├── androidHostTest/kotlin/   # only if host tests are enabled
│       ├── iosMain/kotlin/           # shared by device + simulator automatically
│       └── iosTest/kotlin/
└── .github/workflows/ci.yml
```

- Copy and adapt the templates in [references/build-config.md](references/build-config.md). Fill versions only after looking them up (rule 3).
- With `iosArm64()` and `iosSimulatorArm64()` declared, Kotlin's default hierarchy creates `iosMain` and `iosTest` for you. Do not add manual `dependsOn` wiring.
- Add a tiny sample consumer (an Android app module, or an Xcode project) only if the user wants one. It is the best smoke test, but it adds maintenance.

### 3. Implement

- **Prefer multiplatform libraries** and check that each one supports Android, `iosArm64` and `iosSimulatorArm64` before adding it (klibs.io is a good search index). Typical picks: kotlinx-coroutines, kotlinx-serialization, Ktor client (OkHttp engine on Android, Darwin on iOS), kotlinx-datetime, Okio or kotlinx-io, SQLDelight or Room (KMP), DataStore.
- **Interfaces first, `expect/actual` second.** For anything beyond a tiny factory or constant, define an interface in `commonMain` and inject the platform implementation. It is easier to test and to call from Swift. `expect/actual` classes can still emit Beta warnings, so prefer `expect fun` factories.
- **Android `Context`:** take it as an explicit parameter in an `androidMain` factory, or use AndroidX App Startup. Avoid reflection hacks and hidden global singletons.
- **iOS APIs:** use the `platform.*` bindings (Foundation, Security and so on) in `iosMain`. Reach for cinterop only when no binding exists.
- **Concurrency:** inject a `CoroutineDispatcher` (default `Dispatchers.Default`) so tests can replace it. Kotlin/Native uses the modern memory model, so do not use freezing or `@SharedImmutable`.
- **Keep DI internal.** Manual wiring or Koin is fine, but never expose the DI framework in the public API.
- **Errors:** prefer sealed result types across the public boundary. Kotlin exceptions that Swift cannot see will crash the app unless declared with `@Throws`.

### 4. Make the API Swift-friendly

Read [references/ios-interop.md](references/ios-interop.md) before finalising the public API. The short version:

- Avoid default arguments, top-level functions, extension functions and heavily generic types on public API. Swift sees them poorly or not at all.
- `Flow` and cancellation are awkward in plain Objective-C export. Either use SKIE or expose a small callback/`Cancellable` wrapper.
- SKIE only supports certain Kotlin versions and lags new releases. Check its compatibility page before choosing the Kotlin version, and do not bump Kotlin ahead of SKIE.
- Swift Export is still maturing. Do not choose it for production unless the user asks, and re-check its status first.

### 5. Test

- Put most tests in `commonTest` with `kotlin.test`, `kotlinx-coroutines-test` (`runTest`) and Turbine for flows. They then run on both platforms.
- Enable Android host (JVM) tests explicitly, since the new plugin disables them by default. Add device tests only if real Android APIs need them.
- Put tests for `iosMain` code in `iosTest`. They need macOS and an iOS simulator.

### 6. Verify (definition of done)

Run `./gradlew tasks` to confirm exact task names, since they depend on your module and framework names. Typically:

1. `./gradlew :library:allTests` (Android host tests plus iOS simulator tests on macOS).
2. `./gradlew :library:assemble` and the XCFramework task (`assemble<Name>XCFramework` or `assemble<Name>ReleaseXCFramework`, on macOS).
3. If the library is published, run an API/ABI check (binary-compatibility-validator or the Kotlin Gradle plugin's ABI validation, whichever is current) so accidental API breaks are caught.
4. Optionally import the XCFramework in a throwaway Xcode project and call one function from Swift.

On Linux or Windows, Apple targets are skipped and Gradle warns. State clearly which checks did and did not run.

### 7. Distribute

Read [references/publishing-and-ci.md](references/publishing-and-ci.md). In short:

- **Kotlin / KMP / Android consumers:** publish to Maven (Central or GitHub Packages). One coordinate serves every target.
- **Native Swift consumers:** ship a zipped **XCFramework** with a `Package.swift` binary target (Swift Package Manager). Use a static framework.
- **CI:** iOS steps need a macOS runner. Android-only checks can run on Linux.

### 8. Report back

Finish with a short summary: what was created, the exact commands to build, test and publish, the versions you pinned (and where you got them), and anything you could not verify.

## Common mistakes

- Pinning Kotlin, AGP, Gradle and JDK versions that are not compatible with each other.
- Leaving old `com.android.library`, `androidTarget()` or `androidUnitTest` names in a project that uses the new plugin. The test source sets are now `androidHostTest` and `androidDeviceTest`.
- Forgetting `api(...)` plus `export(...)` when a dependency's types appear in the public API that Swift sees.
- Naming a public Kotlin class the same as a Foundation or UIKit class (for example `Timer`), which causes Objective-C name clashes in Swift.
- Expecting the first iOS build to be fast. Kotlin/Native downloads its toolchain into `~/.konan`, so cache that folder in CI.
- Shipping a dynamic framework for SwiftPM. Use `isStatic = true`.
- Enabling Android resources, Java compilation or BuildConfig without checking that the new plugin supports them (resources and Java are opt-in; BuildConfig is not supported).

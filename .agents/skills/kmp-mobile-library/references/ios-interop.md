# Designing a Swift-friendly API

Kotlin reaches Swift through an Objective-C header that the Kotlin/Native compiler generates. Anything the header cannot express well becomes awkward in Swift. Design the public API around that.

Contents:
- What looks bad in Swift, and what to do instead
- Type mapping cheat sheet
- Exceptions
- Flow and suspend: two options
- Controlling what Swift sees
- Framework settings
- What the Swift side looks like

## What looks bad in Swift, and what to do instead

| Kotlin construct | What Swift gets | Do this instead |
|---|---|---|
| Default arguments | Not exported. Swift must pass every argument. | Add explicit overloads, or a config/builder object. |
| Top-level functions | `FileNameKt.doThing()` | Put them in a class or `object` with a clear name. |
| Extension functions | Static-style `FileNameKt.foo(receiver)` | Use member functions on the type. |
| Sealed classes / interfaces | A class hierarchy, no exhaustive `switch` | Use SKIE, or add a plain `enum` + `kind` property. |
| Generic types and functions | Type information is partly lost (`Any`) | Keep generics out of the public API where possible. |
| `Flow<T>` | An opaque type Swift cannot collect | SKIE, or a callback wrapper (below). |
| `suspend` functions | Completion handler, imported as `async throws` in Swift | Fine. Cancellation is weak without SKIE. Declare `@Throws`. |
| Value (inline) classes | Unreliable mapping | Avoid them in public API. |
| `object` | `Foo.shared` | Fine. Prefer a normal class with a factory for testability. |
| Class named like a Foundation/UIKit type | Name clash | Pick a distinctive name, or add `@ObjCName`. |

## Type mapping cheat sheet

- `Int` maps to `Int32`, `Long` to `Int64`, `Boolean` to `Bool`, `String` to `String`.
- Inside collections and for nullable primitives, numbers become boxed types: `List<Int>` is `[KotlinInt]`, `Int?` is `KotlinInt?`.
- `List`, `Set`, `Map` map to `Array`, `Set`, `Dictionary` (with boxed primitives as above).
- Interfaces become protocols. Enums become Swift-visible classes with constants.
- Prefer simple data holders (`data class` with plain fields) and small interfaces for the public surface.

## Exceptions

Swift cannot catch an ordinary Kotlin exception. An uncaught one terminates the app.

```kotlin
@Throws(MyLibException::class, CancellationException::class)
public suspend fun fetchProfile(id: String): Profile
```

- Annotate every public function that can fail with `@Throws(...)` listing the exception types Swift may see.
- Better: return a sealed result type (`Success` / `Failure`) from the public API and keep exceptions internal.
- `suspend` functions map `CancellationException` automatically. Other exceptions still need `@Throws`.

## Flow and suspend: two options

### Option A: SKIE (recommended when it supports your Kotlin version)

SKIE is a compiler plugin that improves the generated Swift API: `Flow` becomes `AsyncSequence`, sealed types get exhaustive `switch`, `suspend` cancellation works, and default arguments are exposed.

- Confirm the Kotlin version is supported (skie.touchlab.co/intro). SKIE has trailed new Kotlin releases by days to weeks, and preview releases of Kotlin are not supported.
- Pin Kotlin to a SKIE-supported version. Never bump Kotlin first.
- It changes the generated framework, so test the Swift side after every Kotlin or SKIE upgrade.

### Option B: a small wrapper, no extra plugin

Expose a callback API next to the `Flow` that Kotlin consumers use:

```kotlin
public fun interface Cancellable { public fun cancel() }

public class PriceFeed internal constructor(
    private val scope: CoroutineScope,
    private val source: Flow<Price>,
) {
    /** For Kotlin / Android consumers. */
    public val prices: Flow<Price> get() = source

    /** For Swift consumers. Call cancel() when done to avoid leaks. */
    public fun observePrices(
        onEach: (Price) -> Unit,
        onError: (Throwable) -> Unit,
    ): Cancellable {
        val job = scope.launch {
            try {
                source.collect { onEach(it) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                onError(e)
            }
        }
        return Cancellable { job.cancel() }
    }
}
```

Notes:
- Use `Dispatchers.Main` in the scope you pass in if callbacks must run on the main thread. `Dispatchers.Main` is available on iOS.
- Because default arguments are not exported, Swift has to pass `onError` every time. That is intended here.
- A Swift closure that captures `self` and is held by a Kotlin object can create a retain cycle. Always provide and document `cancel()` / `close()`.

## Controlling what Swift sees

These annotations are opt-in (experimental) and live in `kotlin.native` / `kotlin.experimental`. Check current names and opt-in markers before using them:

- `@ObjCName("Name")` renames a class, function or parameter as seen from Objective-C and Swift.
- `@HiddenFromObjC` hides a declaration from Swift while keeping it public for Kotlin. Useful for Kotlin-only helpers such as raw `Flow` properties when using Option B.
- `@ShouldRefineInSwift` marks a declaration so a hand-written Swift extension can wrap it.
- Anything `internal` (or `private`) is not visible to Swift at all. This is the best tool: keep the public surface small.

## Framework settings

- One framework per library. To ship several Kotlin modules as one Swift import, create an umbrella module that `api(...)`-depends on them and `export(...)`s them in `binaries.framework { }`. Add at least one (possibly empty) Kotlin source file to the umbrella module.
- `baseName` is the Swift module name. Choose a name unlikely to collide with a Swift module or type.
- `isStatic = true` for SwiftPM binary targets.
- `binaryOption("bundleId", "...")` gives the framework a unique bundle identifier.
- Only `export(...)` a dependency if its types appear in the public API.

## What the Swift side should look like

Plain Objective-C export (no SKIE):

```swift
import MyLib

let client = MyLibFactory.shared.create(config: config)   // factory instead of a default-arg constructor
Task {
    do {
        let profile = try await client.fetchProfile(id: "42")   // suspend -> async throws
        print(profile.name)
    } catch {
        print("failed: \(error)")
    }
}

let handle = feed.observePrices(onEach: { price in print(price) }, onError: { _ in })
// later
handle.cancel()
```

With SKIE, the observer becomes `for await price in feed.prices { ... }`.

Whenever you finalise a public API, write those few lines of Swift as a sanity check. If they look clumsy, change the Kotlin API rather than asking Swift users to work around it.

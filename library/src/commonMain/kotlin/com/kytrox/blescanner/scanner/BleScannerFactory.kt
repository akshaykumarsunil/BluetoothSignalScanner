package com.kytrox.blescanner.scanner

/**
 * Creates a platform-specific [BleScanner] instance.
 *
 * On Android, call the actual function with a [android.content.Context].
 * On iOS, call without arguments.
 */
public expect class BleScannerFactory {
    public fun create(): BleScanner
}

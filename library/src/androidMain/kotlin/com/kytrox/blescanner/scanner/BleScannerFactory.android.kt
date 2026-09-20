package com.kytrox.blescanner.scanner

import android.content.Context

/**
 * Android actual: requires [Context] for accessing BluetoothManager.
 */
public actual class BleScannerFactory(
    private val context: Context,
) {
    public actual fun create(): BleScanner {
        return AndroidBleScanner(context)
    }
}

package com.kytrox.blescanner.scanner

import android.content.Context

/**
 * Creates an Android [BleScanner] backed by [android.bluetooth.le.BluetoothLeScanner].
 */
public class BleScannerFactory(
    private val context: Context,
) {
    public fun create(): BleScanner {
        return AndroidBleScanner(context)
    }
}

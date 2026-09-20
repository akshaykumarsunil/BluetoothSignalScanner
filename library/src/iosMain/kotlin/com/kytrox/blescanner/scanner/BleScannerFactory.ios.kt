package com.kytrox.blescanner.scanner

/**
 * iOS actual: no parameters needed — CoreBluetooth does not
 * require an application context.
 */
public actual class BleScannerFactory {
    public actual fun create(): BleScanner {
        return IosBleScanner()
    }
}

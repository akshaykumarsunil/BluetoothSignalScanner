package com.kytrox.blescanner.scanner

/**
 * Creates an iOS [BleScanner] backed by CoreBluetooth [platform.CoreBluetooth.CBCentralManager].
 */
public class BleScannerFactory {
    public fun create(): BleScanner {
        return IosBleScanner()
    }
}

package com.kytrox.blescanner.scanner

import com.kytrox.blescanner.domain.BleSignal

/**
 * Scans for BLE advertisements for a specified duration.
 *
 * Implementations are platform-specific: Android uses BluetoothLeScanner,
 * iOS uses CBCentralManager.
 */
public interface BleScanner {

    /**
     * Starts a BLE scan and collects all received advertisements.
     *
     * @param durationSeconds how long to scan (default 12 seconds)
     * @return all parsed BLE signals received during the scan window
     */
    public suspend fun scan(durationSeconds: Int = DEFAULT_SCAN_DURATION): List<BleSignal>

    public companion object {
        public const val DEFAULT_SCAN_DURATION: Int = 12
    }
}

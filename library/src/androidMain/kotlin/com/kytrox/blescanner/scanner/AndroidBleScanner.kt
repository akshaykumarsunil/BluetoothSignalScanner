package com.kytrox.blescanner.scanner

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import com.kytrox.blescanner.domain.BleSignal
import com.kytrox.blescanner.parser.AdvertisementParser
import com.kytrox.blescanner.parser.RawAdvertisement
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

private const val MILLIS_PER_SECOND = 1000L

/**
 * Android BLE scanner using [BluetoothLeScanner].
 *
 * Uses SCAN_MODE_LOW_LATENCY for maximum signal capture during
 * the bounded scan window.
 */
internal class AndroidBleScanner(
    private val context: Context,
) : BleScanner {

    @SuppressLint("MissingPermission")
    override suspend fun scan(durationSeconds: Int): List<BleSignal> {
        val scanner = obtainScanner()
        return executeScan(scanner, durationSeconds)
    }

    private fun obtainScanner(): BluetoothLeScanner {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        return manager.adapter.bluetoothLeScanner
            ?: error("Bluetooth is not available or not enabled")
    }

    @SuppressLint("MissingPermission")
    private suspend fun executeScan(
        scanner: BluetoothLeScanner,
        durationSeconds: Int,
    ): List<BleSignal> {
        val results = mutableListOf<BleSignal>()
        val settings = buildScanSettings()
        val callback = createScanCallback(results)

        scanner.startScan(null, settings, callback)
        delay(durationSeconds * MILLIS_PER_SECOND)
        scanner.stopScan(callback)

        return results.toList()
    }

    private fun buildScanSettings(): ScanSettings {
        return ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
    }

    private fun createScanCallback(
        results: MutableList<BleSignal>,
    ): ScanCallback {
        return object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val raw = mapToRawAdvertisement(result)
                val signal = AdvertisementParser.parse(raw)
                results.add(signal)
            }
        }
    }

    private fun mapToRawAdvertisement(result: ScanResult): RawAdvertisement {
        val record = result.scanRecord
        return RawAdvertisement(
            deviceAddress = extractDeviceAddress(result),
            rssi = result.rssi,
            timestampMillis = System.currentTimeMillis(),
            deviceName = record?.deviceName,
            txPowerLevel = extractTxPowerLevel(record),
            serviceUuids = extractServiceUuids(record),
            serviceData = extractServiceData(record),
            manufacturerData = extractManufacturerData(record),
            flags = extractFlags(record),
            appearance = null,
        )
    }

    @SuppressLint("MissingPermission")
    private fun extractDeviceAddress(result: ScanResult): String {
        return result.device.address
    }

    private fun extractTxPowerLevel(
        record: android.bluetooth.le.ScanRecord?,
    ): Int? {
        if (record == null) return null
        val level = record.txPowerLevel
        if (level == Integer.MIN_VALUE) return null
        return level
    }

    private fun extractServiceUuids(
        record: android.bluetooth.le.ScanRecord?,
    ): List<String> {
        return record?.serviceUuids
            ?.map { it.uuid.toString() }
            ?: emptyList()
    }

    private fun extractServiceData(
        record: android.bluetooth.le.ScanRecord?,
    ): Map<String, ByteArray> {
        if (record == null) return emptyMap()
        val serviceData = record.serviceData ?: return emptyMap()
        return serviceData.entries.associate { (uuid, bytes) ->
            uuid.uuid.toString() to bytes
        }
    }

    private fun extractManufacturerData(
        record: android.bluetooth.le.ScanRecord?,
    ): Map<Int, ByteArray> {
        if (record == null) return emptyMap()
        val sparseArray = record.manufacturerSpecificData ?: return emptyMap()
        return buildMap {
            for (i in 0 until sparseArray.size()) {
                put(sparseArray.keyAt(i), sparseArray.valueAt(i))
            }
        }
    }

    private fun extractFlags(
        record: android.bluetooth.le.ScanRecord?,
    ): Int? {
        if (record == null) return null
        val flags = record.advertiseFlags
        if (flags == -1) return null
        return flags
    }
}

package com.kytrox.blescanner.scanner

import com.kytrox.blescanner.domain.BleSignal
import com.kytrox.blescanner.parser.AdvertisementParser
import com.kytrox.blescanner.parser.RawAdvertisement
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.CoreBluetooth.CBAdvertisementDataLocalNameKey
import platform.CoreBluetooth.CBAdvertisementDataManufacturerDataKey
import platform.CoreBluetooth.CBAdvertisementDataServiceDataKey
import platform.CoreBluetooth.CBAdvertisementDataServiceUUIDsKey
import platform.CoreBluetooth.CBAdvertisementDataTxPowerLevelKey
import platform.CoreBluetooth.CBCentralManager
import platform.CoreBluetooth.CBCentralManagerDelegateProtocol
import platform.CoreBluetooth.CBCentralManagerStatePoweredOn
import platform.CoreBluetooth.CBPeripheral
import platform.CoreBluetooth.CBUUID
import platform.Foundation.NSData
import platform.Foundation.NSNumber
import platform.darwin.NSObject
import kotlin.coroutines.resume
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.posix.gettimeofday
import platform.posix.timeval

private const val MILLIS_PER_SECOND = 1000L
private const val BYTE_MASK = 0xFF

/**
 * iOS BLE scanner using [CBCentralManager].
 *
 * Scans for all peripherals (no service filter) and maps
 * each discovered advertisement to [RawAdvertisement].
 */
internal class IosBleScanner : BleScanner {

    override suspend fun scan(durationSeconds: Int): List<BleSignal> {
        return executeScan(durationSeconds)
    }

    private suspend fun executeScan(durationSeconds: Int): List<BleSignal> {
        val results = mutableListOf<BleSignal>()
        val delegate = ScanDelegate(results)
        val manager = CBCentralManager(delegate = delegate, queue = null)

        waitForPoweredOn(delegate)
        manager.scanForPeripheralsWithServices(serviceUUIDs = null, options = null)
        delay(durationSeconds * MILLIS_PER_SECOND)
        manager.stopScan()

        return results.toList()
    }

    private suspend fun waitForPoweredOn(delegate: ScanDelegate) {
        if (delegate.isPoweredOn) return
        suspendCancellableCoroutine { continuation ->
            delegate.onPoweredOn = { continuation.resume(Unit) }
        }
    }
}

private class ScanDelegate(
    private val results: MutableList<BleSignal>,
) : NSObject(), CBCentralManagerDelegateProtocol {

    var isPoweredOn: Boolean = false
        private set

    var onPoweredOn: (() -> Unit)? = null

    override fun centralManagerDidUpdateState(central: CBCentralManager) {
        if (central.state != CBCentralManagerStatePoweredOn) return
        isPoweredOn = true
        onPoweredOn?.invoke()
        onPoweredOn = null
    }

    override fun centralManager(
        central: CBCentralManager,
        didDiscoverPeripheral: CBPeripheral,
        advertisementData: Map<Any?, *>,
        RSSI: NSNumber,
    ) {
        val raw = mapToRawAdvertisement(didDiscoverPeripheral, advertisementData, RSSI)
        val signal = AdvertisementParser.parse(raw)
        results.add(signal)
    }

    private fun mapToRawAdvertisement(
        peripheral: CBPeripheral,
        advertisementData: Map<Any?, *>,
        rssi: NSNumber,
    ): RawAdvertisement {
        return RawAdvertisement(
            deviceAddress = peripheral.identifier.UUIDString,
            rssi = rssi.intValue,
            timestampMillis = currentTimeMillis(),
            deviceName = extractDeviceName(advertisementData),
            txPowerLevel = extractTxPowerLevel(advertisementData),
            serviceUuids = extractServiceUuids(advertisementData),
            serviceData = extractServiceData(advertisementData),
            manufacturerData = extractManufacturerData(advertisementData),
            flags = null,
            appearance = null,
        )
    }

    private fun extractDeviceName(data: Map<Any?, *>): String? {
        return data[CBAdvertisementDataLocalNameKey] as? String
    }

    private fun extractTxPowerLevel(data: Map<Any?, *>): Int? {
        return (data[CBAdvertisementDataTxPowerLevelKey] as? NSNumber)?.intValue
    }

    private fun extractServiceUuids(data: Map<Any?, *>): List<String> {
        val uuids = data[CBAdvertisementDataServiceUUIDsKey] as? List<*> ?: return emptyList()
        return uuids.mapNotNull { (it as? CBUUID)?.UUIDString?.lowercase() }
            .map { expandShortUuid(it) }
    }

    private fun expandShortUuid(uuid: String): String {
        if (uuid.length <= 8) {
            return "0000${uuid.padStart(4, '0')}-0000-1000-8000-00805f9b34fb"
        }
        return uuid
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun extractServiceData(data: Map<Any?, *>): Map<String, ByteArray> {
        val serviceDataMap = data[CBAdvertisementDataServiceDataKey] as? Map<*, *> ?: return emptyMap()
        return buildMap {
            for ((key, value) in serviceDataMap) {
                val uuid = (key as? CBUUID)?.UUIDString?.lowercase() ?: continue
                val nsData = value as? NSData ?: continue
                put(expandShortUuid(uuid), nsData.toByteArray())
            }
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun extractManufacturerData(data: Map<Any?, *>): Map<Int, ByteArray> {
        val nsData = data[CBAdvertisementDataManufacturerDataKey] as? NSData ?: return emptyMap()
        val bytes = nsData.toByteArray()
        if (bytes.size < 2) return emptyMap()

        // First 2 bytes are company ID (little-endian)
        val companyId = (bytes[0].toInt() and BYTE_MASK) or
            ((bytes[1].toInt() and BYTE_MASK) shl 8)
        val payload = bytes.copyOfRange(2, bytes.size)

        return mapOf(companyId to payload)
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun currentTimeMillis(): Long = memScoped {
        val tv = alloc<timeval>()
        gettimeofday(tv.ptr, null)
        (tv.tv_sec * MILLIS_PER_SECOND) + (tv.tv_usec / MILLIS_PER_SECOND)
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val length = this.length.toInt()
    if (length == 0) return ByteArray(0)
    val bytes = ByteArray(length)
    bytes.usePinned { pinned ->
        platform.posix.memcpy(pinned.addressOf(0), this.bytes, this.length)
    }
    return bytes
}


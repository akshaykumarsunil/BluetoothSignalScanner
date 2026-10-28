package com.kytrox.blescanner.scanner

import com.kytrox.blescanner.domain.AdType
import com.kytrox.blescanner.domain.BleSignal
import com.kytrox.blescanner.parser.AdvertisementParser
import com.kytrox.blescanner.parser.RawAdvertisement
import com.kytrox.blescanner.parser.RawAdvertisementParser
import com.kytrox.blescanner.parser.hexToByteArray
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
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
import platform.posix.gettimeofday
import platform.posix.memcpy
import platform.posix.timeval
import kotlin.coroutines.resume

private const val MILLIS_PER_SECOND = 1000L
private const val TYPE_HEADER_SIZE = 1
private const val SHORT_UUID_BYTE_SIZE = 2
private const val UUID_32_BYTE_SIZE = 4

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
        val rawBytes = serializeAdvertisementData(advertisementData)
        return RawAdvertisementParser.parse(
            deviceAddress = peripheral.identifier.UUIDString,
            rssi = rssi.intValue,
            timestampMillis = currentTimeMillis(),
            rawBytes = rawBytes,
        )
    }

    private fun serializeAdvertisementData(data: Map<Any?, *>): ByteArray {
        val buffer = mutableListOf<Byte>()
        appendLocalName(data, buffer)
        appendTxPower(data, buffer)
        appendServiceUuids(data, buffer)
        appendServiceData(data, buffer)
        appendManufacturerData(data, buffer)
        return buffer.toByteArray()
    }

    private fun appendLocalName(data: Map<Any?, *>, buffer: MutableList<Byte>) {
        val name = data[CBAdvertisementDataLocalNameKey] as? String ?: return
        appendStructure(AdType.COMPLETE_LOCAL_NAME.code, name.encodeToByteArray(), buffer)
    }

    private fun appendTxPower(data: Map<Any?, *>, buffer: MutableList<Byte>) {
        val level = (data[CBAdvertisementDataTxPowerLevelKey] as? NSNumber)?.intValue ?: return
        appendStructure(AdType.TX_POWER_LEVEL.code, byteArrayOf(level.toByte()), buffer)
    }

    private fun appendServiceUuids(data: Map<Any?, *>, buffer: MutableList<Byte>) {
        val uuids = data[CBAdvertisementDataServiceUUIDsKey] as? List<*> ?: return
        for (item in uuids) {
            val uuidHex = (item as? CBUUID)?.UUIDString ?: continue
            appendSingleUuid(uuidHex.hexToByteArray(), buffer)
        }
    }

    private fun appendSingleUuid(uuidBytes: ByteArray, buffer: MutableList<Byte>) {
        if (uuidBytes.size == SHORT_UUID_BYTE_SIZE) {
            appendStructure(AdType.COMPLETE_SERVICE_UUIDS_16_BIT.code, uuidBytes.reversedArray(), buffer)
            return
        }
        if (uuidBytes.size == UUID_32_BYTE_SIZE) {
            appendStructure(AdType.COMPLETE_SERVICE_UUIDS_32_BIT.code, uuidBytes.reversedArray(), buffer)
            return
        }
        appendStructure(AdType.COMPLETE_SERVICE_UUIDS_128_BIT.code, uuidBytes, buffer)
    }

    private fun appendServiceData(data: Map<Any?, *>, buffer: MutableList<Byte>) {
        val map = data[CBAdvertisementDataServiceDataKey] as? Map<*, *> ?: return
        for ((key, value) in map) {
            val uuidHex = (key as? CBUUID)?.UUIDString ?: continue
            val payload = (value as? NSData)?.toByteArray() ?: continue
            appendSingleServiceData(uuidHex.hexToByteArray(), payload, buffer)
        }
    }

    private fun appendSingleServiceData(
        uuidBytes: ByteArray,
        payload: ByteArray,
        buffer: MutableList<Byte>,
    ) {
        if (uuidBytes.size == SHORT_UUID_BYTE_SIZE) {
            appendStructure(AdType.SERVICE_DATA_16_BIT.code, uuidBytes.reversedArray() + payload, buffer)
            return
        }
        if (uuidBytes.size == UUID_32_BYTE_SIZE) {
            appendStructure(AdType.SERVICE_DATA_32_BIT.code, uuidBytes.reversedArray() + payload, buffer)
            return
        }
        appendStructure(AdType.SERVICE_DATA_128_BIT.code, uuidBytes + payload, buffer)
    }

    private fun appendManufacturerData(data: Map<Any?, *>, buffer: MutableList<Byte>) {
        val nsData = data[CBAdvertisementDataManufacturerDataKey] as? NSData ?: return
        val bytes = nsData.toByteArray()
        if (bytes.isEmpty()) return
        appendStructure(AdType.MANUFACTURER_SPECIFIC_DATA.code, bytes, buffer)
    }

    private fun appendStructure(
        typeCode: Int,
        payload: ByteArray,
        buffer: MutableList<Byte>,
    ) {
        buffer.add((payload.size + TYPE_HEADER_SIZE).toByte())
        buffer.add(typeCode.toByte())
        for (byte in payload) {
            buffer.add(byte)
        }
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
        memcpy(pinned.addressOf(0), this.bytes, this.length)
    }
    return bytes
}

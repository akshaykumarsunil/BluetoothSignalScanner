package com.kytrox.blescanner.parser

import com.kytrox.blescanner.domain.BleSignal

private const val EDDYSTONE_SERVICE_UUID = "0000feaa-0000-1000-8000-00805f9b34fb"
private const val FRAME_TYPE_UID: Int = 0x00
private const val FRAME_TYPE_URL: Int = 0x10
private const val FRAME_TYPE_TLM: Int = 0x20

private const val UID_NAMESPACE_OFFSET = 2
private const val UID_NAMESPACE_LENGTH = 10
private const val UID_INSTANCE_OFFSET = 12
private const val UID_INSTANCE_LENGTH = 6
private const val UID_MIN_LENGTH = 18

private const val URL_SCHEME_OFFSET = 2
private const val URL_MIN_LENGTH = 3

private const val TLM_VERSION_OFFSET = 1
private const val TLM_BATTERY_OFFSET = 2
private const val TLM_TEMPERATURE_OFFSET = 4
private const val TLM_ADV_COUNT_OFFSET = 6
private const val TLM_UPTIME_OFFSET = 10
private const val TLM_MIN_LENGTH = 14
private const val TLM_UPTIME_DIVISOR = 10.0f

private const val FRAME_TYPE_MASK = 0xF0
private const val BYTE_MASK = 0xFF
private const val FIXED_POINT_SHIFT = 8

/**
 * Parses Eddystone beacon advertisements from service data.
 *
 * Detection: service data with UUID 0xFEAA.
 * Dispatches by frame type byte to UID, URL, or TLM.
 */
internal object EddystoneParser {

    internal fun parse(raw: RawAdvertisement): BleSignal.Eddystone? {
        val serviceBytes = findEddystoneData(raw) ?: return null
        if (serviceBytes.isEmpty()) return null
        return parseFrame(serviceBytes, raw)
    }

    private fun findEddystoneData(raw: RawAdvertisement): ByteArray? {
        return raw.serviceData[EDDYSTONE_SERVICE_UUID]
    }

    private fun parseFrame(
        data: ByteArray,
        raw: RawAdvertisement,
    ): BleSignal.Eddystone? {
        val frameType = data[0].toInt() and FRAME_TYPE_MASK
        return when (frameType) {
            FRAME_TYPE_UID -> parseUid(data, raw)
            FRAME_TYPE_URL -> parseUrl(data, raw)
            FRAME_TYPE_TLM -> parseTlm(data, raw)
            else -> null
        }
    }

    private fun parseUid(
        data: ByteArray,
        raw: RawAdvertisement,
    ): BleSignal.Eddystone.Uid? {
        if (data.size < UID_MIN_LENGTH) return null
        val txPower = data.readInt8(1)
        val namespace = data.copyOfRange(UID_NAMESPACE_OFFSET, UID_NAMESPACE_OFFSET + UID_NAMESPACE_LENGTH).toHexString()
        val instance = data.copyOfRange(UID_INSTANCE_OFFSET, UID_INSTANCE_OFFSET + UID_INSTANCE_LENGTH).toHexString()

        return BleSignal.Eddystone.Uid(
            txPower = txPower,
            namespace = namespace,
            instance = instance,
            rssi = raw.rssi,
            deviceAddress = raw.deviceAddress,
            timestampMillis = raw.timestampMillis,
        )
    }

    private fun parseUrl(
        data: ByteArray,
        raw: RawAdvertisement,
    ): BleSignal.Eddystone.Url? {
        if (data.size < URL_MIN_LENGTH) return null
        val txPower = data.readInt8(1)
        val urlBytes = data.copyOfRange(URL_SCHEME_OFFSET, data.size)
        val url = EddystoneUrlDecoder.decodeUrl(urlBytes)

        return BleSignal.Eddystone.Url(
            txPower = txPower,
            url = url,
            rssi = raw.rssi,
            deviceAddress = raw.deviceAddress,
            timestampMillis = raw.timestampMillis,
        )
    }

    private fun parseTlm(
        data: ByteArray,
        raw: RawAdvertisement,
    ): BleSignal.Eddystone.Tlm? {
        if (data.size < TLM_MIN_LENGTH) return null

        val version = data[TLM_VERSION_OFFSET].toInt() and BYTE_MASK
        val battery = data.readUInt16BE(TLM_BATTERY_OFFSET)
        val temperature = decodeTemperature(data)
        val advCount = readUInt32BE(data, TLM_ADV_COUNT_OFFSET)
        val uptime = readUInt32BE(data, TLM_UPTIME_OFFSET) / TLM_UPTIME_DIVISOR

        return BleSignal.Eddystone.Tlm(
            version = version,
            batteryMilliVolts = battery,
            temperatureCelsius = temperature,
            advertisementCount = advCount,
            uptimeSeconds = uptime,
            rssi = raw.rssi,
            deviceAddress = raw.deviceAddress,
            timestampMillis = raw.timestampMillis,
        )
    }

    // 8.8 fixed-point format: integer part (signed) + fractional part
    private fun decodeTemperature(data: ByteArray): Float {
        val integerPart = data.readInt8(TLM_TEMPERATURE_OFFSET)
        val fractionalPart = data[TLM_TEMPERATURE_OFFSET + 1].toInt() and BYTE_MASK
        return integerPart + (fractionalPart.toFloat() / (1 shl FIXED_POINT_SHIFT))
    }

    private fun readUInt32BE(data: ByteArray, offset: Int): Long {
        return ((data[offset].toLong() and 0xFF) shl 24) or
            ((data[offset + 1].toLong() and 0xFF) shl 16) or
            ((data[offset + 2].toLong() and 0xFF) shl 8) or
            (data[offset + 3].toLong() and 0xFF)
    }
}

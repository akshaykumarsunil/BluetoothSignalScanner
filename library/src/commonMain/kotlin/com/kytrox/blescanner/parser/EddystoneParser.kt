package com.kytrox.blescanner.parser

import com.kytrox.blescanner.domain.BleSignal

private const val EDDYSTONE_UUID_16BIT = 0xFEAA
private const val UUID_16_BYTE_LENGTH = 2
private const val MIN_SERVICE_DATA_LENGTH = 3
private const val FRAME_TYPE_UID = 0x00
private const val FRAME_TYPE_URL = 0x10
private const val FRAME_TYPE_TLM = 0x20

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
private const val SHIFT_24_BITS = 24
private const val SHIFT_16_BITS = 16
private const val SHIFT_8_BITS = 8

/**
 * Parses Eddystone beacon advertisements from raw 16-bit Service Data (0x16)
 * byte arrays stored in [RawAdvertisement.serviceData16Bit].
 */
internal object EddystoneParser {

    internal fun parse(raw: RawAdvertisement): BleSignal.Eddystone? {
        val frameBytes = findEddystoneFrameBytes(raw) ?: return null
        if (frameBytes.isEmpty()) return null
        return parseFrame(frameBytes, raw)
    }

    private fun findEddystoneFrameBytes(raw: RawAdvertisement): ByteArray? {
        for (entry in raw.serviceData16Bit) {
            if (isEddystoneEntry(entry)) {
                return entry.copyOfRange(UUID_16_BYTE_LENGTH, entry.size)
            }
        }
        return null
    }

    private fun isEddystoneEntry(entry: ByteArray): Boolean {
        if (entry.size < MIN_SERVICE_DATA_LENGTH) return false
        val uuidLittleEndian = entry.readUInt16LE(0)
        val uuidBigEndian = entry.readUInt16BE(0)
        return uuidLittleEndian == EDDYSTONE_UUID_16BIT || uuidBigEndian == EDDYSTONE_UUID_16BIT
    }

    private fun parseFrame(data: ByteArray, raw: RawAdvertisement): BleSignal.Eddystone? {
        val frameType = data[0].toInt() and FRAME_TYPE_MASK
        return when (frameType) {
            FRAME_TYPE_UID -> parseUid(data, raw)
            FRAME_TYPE_URL -> parseUrl(data, raw)
            FRAME_TYPE_TLM -> parseTlm(data, raw)
            else -> null
        }
    }

    private fun parseUid(data: ByteArray, raw: RawAdvertisement): BleSignal.Eddystone.Uid? {
        if (data.size < UID_MIN_LENGTH) return null
        return BleSignal.Eddystone.Uid(
            txPower = data.readInt8(1),
            namespace = sliceHex(data, UID_NAMESPACE_OFFSET, UID_NAMESPACE_LENGTH),
            instance = sliceHex(data, UID_INSTANCE_OFFSET, UID_INSTANCE_LENGTH),
            rssi = raw.rssi,
            deviceAddress = raw.deviceAddress,
            timestampMillis = raw.timestampMillis,
        )
    }

    private fun sliceHex(data: ByteArray, offset: Int, length: Int): String {
        return data.copyOfRange(offset, offset + length).toHexString()
    }

    private fun parseUrl(data: ByteArray, raw: RawAdvertisement): BleSignal.Eddystone.Url? {
        if (data.size < URL_MIN_LENGTH) return null
        val urlBytes = data.copyOfRange(URL_SCHEME_OFFSET, data.size)
        return BleSignal.Eddystone.Url(
            txPower = data.readInt8(1),
            url = EddystoneUrlDecoder.decodeUrl(urlBytes),
            rssi = raw.rssi,
            deviceAddress = raw.deviceAddress,
            timestampMillis = raw.timestampMillis,
        )
    }

    private fun parseTlm(data: ByteArray, raw: RawAdvertisement): BleSignal.Eddystone.Tlm? {
        if (data.size < TLM_MIN_LENGTH) return null
        return BleSignal.Eddystone.Tlm(
            version = data[TLM_VERSION_OFFSET].toInt() and BYTE_MASK,
            batteryMilliVolts = data.readUInt16BE(TLM_BATTERY_OFFSET),
            temperatureCelsius = decodeTemperature(data),
            advertisementCount = readUInt32BE(data, TLM_ADV_COUNT_OFFSET),
            uptimeSeconds = readUInt32BE(data, TLM_UPTIME_OFFSET) / TLM_UPTIME_DIVISOR,
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
        val b0 = (data[offset].toLong() and BYTE_MASK.toLong()) shl SHIFT_24_BITS
        val b1 = (data[offset + 1].toLong() and BYTE_MASK.toLong()) shl SHIFT_16_BITS
        val b2 = (data[offset + 2].toLong() and BYTE_MASK.toLong()) shl SHIFT_8_BITS
        val b3 = data[offset + 3].toLong() and BYTE_MASK.toLong()
        return b0 or b1 or b2 or b3
    }
}

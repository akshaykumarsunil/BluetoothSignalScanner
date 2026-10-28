package com.kytrox.blescanner.parser

import com.kytrox.blescanner.domain.AdType

private const val BYTE_MASK = 0xFF
private const val LENGTH_BYTE_OFFSET = 0
private const val TYPE_BYTE_OFFSET = 1
private const val DATA_START_OFFSET = 2
private const val HEADER_BYTE_COUNT = 1

private class ParsedStructures(
    val known: MutableMap<AdType, MutableList<ByteArray>> = mutableMapOf(),
    val unknown: MutableMap<Int, MutableList<ByteArray>> = mutableMapOf(),
) {
    fun toRawAdvertisement(address: String, rssi: Int, timestamp: Long, rawBytes: ByteArray): RawAdvertisement {
        return RawAdvertisement(
            deviceAddress = address,
            rssi = rssi,
            timestampMillis = timestamp,
            rawBytes = rawBytes,
            rawHex = rawBytes.toHexString(),
            adStructures = known,
            unknownAdStructures = unknown,
        )
    }
}

/**
 * Parses a raw BLE advertisement packet [ByteArray] or hexadecimal string
 * (following the Bluetooth SIG Length-Type-Value format) and routes each
 * AD structure into [RawAdvertisement] by its [AdType].
 */
public object RawAdvertisementParser {

    public fun parse(
        deviceAddress: String,
        rssi: Int,
        timestampMillis: Long,
        rawBytes: ByteArray,
    ): RawAdvertisement {
        val parsed = parseAdStructures(rawBytes)
        return parsed.toRawAdvertisement(deviceAddress, rssi, timestampMillis, rawBytes)
    }

    public fun parseHex(
        deviceAddress: String,
        rssi: Int,
        timestampMillis: Long,
        hexPayload: String,
    ): RawAdvertisement {
        val rawBytes = hexPayload.hexToByteArray()
        return parse(deviceAddress, rssi, timestampMillis, rawBytes)
    }

    private fun parseAdStructures(rawBytes: ByteArray): ParsedStructures {
        val parsed = ParsedStructures()
        var offset = 0
        while (offset < rawBytes.size) {
            val length = rawBytes[offset + LENGTH_BYTE_OFFSET].toInt() and BYTE_MASK
            if (!isValidStructureLength(offset, length, rawBytes.size)) return parsed
            routeStructure(rawBytes, offset, length, parsed)
            offset += length + HEADER_BYTE_COUNT
        }
        return parsed
    }

    private fun isValidStructureLength(offset: Int, length: Int, totalSize: Int): Boolean {
        if (length <= 0) return false
        return offset + length < totalSize
    }

    private fun routeStructure(
        rawBytes: ByteArray,
        offset: Int,
        length: Int,
        parsed: ParsedStructures,
    ) {
        val typeCode = rawBytes[offset + TYPE_BYTE_OFFSET].toInt() and BYTE_MASK
        val payload = rawBytes.copyOfRange(offset + DATA_START_OFFSET, offset + HEADER_BYTE_COUNT + length)
        val adType = AdType.fromCode(typeCode)
        if (adType != null) {
            parsed.known.getOrPut(adType) { mutableListOf() }.add(payload)
            return
        }
        parsed.unknown.getOrPut(typeCode) { mutableListOf() }.add(payload)
    }
}

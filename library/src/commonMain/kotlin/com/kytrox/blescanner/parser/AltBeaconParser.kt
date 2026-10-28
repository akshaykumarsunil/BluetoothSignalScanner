package com.kytrox.blescanner.parser

import com.kytrox.blescanner.domain.BleSignal
import com.kytrox.blescanner.domain.CompanyIdentifierLookup

private const val COMPANY_ID_OFFSET = 0
private const val BEACON_CODE_OFFSET = 2
private const val BEACON_ID_OFFSET = 4
private const val BEACON_ID_LENGTH = 20
private const val REF_RSSI_OFFSET = 24
private const val MFG_RESERVED_OFFSET = 25
private const val MIN_ALTBEACON_PAYLOAD_LENGTH = 26
private const val ALTBEACON_CODE_HIGH: Byte = 0xBE.toByte()
private const val ALTBEACON_CODE_LOW: Byte = 0xAC.toByte()
private const val BYTE_MASK = 0xFF

/**
 * Parses AltBeacon advertisements from raw Manufacturer Specific Data (0xFF)
 * byte arrays stored in [RawAdvertisement.manufacturerSpecificData].
 */
internal object AltBeaconParser {

    internal fun parse(raw: RawAdvertisement): BleSignal.AltBeacon? {
        for (manufacturerBytes in raw.manufacturerSpecificData) {
            val beacon = tryParse(manufacturerBytes, raw)
            if (beacon != null) return beacon
        }
        return null
    }

    private fun tryParse(data: ByteArray, raw: RawAdvertisement): BleSignal.AltBeacon? {
        if (!isAltBeacon(data)) return null
        return extractBeacon(data, raw)
    }

    private fun isAltBeacon(data: ByteArray): Boolean {
        if (data.size < MIN_ALTBEACON_PAYLOAD_LENGTH) return false
        return data[BEACON_CODE_OFFSET] == ALTBEACON_CODE_HIGH &&
            data[BEACON_CODE_OFFSET + 1] == ALTBEACON_CODE_LOW
    }

    private fun extractBeacon(data: ByteArray, raw: RawAdvertisement): BleSignal.AltBeacon {
        val manufacturerId = data.readUInt16LE(COMPANY_ID_OFFSET)
        return BleSignal.AltBeacon(
            manufacturerId = manufacturerId,
            manufacturerName = CompanyIdentifierLookup.resolve(manufacturerId),
            beaconId = extractBeaconId(data),
            refRssi = data.readInt8(REF_RSSI_OFFSET),
            mfgReserved = data[MFG_RESERVED_OFFSET].toInt() and BYTE_MASK,
            rssi = raw.rssi,
            deviceAddress = raw.deviceAddress,
            timestampMillis = raw.timestampMillis,
        )
    }

    private fun extractBeaconId(data: ByteArray): String {
        val beaconIdEnd = BEACON_ID_OFFSET + BEACON_ID_LENGTH
        return data.copyOfRange(BEACON_ID_OFFSET, beaconIdEnd).toHexString()
    }
}

package com.kytrox.blescanner.parser

import com.kytrox.blescanner.domain.BleSignal
import com.kytrox.blescanner.domain.CompanyIdentifierLookup

private const val ALTBEACON_CODE_HIGH: Byte = 0xBE.toByte()
private const val ALTBEACON_CODE_LOW: Byte = 0xAC.toByte()
private const val BEACON_CODE_OFFSET = 0
private const val BEACON_ID_OFFSET = 2
private const val BEACON_ID_LENGTH = 20
private const val REF_RSSI_OFFSET = 22
private const val MFG_RESERVED_OFFSET = 23
private const val MIN_ALTBEACON_LENGTH = 24

/**
 * Parses AltBeacon advertisements from manufacturer-specific data.
 *
 * Detection: any manufacturer data entry where bytes 0-1 == 0xBEAC (big-endian).
 * The manufacturer ID (2 bytes before the beacon code in the raw AD) is
 * already extracted by the platform scanner as the map key.
 */
internal object AltBeaconParser {

    internal fun parse(raw: RawAdvertisement): BleSignal.AltBeacon? {
        for ((manufacturerId, data) in raw.manufacturerData) {
            val beacon = tryParse(manufacturerId, data, raw)
            if (beacon != null) return beacon
        }
        return null
    }

    private fun tryParse(
        manufacturerId: Int,
        data: ByteArray,
        raw: RawAdvertisement,
    ): BleSignal.AltBeacon? {
        if (!isAltBeacon(data)) return null
        return extractBeacon(manufacturerId, data, raw)
    }

    private fun isAltBeacon(data: ByteArray): Boolean {
        if (data.size < MIN_ALTBEACON_LENGTH) return false
        return data[BEACON_CODE_OFFSET] == ALTBEACON_CODE_HIGH &&
            data[BEACON_CODE_OFFSET + 1] == ALTBEACON_CODE_LOW
    }

    private fun extractBeacon(
        manufacturerId: Int,
        data: ByteArray,
        raw: RawAdvertisement,
    ): BleSignal.AltBeacon {
        val beaconId = data.copyOfRange(BEACON_ID_OFFSET, BEACON_ID_OFFSET + BEACON_ID_LENGTH).toHexString()
        val refRssi = data.readInt8(REF_RSSI_OFFSET)
        val mfgReserved = data[MFG_RESERVED_OFFSET].toInt() and 0xFF

        return BleSignal.AltBeacon(
            manufacturerId = manufacturerId,
            manufacturerName = CompanyIdentifierLookup.resolve(manufacturerId),
            beaconId = beaconId,
            refRssi = refRssi,
            mfgReserved = mfgReserved,
            rssi = raw.rssi,
            deviceAddress = raw.deviceAddress,
            timestampMillis = raw.timestampMillis,
        )
    }
}

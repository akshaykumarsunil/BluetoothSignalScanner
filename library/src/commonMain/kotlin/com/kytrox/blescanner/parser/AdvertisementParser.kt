package com.kytrox.blescanner.parser

import com.kytrox.blescanner.domain.BleSignal

/**
 * Orchestrates advertisement parsing by trying protocol-specific
 * parsers in priority order and falling back to SIG GAP.
 *
 * Priority: AltBeacon -> Eddystone -> SIG (always succeeds).
 */
public object AdvertisementParser {

    public fun parse(raw: RawAdvertisement): BleSignal {
        return tryAltBeacon(raw)
            ?: tryEddystone(raw)
            ?: parseSig(raw)
    }

    public fun parseBytes(
        deviceAddress: String,
        rssi: Int,
        timestampMillis: Long,
        rawBytes: ByteArray,
    ): BleSignal {
        val raw = RawAdvertisementParser.parse(deviceAddress, rssi, timestampMillis, rawBytes)
        return parse(raw)
    }

    public fun parseHex(
        deviceAddress: String,
        rssi: Int,
        timestampMillis: Long,
        hexPayload: String,
    ): BleSignal {
        val raw = RawAdvertisementParser.parseHex(deviceAddress, rssi, timestampMillis, hexPayload)
        return parse(raw)
    }

    private fun tryAltBeacon(raw: RawAdvertisement): BleSignal.AltBeacon? {
        return AltBeaconParser.parse(raw)
    }

    private fun tryEddystone(raw: RawAdvertisement): BleSignal.Eddystone? {
        return EddystoneParser.parse(raw)
    }

    private fun parseSig(raw: RawAdvertisement): BleSignal.Sig {
        return SigParser.parse(raw)
    }
}

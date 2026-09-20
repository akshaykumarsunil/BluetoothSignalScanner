package com.kytrox.blescanner.parser

import com.kytrox.blescanner.domain.BleSignal

/**
 * Orchestrates advertisement parsing by trying protocol-specific
 * parsers in priority order and falling back to SIG GAP.
 *
 * Priority: AltBeacon → Eddystone → SIG (always succeeds).
 */
internal object AdvertisementParser {

    internal fun parse(raw: RawAdvertisement): BleSignal {
        return tryAltBeacon(raw)
            ?: tryEddystone(raw)
            ?: parseSig(raw)
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

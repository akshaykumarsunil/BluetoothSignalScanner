package com.kytrox.blescanner.parser

import com.kytrox.blescanner.domain.AppearanceLookup
import com.kytrox.blescanner.domain.BleSignal
import com.kytrox.blescanner.domain.CompanyIdentifierLookup
import com.kytrox.blescanner.domain.DiscoveryMode
import com.kytrox.blescanner.domain.ManufacturerData

private const val FLAG_LIMITED_DISCOVERABLE = 0x01
private const val FLAG_GENERAL_DISCOVERABLE = 0x02
private const val FLAG_BR_EDR_NOT_SUPPORTED = 0x04
private const val FLAG_LE_BR_EDR_CONTROLLER = 0x08
private const val FLAG_LE_BR_EDR_HOST = 0x10

/**
 * Parses standard Bluetooth SIG GAP advertisements into
 * human-readable [BleSignal.Sig] models.
 *
 * This parser always succeeds and is used as the fallback.
 */
internal object SigParser {

    internal fun parse(raw: RawAdvertisement): BleSignal.Sig {
        return BleSignal.Sig(
            deviceName = raw.deviceName,
            txPowerLevel = formatTxPower(raw.txPowerLevel),
            serviceUuids = raw.serviceUuids,
            appearance = resolveAppearance(raw.appearance),
            discoveryModes = parseFlags(raw.flags),
            manufacturerSpecificData = parseManufacturerData(raw.manufacturerData),
            serviceData = formatServiceData(raw.serviceData),
            rssi = raw.rssi,
            deviceAddress = raw.deviceAddress,
            timestampMillis = raw.timestampMillis,
        )
    }

    private fun formatTxPower(level: Int?): String? {
        if (level == null) return null
        return "$level dBm"
    }

    private fun resolveAppearance(code: Int?): String? {
        if (code == null) return null
        return AppearanceLookup.resolve(code)
    }

    internal fun parseFlags(flags: Int?): List<DiscoveryMode> {
        if (flags == null) return emptyList()
        return buildList {
            addFlagIfSet(flags, FLAG_LIMITED_DISCOVERABLE, DiscoveryMode.LIMITED_DISCOVERABLE)
            addFlagIfSet(flags, FLAG_GENERAL_DISCOVERABLE, DiscoveryMode.GENERAL_DISCOVERABLE)
            addFlagIfSet(flags, FLAG_BR_EDR_NOT_SUPPORTED, DiscoveryMode.BR_EDR_NOT_SUPPORTED)
            addFlagIfSet(flags, FLAG_LE_BR_EDR_CONTROLLER, DiscoveryMode.LE_AND_BR_EDR_CONTROLLER)
            addFlagIfSet(flags, FLAG_LE_BR_EDR_HOST, DiscoveryMode.LE_AND_BR_EDR_HOST)
        }
    }

    private fun MutableList<DiscoveryMode>.addFlagIfSet(
        flags: Int,
        mask: Int,
        mode: DiscoveryMode,
    ) {
        if (flags and mask != 0) add(mode)
    }

    private fun parseManufacturerData(
        data: Map<Int, ByteArray>,
    ): List<ManufacturerData> {
        return data.map { (companyId, payload) ->
            ManufacturerData(
                companyId = companyId,
                companyName = CompanyIdentifierLookup.resolve(companyId),
                payload = payload.toHexString(),
            )
        }
    }

    private fun formatServiceData(
        data: Map<String, ByteArray>,
    ): Map<String, String> {
        return data.mapValues { (_, bytes) -> bytes.toHexString() }
    }
}

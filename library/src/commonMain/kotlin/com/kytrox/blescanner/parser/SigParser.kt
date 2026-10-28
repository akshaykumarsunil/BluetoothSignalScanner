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

private const val BYTE_MASK = 0xFF
private const val UUID_16_BYTE_LENGTH = 2
private const val UUID_32_BYTE_LENGTH = 4
private const val UUID_128_BYTE_LENGTH = 16
private const val COMPANY_ID_BYTE_LENGTH = 2
private const val APPEARANCE_BYTE_LENGTH = 2
private const val CONNECTION_INTERVAL_BYTE_LENGTH = 4
private const val MAC_ADDRESS_BYTE_LENGTH = 6
private const val LE_DEVICE_ADDRESS_BYTE_LENGTH = 7
private const val LE_ROLE_PERIPHERAL_ONLY = 0x00
private const val LE_ROLE_CENTRAL_ONLY = 0x01
private const val LE_ROLE_PERIPHERAL_PREFERRED = 0x02
private const val LE_ROLE_CENTRAL_PREFERRED = 0x03

/**
 * Parses standard Bluetooth SIG GAP advertisements from the raw
 * [ByteArray] fields in [RawAdvertisement] into human-readable [BleSignal.Sig] models.
 */
internal object SigParser {

    internal fun parse(raw: RawAdvertisement): BleSignal.Sig {
        val coreSignal = parseCoreFields(raw)
        val withNames = enrichNamesAndUris(coreSignal, raw)
        return enrichAddressingAndTiming(withNames, raw)
    }

    private fun parseCoreFields(raw: RawAdvertisement): BleSignal.Sig {
        return BleSignal.Sig(
            deviceName = extractDeviceName(raw),
            txPowerLevel = formatTxPower(raw.txPowerLevel),
            serviceUuids = extractServiceUuids(raw),
            appearance = resolveAppearance(raw.appearance),
            discoveryModes = parseFlagsFromBytes(raw.flags),
            manufacturerSpecificData = parseManufacturerData(raw.manufacturerSpecificData),
            serviceData = parseServiceData(raw),
            rssi = raw.rssi,
            deviceAddress = raw.deviceAddress,
            timestampMillis = raw.timestampMillis,
        )
    }

    private fun enrichNamesAndUris(base: BleSignal.Sig, raw: RawAdvertisement): BleSignal.Sig {
        return base.copy(
            shortenedLocalName = raw.shortenedLocalName?.decodeToString(),
            completeLocalName = raw.completeLocalName?.decodeToString(),
            serviceSolicitationUuids = extractSolicitationUuids(raw),
            uris = raw.uris.map { UriDecoder.decode(it) },
            broadcastName = raw.broadcastName?.decodeToString(),
            adStructures = raw.adStructures.mapValues { (_, list) -> list.map { it.toHexString() } },
        )
    }

    private fun enrichAddressingAndTiming(base: BleSignal.Sig, raw: RawAdvertisement): BleSignal.Sig {
        return base.copy(
            classOfDevice = raw.classOfDevice?.toHexString(),
            leRole = decodeLeRole(raw.leRole),
            leBluetoothDeviceAddress = decodeLeDeviceAddress(raw.leBluetoothDeviceAddress),
            publicTargetAddresses = decodeTargetAddresses(raw.publicTargetAddresses),
            randomTargetAddresses = decodeTargetAddresses(raw.randomTargetAddresses),
            advertisingInterval = decodeAdvertisingInterval(raw),
            peripheralConnectionIntervalRange = decodeConnectionIntervalRange(raw.peripheralConnectionIntervalRange),
        )
    }

    private fun extractDeviceName(raw: RawAdvertisement): String? {
        val nameBytes = raw.completeLocalName ?: raw.shortenedLocalName ?: return null
        return nameBytes.decodeToString()
    }

    private fun formatTxPower(bytes: ByteArray?): String? {
        if (bytes == null || bytes.isEmpty()) return null
        return "${bytes.readInt8(0)} dBm"
    }

    private fun resolveAppearance(bytes: ByteArray?): String? {
        if (bytes == null || bytes.size < APPEARANCE_BYTE_LENGTH) return null
        val code = bytes.readUInt16LE(0)
        return AppearanceLookup.resolve(code)
    }

    private fun parseFlagsFromBytes(bytes: ByteArray?): List<DiscoveryMode> {
        if (bytes == null || bytes.isEmpty()) return emptyList()
        return parseFlags(bytes[0].toInt() and BYTE_MASK)
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

    private fun extractServiceUuids(raw: RawAdvertisement): List<String> {
        return buildList {
            addAll(decodeUuid16List(raw.incompleteServiceUuids16Bit))
            addAll(decodeUuid16List(raw.completeServiceUuids16Bit))
            addAll(decodeUuid32List(raw.incompleteServiceUuids32Bit))
            addAll(decodeUuid32List(raw.completeServiceUuids32Bit))
            addAll(decodeUuid128List(raw.incompleteServiceUuids128Bit))
            addAll(decodeUuid128List(raw.completeServiceUuids128Bit))
            addAll(extractUuid128FromServiceData(raw.serviceData128Bit))
        }.distinct()
    }

    private fun extractSolicitationUuids(raw: RawAdvertisement): List<String> {
        return buildList {
            addAll(decodeUuid16List(raw.serviceSolicitationUuids16Bit))
            addAll(decodeUuid32List(raw.serviceSolicitationUuids32Bit))
            addAll(decodeUuid128List(raw.serviceSolicitationUuids128Bit))
        }.distinct()
    }

    private fun decodeUuid16List(entries: List<ByteArray>): List<String> {
        return buildList {
            for (bytes in entries) {
                var offset = 0
                while (offset + UUID_16_BYTE_LENGTH <= bytes.size) {
                    add(bytes.le16BitUuidAt(offset))
                    offset += UUID_16_BYTE_LENGTH
                }
            }
        }
    }

    private fun decodeUuid32List(entries: List<ByteArray>): List<String> {
        return buildList {
            for (bytes in entries) {
                var offset = 0
                while (offset + UUID_32_BYTE_LENGTH <= bytes.size) {
                    add(bytes.le32BitUuidAt(offset))
                    offset += UUID_32_BYTE_LENGTH
                }
            }
        }
    }

    private fun decodeUuid128List(entries: List<ByteArray>): List<String> {
        return buildList {
            for (bytes in entries) {
                var offset = 0
                while (offset + UUID_128_BYTE_LENGTH <= bytes.size) {
                    add(bytes.copyOfRange(offset, offset + UUID_128_BYTE_LENGTH).toUuid())
                    offset += UUID_128_BYTE_LENGTH
                }
            }
        }
    }

    private fun extractUuid128FromServiceData(entries: List<ByteArray>): List<String> {
        return entries
            .filter { it.size >= UUID_128_BYTE_LENGTH }
            .map { it.copyOfRange(0, UUID_128_BYTE_LENGTH).toUuid() }
    }

    private fun parseManufacturerData(entries: List<ByteArray>): List<ManufacturerData> {
        return entries
            .filter { it.size >= COMPANY_ID_BYTE_LENGTH }
            .map { bytes ->
                val companyId = bytes.readUInt16LE(0)
                val payload = bytes.copyOfRange(COMPANY_ID_BYTE_LENGTH, bytes.size).toHexString()
                ManufacturerData(
                    companyId = companyId,
                    companyName = CompanyIdentifierLookup.resolve(companyId),
                    payload = payload,
                )
            }
    }

    private fun parseServiceData(raw: RawAdvertisement): Map<String, String> {
        return buildMap {
            putServiceData16(raw.serviceData16Bit, this)
            putServiceData32(raw.serviceData32Bit, this)
            putServiceData128(raw.serviceData128Bit, this)
        }
    }

    private fun putServiceData16(
        entries: List<ByteArray>,
        destination: MutableMap<String, String>,
    ) {
        for (bytes in entries) {
            if (bytes.size < UUID_16_BYTE_LENGTH) continue
            val uuid = bytes.le16BitUuidAt(0)
            destination[uuid] = bytes.copyOfRange(UUID_16_BYTE_LENGTH, bytes.size).toHexString()
        }
    }

    private fun putServiceData32(
        entries: List<ByteArray>,
        destination: MutableMap<String, String>,
    ) {
        for (bytes in entries) {
            if (bytes.size < UUID_32_BYTE_LENGTH) continue
            val uuid = bytes.le32BitUuidAt(0)
            destination[uuid] = bytes.copyOfRange(UUID_32_BYTE_LENGTH, bytes.size).toHexString()
        }
    }

    private fun putServiceData128(
        entries: List<ByteArray>,
        destination: MutableMap<String, String>,
    ) {
        for (bytes in entries) {
            if (bytes.size < UUID_128_BYTE_LENGTH) continue
            val uuid = bytes.copyOfRange(0, UUID_128_BYTE_LENGTH).toUuid()
            destination[uuid] = bytes.copyOfRange(UUID_128_BYTE_LENGTH, bytes.size).toHexString()
        }
    }

    private fun decodeLeRole(bytes: ByteArray?): String? {
        if (bytes == null || bytes.isEmpty()) return null
        return when (bytes[0].toInt() and BYTE_MASK) {
            LE_ROLE_PERIPHERAL_ONLY -> "Peripheral Only"
            LE_ROLE_CENTRAL_ONLY -> "Central Only"
            LE_ROLE_PERIPHERAL_PREFERRED -> "Peripheral and Central (Peripheral Preferred)"
            LE_ROLE_CENTRAL_PREFERRED -> "Peripheral and Central (Central Preferred)"
            else -> "Unknown"
        }
    }

    private fun decodeLeDeviceAddress(bytes: ByteArray?): String? {
        if (bytes == null || bytes.size < LE_DEVICE_ADDRESS_BYTE_LENGTH) return null
        return bytes.formatMacAddressAt(0)
    }

    private fun decodeTargetAddresses(entries: List<ByteArray>): List<String> {
        return buildList {
            for (bytes in entries) {
                var offset = 0
                while (offset + MAC_ADDRESS_BYTE_LENGTH <= bytes.size) {
                    add(bytes.formatMacAddressAt(offset))
                    offset += MAC_ADDRESS_BYTE_LENGTH
                }
            }
        }
    }

    private fun decodeAdvertisingInterval(raw: RawAdvertisement): String? {
        val bytes = raw.advertisingInterval ?: raw.advertisingIntervalLong ?: return null
        return bytes.toHexString()
    }

    private fun decodeConnectionIntervalRange(bytes: ByteArray?): String? {
        if (bytes == null || bytes.size < CONNECTION_INTERVAL_BYTE_LENGTH) return null
        return bytes.toHexString()
    }
}

package com.kytrox.blescanner.parser

import com.kytrox.blescanner.domain.AdType

public const val MAX_LEGACY_ADVERTISEMENT_BYTES: Int = 31
public const val MAX_LEGACY_ADVERTISEMENT_HEX_CHARS: Int = 62
public const val FLAG_STRUCTURE_BYTE_LENGTH: Int = 3
public const val MAX_CUSTOM_BEACON_BYTES: Int = 28
public const val MAX_CUSTOM_BEACON_HEX_CHARS: Int = 56

/**
 * Platform-agnostic representation of a raw BLE advertisement packet.
 *
 * Stores the raw packet [rawBytes] and [rawHex] (up to 31 bytes in legacy
 * advertising, or 28 bytes / 56 hex chars of custom beacon data after the
 * 3-byte Flags AD structure), along with the routed [ByteArray] payloads
 * for every official Bluetooth SIG Common Data Type ([AdType]).
 */
public data class RawAdvertisement(
    val deviceAddress: String,
    val rssi: Int,
    val timestampMillis: Long,
    val rawBytes: ByteArray = ByteArray(0),
    val rawHex: String = rawBytes.toHexString(),
    val adStructures: Map<AdType, List<ByteArray>> = emptyMap(),
    val unknownAdStructures: Map<Int, List<ByteArray>> = emptyMap(),
    val flags: ByteArray? = adStructures.lastEntryOrNull(AdType.FLAGS),
    val incompleteServiceUuids16Bit: List<ByteArray> = adStructures.entriesOf(AdType.INCOMPLETE_SERVICE_UUIDS_16_BIT),
    val completeServiceUuids16Bit: List<ByteArray> = adStructures.entriesOf(AdType.COMPLETE_SERVICE_UUIDS_16_BIT),
    val incompleteServiceUuids32Bit: List<ByteArray> = adStructures.entriesOf(AdType.INCOMPLETE_SERVICE_UUIDS_32_BIT),
    val completeServiceUuids32Bit: List<ByteArray> = adStructures.entriesOf(AdType.COMPLETE_SERVICE_UUIDS_32_BIT),
    val incompleteServiceUuids128Bit: List<ByteArray> = adStructures.entriesOf(AdType.INCOMPLETE_SERVICE_UUIDS_128_BIT),
    val completeServiceUuids128Bit: List<ByteArray> = adStructures.entriesOf(AdType.COMPLETE_SERVICE_UUIDS_128_BIT),
    val shortenedLocalName: ByteArray? = adStructures.lastEntryOrNull(AdType.SHORTENED_LOCAL_NAME),
    val completeLocalName: ByteArray? = adStructures.lastEntryOrNull(AdType.COMPLETE_LOCAL_NAME),
    val txPowerLevel: ByteArray? = adStructures.lastEntryOrNull(AdType.TX_POWER_LEVEL),
    val classOfDevice: ByteArray? = adStructures.lastEntryOrNull(AdType.CLASS_OF_DEVICE),
    val simplePairingHashC192: ByteArray? = adStructures.lastEntryOrNull(AdType.SIMPLE_PAIRING_HASH_C192),
    val simplePairingRandomizerR192: ByteArray? = adStructures.lastEntryOrNull(AdType.SIMPLE_PAIRING_RANDOMIZER_R192),
    val deviceIdOrTkValue: ByteArray? = adStructures.lastEntryOrNull(AdType.DEVICE_ID_OR_TK_VALUE),
    val securityManagerOobFlags: ByteArray? = adStructures.lastEntryOrNull(AdType.SECURITY_MANAGER_OOB_FLAGS),
    val peripheralConnectionIntervalRange: ByteArray? = adStructures.lastEntryOrNull(AdType.PERIPHERAL_CONNECTION_INTERVAL_RANGE),
    val serviceSolicitationUuids16Bit: List<ByteArray> = adStructures.entriesOf(AdType.SERVICE_SOLICITATION_UUIDS_16_BIT),
    val serviceSolicitationUuids128Bit: List<ByteArray> = adStructures.entriesOf(AdType.SERVICE_SOLICITATION_UUIDS_128_BIT),
    val serviceData16Bit: List<ByteArray> = adStructures.entriesOf(AdType.SERVICE_DATA_16_BIT),
    val publicTargetAddresses: List<ByteArray> = adStructures.entriesOf(AdType.PUBLIC_TARGET_ADDRESS),
    val randomTargetAddresses: List<ByteArray> = adStructures.entriesOf(AdType.RANDOM_TARGET_ADDRESS),
    val appearance: ByteArray? = adStructures.lastEntryOrNull(AdType.APPEARANCE),
    val advertisingInterval: ByteArray? = adStructures.lastEntryOrNull(AdType.ADVERTISING_INTERVAL),
    val leBluetoothDeviceAddress: ByteArray? = adStructures.lastEntryOrNull(AdType.LE_BLUETOOTH_DEVICE_ADDRESS),
    val leRole: ByteArray? = adStructures.lastEntryOrNull(AdType.LE_ROLE),
    val simplePairingHashC256: ByteArray? = adStructures.lastEntryOrNull(AdType.SIMPLE_PAIRING_HASH_C256),
    val simplePairingRandomizerR256: ByteArray? = adStructures.lastEntryOrNull(AdType.SIMPLE_PAIRING_RANDOMIZER_R256),
    val serviceSolicitationUuids32Bit: List<ByteArray> = adStructures.entriesOf(AdType.SERVICE_SOLICITATION_UUIDS_32_BIT),
    val serviceData32Bit: List<ByteArray> = adStructures.entriesOf(AdType.SERVICE_DATA_32_BIT),
    val serviceData128Bit: List<ByteArray> = adStructures.entriesOf(AdType.SERVICE_DATA_128_BIT),
    val leSecureConnectionsConfirmation: ByteArray? = adStructures.lastEntryOrNull(AdType.LE_SECURE_CONNECTIONS_CONFIRMATION),
    val leSecureConnectionsRandom: ByteArray? = adStructures.lastEntryOrNull(AdType.LE_SECURE_CONNECTIONS_RANDOM),
    val uris: List<ByteArray> = adStructures.entriesOf(AdType.URI),
    val indoorPositioning: ByteArray? = adStructures.lastEntryOrNull(AdType.INDOOR_POSITIONING),
    val transportDiscoveryData: List<ByteArray> = adStructures.entriesOf(AdType.TRANSPORT_DISCOVERY_DATA),
    val leSupportedFeatures: ByteArray? = adStructures.lastEntryOrNull(AdType.LE_SUPPORTED_FEATURES),
    val channelMapUpdateIndication: ByteArray? = adStructures.lastEntryOrNull(AdType.CHANNEL_MAP_UPDATE_INDICATION),
    val pbAdv: List<ByteArray> = adStructures.entriesOf(AdType.PB_ADV),
    val meshMessages: List<ByteArray> = adStructures.entriesOf(AdType.MESH_MESSAGE),
    val meshBeacons: List<ByteArray> = adStructures.entriesOf(AdType.MESH_BEACON),
    val bigInfo: ByteArray? = adStructures.lastEntryOrNull(AdType.BIG_INFO),
    val broadcastCode: ByteArray? = adStructures.lastEntryOrNull(AdType.BROADCAST_CODE),
    val resolvableSetIdentifier: ByteArray? = adStructures.lastEntryOrNull(AdType.RESOLVABLE_SET_IDENTIFIER),
    val advertisingIntervalLong: ByteArray? = adStructures.lastEntryOrNull(AdType.ADVERTISING_INTERVAL_LONG),
    val broadcastName: ByteArray? = adStructures.lastEntryOrNull(AdType.BROADCAST_NAME),
    val encryptedAdvertisingData: List<ByteArray> = adStructures.entriesOf(AdType.ENCRYPTED_ADVERTISING_DATA),
    val periodicAdvertisingResponseTiming: ByteArray? = adStructures.lastEntryOrNull(AdType.PERIODIC_ADVERTISING_RESPONSE_TIMING),
    val electronicShelfLabel: ByteArray? = adStructures.lastEntryOrNull(AdType.ELECTRONIC_SHELF_LABEL),
    val threeDInformationData: ByteArray? = adStructures.lastEntryOrNull(AdType.THREE_D_INFORMATION_DATA),
    val manufacturerSpecificData: List<ByteArray> = adStructures.entriesOf(AdType.MANUFACTURER_SPECIFIC_DATA),
)

private fun Map<AdType, List<ByteArray>>.lastEntryOrNull(type: AdType): ByteArray? {
    return this[type]?.lastOrNull()
}

private fun Map<AdType, List<ByteArray>>.entriesOf(type: AdType): List<ByteArray> {
    return this[type] ?: emptyList()
}

package com.kytrox.ble_scanner_app.blescanner.data

import com.kytrox.ble_scanner_app.blescanner.data.dto.BleSignalPayloadKeys
import com.kytrox.ble_scanner_app.blescanner.data.dto.SignalProtocolType
import com.kytrox.blescanner.domain.BleSignal
import com.kytrox.blescanner.domain.ManufacturerData

class BleSignalPayloadMapper {

    fun mapToPayload(signal: BleSignal): Map<String, Any?> {
        return when (signal) {
            is BleSignal.Eddystone.Uid -> mapEddystoneUid(signal)
            is BleSignal.Eddystone.Url -> mapEddystoneUrl(signal)
            is BleSignal.Eddystone.Tlm -> mapEddystoneTlm(signal)
            is BleSignal.AltBeacon -> mapAltBeacon(signal)
            is BleSignal.Sig -> mapSig(signal)
        }
    }

    private fun mapEddystoneUid(signal: BleSignal.Eddystone.Uid): Map<String, Any?> {
        return buildBaseMap(signal, SignalProtocolType.EDDYSTONE_UID) + mapOf(
            BleSignalPayloadKeys.TX_POWER to signal.txPower,
            BleSignalPayloadKeys.NAMESPACE to signal.namespace,
            BleSignalPayloadKeys.INSTANCE to signal.instance,
        )
    }

    private fun mapEddystoneUrl(signal: BleSignal.Eddystone.Url): Map<String, Any?> {
        return buildBaseMap(signal, SignalProtocolType.EDDYSTONE_URL) + mapOf(
            BleSignalPayloadKeys.TX_POWER to signal.txPower,
            BleSignalPayloadKeys.URL to signal.url,
        )
    }

    private fun mapEddystoneTlm(signal: BleSignal.Eddystone.Tlm): Map<String, Any?> {
        return buildBaseMap(signal, SignalProtocolType.EDDYSTONE_TLM) + mapOf(
            BleSignalPayloadKeys.VERSION to signal.version,
            BleSignalPayloadKeys.BATTERY_MILLI_VOLTS to signal.batteryMilliVolts,
            BleSignalPayloadKeys.TEMPERATURE_CELSIUS to signal.temperatureCelsius.toDouble(),
            BleSignalPayloadKeys.ADVERTISEMENT_COUNT to signal.advertisementCount,
            BleSignalPayloadKeys.UPTIME_SECONDS to signal.uptimeSeconds.toDouble(),
        )
    }

    private fun mapAltBeacon(signal: BleSignal.AltBeacon): Map<String, Any?> {
        return buildBaseMap(signal, SignalProtocolType.ALT_BEACON) + mapOf(
            BleSignalPayloadKeys.MANUFACTURER_ID to signal.manufacturerId,
            BleSignalPayloadKeys.MANUFACTURER_NAME to signal.manufacturerName,
            BleSignalPayloadKeys.BEACON_ID to signal.beaconId,
            BleSignalPayloadKeys.REF_RSSI to signal.refRssi,
            BleSignalPayloadKeys.MFG_RESERVED to signal.mfgReserved,
        )
    }

    private fun mapSig(signal: BleSignal.Sig): Map<String, Any?> {
        return buildBaseMap(signal, SignalProtocolType.SIG) +
            buildSigIdentityFields(signal) +
            buildSigPayloadFields(signal)
    }

    private fun buildSigIdentityFields(signal: BleSignal.Sig): Map<String, Any?> {
        val resolvedName = signal.deviceName ?: signal.completeLocalName ?: signal.shortenedLocalName
        return mapOf(
            BleSignalPayloadKeys.DEVICE_NAME to resolvedName,
            BleSignalPayloadKeys.TX_POWER_LEVEL to signal.txPowerLevel,
            BleSignalPayloadKeys.APPEARANCE to signal.appearance,
            BleSignalPayloadKeys.LE_ROLE to signal.leRole,
            BleSignalPayloadKeys.BROADCAST_NAME to signal.broadcastName,
            BleSignalPayloadKeys.ADVERTISING_INTERVAL to signal.advertisingInterval,
        )
    }

    private fun buildSigPayloadFields(signal: BleSignal.Sig): Map<String, Any?> {
        return mapOf(
            BleSignalPayloadKeys.SERVICE_UUIDS to signal.serviceUuids,
            BleSignalPayloadKeys.DISCOVERY_MODES to signal.discoveryModes.map { it.displayName },
            BleSignalPayloadKeys.MANUFACTURER_DATA to signal.manufacturerSpecificData.map(::mapManufacturer),
            BleSignalPayloadKeys.SERVICE_DATA to signal.serviceData,
            BleSignalPayloadKeys.URIS to signal.uris,
        )
    }

    private fun mapManufacturer(manufacturerData: ManufacturerData): Map<String, Any> {
        return mapOf(
            BleSignalPayloadKeys.COMPANY_ID to manufacturerData.companyId,
            BleSignalPayloadKeys.COMPANY_NAME to manufacturerData.companyName,
            BleSignalPayloadKeys.PAYLOAD to manufacturerData.payload,
        )
    }

    private fun buildBaseMap(
        signal: BleSignal,
        protocolType: SignalProtocolType,
    ): Map<String, Any> {
        return mapOf(
            BleSignalPayloadKeys.TYPE to protocolType.wireValue,
            BleSignalPayloadKeys.DEVICE_ADDRESS to signal.deviceAddress,
            BleSignalPayloadKeys.RSSI to signal.rssi,
            BleSignalPayloadKeys.TIMESTAMP_MILLIS to signal.timestampMillis,
        )
    }
}


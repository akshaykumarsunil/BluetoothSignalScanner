import Foundation
import BleSignalScanner

public final class IosBleSignalPayloadMapper {

    public init() {}

    public func mapToPayload(signal: any BleSignal) -> [String: Any] {
        if let uidSignal = signal as? BleSignalEddystoneUid { return mapEddystoneUid(uidSignal) }
        if let urlSignal = signal as? BleSignalEddystoneUrl { return mapEddystoneUrl(urlSignal) }
        if let tlmSignal = signal as? BleSignalEddystoneTlm { return mapEddystoneTlm(tlmSignal) }
        if let altSignal = signal as? BleSignalAltBeacon { return mapAltBeacon(altSignal) }
        if let sigSignal = signal as? BleSignalSig { return mapSig(sigSignal) }
        return buildBaseDictionary(signal: signal, protocolType: .sig)
    }

    private func mapEddystoneUid(_ signal: BleSignalEddystoneUid) -> [String: Any] {
        var payload = buildBaseDictionary(signal: signal, protocolType: .eddystoneUid)
        payload[IosBleSignalPayloadKeys.txPower] = signal.txPower
        payload[IosBleSignalPayloadKeys.namespace] = signal.namespace
        payload[IosBleSignalPayloadKeys.instance] = signal.instance
        return payload
    }

    private func mapEddystoneUrl(_ signal: BleSignalEddystoneUrl) -> [String: Any] {
        var payload = buildBaseDictionary(signal: signal, protocolType: .eddystoneUrl)
        payload[IosBleSignalPayloadKeys.txPower] = signal.txPower
        payload[IosBleSignalPayloadKeys.url] = signal.url
        return payload
    }

    private func mapEddystoneTlm(_ signal: BleSignalEddystoneTlm) -> [String: Any] {
        var payload = buildBaseDictionary(signal: signal, protocolType: .eddystoneTlm)
        payload[IosBleSignalPayloadKeys.version] = signal.version
        payload[IosBleSignalPayloadKeys.batteryMilliVolts] = signal.batteryMilliVolts
        payload[IosBleSignalPayloadKeys.temperatureCelsius] = Double(signal.temperatureCelsius)
        payload[IosBleSignalPayloadKeys.advertisementCount] = signal.advertisementCount
        payload[IosBleSignalPayloadKeys.uptimeSeconds] = Double(signal.uptimeSeconds)
        return payload
    }

    private func mapAltBeacon(_ signal: BleSignalAltBeacon) -> [String: Any] {
        var payload = buildBaseDictionary(signal: signal, protocolType: .altBeacon)
        payload[IosBleSignalPayloadKeys.manufacturerId] = signal.manufacturerId
        payload[IosBleSignalPayloadKeys.manufacturerName] = signal.manufacturerName
        payload[IosBleSignalPayloadKeys.beaconId] = signal.beaconId
        payload[IosBleSignalPayloadKeys.refRssi] = signal.refRssi
        payload[IosBleSignalPayloadKeys.mfgReserved] = signal.mfgReserved
        return payload
    }

    private func mapSig(_ signal: BleSignalSig) -> [String: Any] {
        var payload = buildBaseDictionary(signal: signal, protocolType: .sig)
        populateSigIdentityFields(from: signal, into: &payload)
        populateSigCollectionFields(from: signal, into: &payload)
        return payload
    }

    private func populateSigIdentityFields(from signal: BleSignalSig, into payload: inout [String: Any]) {
        let resolvedName = signal.deviceName ?? signal.completeLocalName ?? signal.shortenedLocalName
        payload[IosBleSignalPayloadKeys.deviceName] = resolvedName
        payload[IosBleSignalPayloadKeys.txPowerLevel] = signal.txPowerLevel
        payload[IosBleSignalPayloadKeys.appearance] = signal.appearance
        payload[IosBleSignalPayloadKeys.leRole] = signal.leRole
        payload[IosBleSignalPayloadKeys.broadcastName] = signal.broadcastName
        payload[IosBleSignalPayloadKeys.advertisingInterval] = signal.advertisingInterval
    }

    private func populateSigCollectionFields(from signal: BleSignalSig, into payload: inout [String: Any]) {
        payload[IosBleSignalPayloadKeys.serviceUuids] = signal.serviceUuids
        payload[IosBleSignalPayloadKeys.discoveryModes] = signal.discoveryModes.map { $0.displayName }
        payload[IosBleSignalPayloadKeys.manufacturerData] = signal.manufacturerSpecificData.map(mapManufacturer)
        payload[IosBleSignalPayloadKeys.serviceData] = signal.serviceData
        payload[IosBleSignalPayloadKeys.uris] = signal.uris
    }

    private func mapManufacturer(_ item: ManufacturerData) -> [String: Any] {
        return [
            IosBleSignalPayloadKeys.companyId: item.companyId,
            IosBleSignalPayloadKeys.companyName: item.companyName,
            IosBleSignalPayloadKeys.payload: item.payload,
        ]
    }

    private func buildBaseDictionary(
        signal: any BleSignal,
        protocolType: IosSignalProtocolType
    ) -> [String: Any] {
        return [
            IosBleSignalPayloadKeys.type: protocolType.rawValue,
            IosBleSignalPayloadKeys.deviceAddress: signal.deviceAddress,
            IosBleSignalPayloadKeys.rssi: signal.rssi,
            IosBleSignalPayloadKeys.timestampMillis: signal.timestampMillis,
        ]
    }
}


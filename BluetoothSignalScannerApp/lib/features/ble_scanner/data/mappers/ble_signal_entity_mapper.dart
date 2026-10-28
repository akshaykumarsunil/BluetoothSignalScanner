import '../../domain/entities/ble_protocol_type.dart';
import '../../domain/entities/ble_signal_entity.dart';
import '../../domain/entities/manufacturer_entry.dart';
import '../dto/ble_signal_dto.dart';
import '../dto/ble_signal_payload_keys.dart';
import '../dto/manufacturer_data_dto.dart';

class BleSignalEntityMapper {
  const BleSignalEntityMapper();

  BleSignalEntity mapToEntity(BleSignalDto dto) {
    final protocol = BleProtocolType.fromWireValue(dto.type);
    return switch (protocol) {
      BleProtocolType.eddystoneUid => _mapEddystoneUid(dto),
      BleProtocolType.eddystoneUrl => _mapEddystoneUrl(dto),
      BleProtocolType.eddystoneTlm => _mapEddystoneTlm(dto),
      BleProtocolType.altBeacon => _mapAltBeacon(dto),
      BleProtocolType.sig => _mapSig(dto),
    };
  }

  EddystoneUidSignal _mapEddystoneUid(BleSignalDto dto) {
    final m = dto.beaconMetrics;
    return EddystoneUidSignal(
      deviceAddress: dto.deviceAddress,
      rssi: dto.rssi,
      timestampMillis: dto.timestampMillis,
      txPower: m.txPower ?? BleSignalPayloadKeys.zeroNumericFallback,
      namespaceId: m.namespaceId ?? BleSignalPayloadKeys.emptyTextFallback,
      instanceId: m.instanceId ?? BleSignalPayloadKeys.emptyTextFallback,
    );
  }

  EddystoneUrlSignal _mapEddystoneUrl(BleSignalDto dto) {
    final m = dto.beaconMetrics;
    return EddystoneUrlSignal(
      deviceAddress: dto.deviceAddress,
      rssi: dto.rssi,
      timestampMillis: dto.timestampMillis,
      txPower: m.txPower ?? BleSignalPayloadKeys.zeroNumericFallback,
      url: m.url ?? BleSignalPayloadKeys.emptyTextFallback,
    );
  }

  EddystoneTlmSignal _mapEddystoneTlm(BleSignalDto dto) {
    final m = dto.beaconMetrics;
    return EddystoneTlmSignal(
      deviceAddress: dto.deviceAddress,
      rssi: dto.rssi,
      timestampMillis: dto.timestampMillis,
      version: m.version ?? BleSignalPayloadKeys.zeroNumericFallback,
      batteryMilliVolts: m.batteryMilliVolts ?? BleSignalPayloadKeys.zeroNumericFallback,
      temperatureCelsius: m.temperatureCelsius ?? BleSignalPayloadKeys.zeroDecimalFallback,
      advertisementCount: m.advertisementCount ?? BleSignalPayloadKeys.zeroNumericFallback,
      uptimeSeconds: m.uptimeSeconds ?? BleSignalPayloadKeys.zeroDecimalFallback,
    );
  }

  AltBeaconSignal _mapAltBeacon(BleSignalDto dto) {
    final m = dto.beaconMetrics;
    return AltBeaconSignal(
      deviceAddress: dto.deviceAddress,
      rssi: dto.rssi,
      timestampMillis: dto.timestampMillis,
      manufacturerId: m.manufacturerId ?? BleSignalPayloadKeys.zeroNumericFallback,
      manufacturerName: m.manufacturerName ?? BleSignalPayloadKeys.emptyTextFallback,
      beaconId: m.beaconId ?? BleSignalPayloadKeys.emptyTextFallback,
      refRssi: m.refRssi ?? BleSignalPayloadKeys.zeroNumericFallback,
      mfgReserved: m.mfgReserved ?? BleSignalPayloadKeys.zeroNumericFallback,
    );
  }

  SigBleSignal _mapSig(BleSignalDto dto) {
    final meta = dto.sigMetadata;
    final mfg = meta.manufacturerData.map(_mapManufacturer).toList(growable: false);
    return SigBleSignal(
      deviceAddress: dto.deviceAddress,
      rssi: dto.rssi,
      timestampMillis: dto.timestampMillis,
      deviceName: meta.deviceName,
      txPowerLevel: meta.txPowerLevel,
      serviceUuids: meta.serviceUuids,
      appearance: meta.appearance,
      discoveryModes: meta.discoveryModes,
      manufacturerData: mfg,
      serviceData: meta.serviceData,
      uris: meta.uris,
      advertisingInterval: meta.advertisingInterval,
      leRole: meta.leRole,
      broadcastName: meta.broadcastName,
    );
  }

  ManufacturerEntry _mapManufacturer(ManufacturerDataDto dto) {
    return ManufacturerEntry(
      companyId: dto.companyId,
      companyName: dto.companyName,
      payloadHex: dto.payload,
    );
  }
}

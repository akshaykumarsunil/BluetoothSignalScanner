import '../../../../core/errors/ble_scan_exception.dart';
import '../../domain/entities/ble_protocol_type.dart';
import '../dto/ble_signal_dto.dart';
import '../dto/ble_signal_payload_keys.dart';
import '../dto/manufacturer_data_dto.dart';

class BleSignalDtoParser {
  const BleSignalDtoParser();

  BleSignalDto parseFromMap(Map<Object?, Object?> rawMap) {
    if (rawMap.isEmpty) {
      throw const BleScanException(
        code: BleScanException.payloadParseErrorCode,
        message: 'Received empty BLE signal payload map.',
      );
    }
    return _constructDto(rawMap);
  }

  BleSignalDto _constructDto(Map<Object?, Object?> raw) {
    return BleSignalDto(
      type: _readString(raw, BleSignalPayloadKeys.type) ?? BleProtocolType.sig.wireValue,
      deviceAddress: _readAddress(raw),
      rssi: _readInt(raw, BleSignalPayloadKeys.rssi) ?? BleSignalPayloadKeys.zeroNumericFallback,
      timestampMillis: _readTimestamp(raw),
      beaconMetrics: _parseBeaconMetrics(raw),
      sigMetadata: _parseSigMetadata(raw),
    );
  }

  BeaconMetricsDto _parseBeaconMetrics(Map<Object?, Object?> raw) {
    return BeaconMetricsDto(
      txPower: _readInt(raw, BleSignalPayloadKeys.txPower),
      namespaceId: _readString(raw, BleSignalPayloadKeys.namespace),
      instanceId: _readString(raw, BleSignalPayloadKeys.instance),
      url: _readString(raw, BleSignalPayloadKeys.url),
      version: _readInt(raw, BleSignalPayloadKeys.version),
      batteryMilliVolts: _readInt(raw, BleSignalPayloadKeys.batteryMilliVolts),
      temperatureCelsius: _readDouble(raw, BleSignalPayloadKeys.temperatureCelsius),
      advertisementCount: _readInt(raw, BleSignalPayloadKeys.advertisementCount),
      uptimeSeconds: _readDouble(raw, BleSignalPayloadKeys.uptimeSeconds),
      manufacturerId: _readInt(raw, BleSignalPayloadKeys.manufacturerId),
      manufacturerName: _readString(raw, BleSignalPayloadKeys.manufacturerName),
      beaconId: _readString(raw, BleSignalPayloadKeys.beaconId),
      refRssi: _readInt(raw, BleSignalPayloadKeys.refRssi),
      mfgReserved: _readInt(raw, BleSignalPayloadKeys.mfgReserved),
    );
  }

  SigMetadataDto _parseSigMetadata(Map<Object?, Object?> raw) {
    return SigMetadataDto(
      deviceName: _readString(raw, BleSignalPayloadKeys.deviceName),
      txPowerLevel: _readString(raw, BleSignalPayloadKeys.txPowerLevel),
      serviceUuids: _readStringList(raw, BleSignalPayloadKeys.serviceUuids),
      appearance: _readString(raw, BleSignalPayloadKeys.appearance),
      discoveryModes: _readStringList(raw, BleSignalPayloadKeys.discoveryModes),
      manufacturerData: _readManufacturerList(raw),
      serviceData: _readStringMap(raw, BleSignalPayloadKeys.serviceData),
      uris: _readStringList(raw, BleSignalPayloadKeys.uris),
      advertisingInterval: _readString(raw, BleSignalPayloadKeys.advertisingInterval),
      leRole: _readString(raw, BleSignalPayloadKeys.leRole),
      broadcastName: _readString(raw, BleSignalPayloadKeys.broadcastName),
    );
  }

  String _readAddress(Map<Object?, Object?> rawMap) {
    return _readString(rawMap, BleSignalPayloadKeys.deviceAddress) ??
        BleSignalPayloadKeys.unknownAddressFallback;
  }

  int _readTimestamp(Map<Object?, Object?> rawMap) {
    return _readInt(rawMap, BleSignalPayloadKeys.timestampMillis) ??
        BleSignalPayloadKeys.zeroNumericFallback;
  }

  String? _readString(Map<Object?, Object?> rawMap, String key) {
    final value = rawMap[key];
    if (value is String) return value;
    return null;
  }

  int? _readInt(Map<Object?, Object?> rawMap, String key) {
    final value = rawMap[key];
    if (value is int) return value;
    if (value is num) return value.toInt();
    return null;
  }

  double? _readDouble(Map<Object?, Object?> rawMap, String key) {
    final value = rawMap[key];
    if (value is double) return value;
    if (value is num) return value.toDouble();
    return null;
  }

  List<String> _readStringList(Map<Object?, Object?> rawMap, String key) {
    final value = rawMap[key];
    if (value is! List) return const <String>[];
    return value.whereType<String>().toList(growable: false);
  }

  Map<String, String> _readStringMap(Map<Object?, Object?> rawMap, String key) {
    final value = rawMap[key];
    if (value is! Map) return const <String, String>{};
    return value.map((k, v) => MapEntry(k.toString(), v.toString()));
  }

  List<ManufacturerDataDto> _readManufacturerList(Map<Object?, Object?> rawMap) {
    final value = rawMap[BleSignalPayloadKeys.manufacturerData];
    if (value is! List) return const <ManufacturerDataDto>[];
    return value.whereType<Map<Object?, Object?>>().map(_parseManufacturerDto).toList();
  }

  ManufacturerDataDto _parseManufacturerDto(Map<Object?, Object?> item) {
    return ManufacturerDataDto(
      companyId: _readInt(item, BleSignalPayloadKeys.companyId) ??
          BleSignalPayloadKeys.zeroNumericFallback,
      companyName: _readString(item, BleSignalPayloadKeys.companyName) ??
          BleSignalPayloadKeys.emptyTextFallback,
      payload: _readString(item, BleSignalPayloadKeys.payload) ??
          BleSignalPayloadKeys.emptyTextFallback,
    );
  }
}

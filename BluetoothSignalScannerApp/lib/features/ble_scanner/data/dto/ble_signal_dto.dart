import 'manufacturer_data_dto.dart';

class BeaconMetricsDto {
  final int? txPower;
  final String? namespaceId;
  final String? instanceId;
  final String? url;
  final int? version;
  final int? batteryMilliVolts;
  final double? temperatureCelsius;
  final int? advertisementCount;
  final double? uptimeSeconds;
  final int? manufacturerId;
  final String? manufacturerName;
  final String? beaconId;
  final int? refRssi;
  final int? mfgReserved;

  const BeaconMetricsDto({
    this.txPower,
    this.namespaceId,
    this.instanceId,
    this.url,
    this.version,
    this.batteryMilliVolts,
    this.temperatureCelsius,
    this.advertisementCount,
    this.uptimeSeconds,
    this.manufacturerId,
    this.manufacturerName,
    this.beaconId,
    this.refRssi,
    this.mfgReserved,
  });
}

class SigMetadataDto {
  final String? deviceName;
  final String? txPowerLevel;
  final List<String> serviceUuids;
  final String? appearance;
  final List<String> discoveryModes;
  final List<ManufacturerDataDto> manufacturerData;
  final Map<String, String> serviceData;
  final List<String> uris;
  final String? advertisingInterval;
  final String? leRole;
  final String? broadcastName;

  const SigMetadataDto({
    this.deviceName,
    this.txPowerLevel,
    this.serviceUuids = const <String>[],
    this.appearance,
    this.discoveryModes = const <String>[],
    this.manufacturerData = const <ManufacturerDataDto>[],
    this.serviceData = const <String, String>{},
    this.uris = const <String>[],
    this.advertisingInterval,
    this.leRole,
    this.broadcastName,
  });
}

class BleSignalDto {
  final String type;
  final String deviceAddress;
  final int rssi;
  final int timestampMillis;
  final BeaconMetricsDto beaconMetrics;
  final SigMetadataDto sigMetadata;

  const BleSignalDto({
    required this.type,
    required this.deviceAddress,
    required this.rssi,
    required this.timestampMillis,
    required this.beaconMetrics,
    required this.sigMetadata,
  });
}

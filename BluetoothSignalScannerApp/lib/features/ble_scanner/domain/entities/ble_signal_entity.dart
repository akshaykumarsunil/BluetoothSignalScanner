import 'ble_protocol_type.dart';
import 'manufacturer_entry.dart';
import 'rssi_signal_quality.dart';

sealed class BleSignalEntity {
  final String deviceAddress;
  final int rssi;
  final int timestampMillis;
  final BleProtocolType protocolType;

  const BleSignalEntity({
    required this.deviceAddress,
    required this.rssi,
    required this.timestampMillis,
    required this.protocolType,
  });

  String get headlineTitle;

  RssiSignalQuality get signalQuality => RssiSignalQuality.fromRssi(rssi);
}

final class EddystoneUidSignal extends BleSignalEntity {
  final int txPower;
  final String namespaceId;
  final String instanceId;

  const EddystoneUidSignal({
    required super.deviceAddress,
    required super.rssi,
    required super.timestampMillis,
    required this.txPower,
    required this.namespaceId,
    required this.instanceId,
  }) : super(protocolType: BleProtocolType.eddystoneUid);

  @override
  String get headlineTitle => 'Instance: $instanceId';
}

final class EddystoneUrlSignal extends BleSignalEntity {
  final int txPower;
  final String url;

  const EddystoneUrlSignal({
    required super.deviceAddress,
    required super.rssi,
    required super.timestampMillis,
    required this.txPower,
    required this.url,
  }) : super(protocolType: BleProtocolType.eddystoneUrl);

  @override
  String get headlineTitle => url;
}

final class EddystoneTlmSignal extends BleSignalEntity {
  final int version;
  final int batteryMilliVolts;
  final double temperatureCelsius;
  final int advertisementCount;
  final double uptimeSeconds;

  const EddystoneTlmSignal({
    required super.deviceAddress,
    required super.rssi,
    required super.timestampMillis,
    required this.version,
    required this.batteryMilliVolts,
    required this.temperatureCelsius,
    required this.advertisementCount,
    required this.uptimeSeconds,
  }) : super(protocolType: BleProtocolType.eddystoneTlm);

  @override
  String get headlineTitle => 'Telemetry Frame (v$version)';
}

final class AltBeaconSignal extends BleSignalEntity {
  final int manufacturerId;
  final String manufacturerName;
  final String beaconId;
  final int refRssi;
  final int mfgReserved;

  const AltBeaconSignal({
    required super.deviceAddress,
    required super.rssi,
    required super.timestampMillis,
    required this.manufacturerId,
    required this.manufacturerName,
    required this.beaconId,
    required this.refRssi,
    required this.mfgReserved,
  }) : super(protocolType: BleProtocolType.altBeacon);

  @override
  String get headlineTitle => '$manufacturerName AltBeacon';
}

final class SigBleSignal extends BleSignalEntity {
  final String? deviceName;
  final String? txPowerLevel;
  final List<String> serviceUuids;
  final String? appearance;
  final List<String> discoveryModes;
  final List<ManufacturerEntry> manufacturerData;
  final Map<String, String> serviceData;
  final List<String> uris;
  final String? advertisingInterval;
  final String? leRole;
  final String? broadcastName;

  const SigBleSignal({
    required super.deviceAddress,
    required super.rssi,
    required super.timestampMillis,
    this.deviceName,
    this.txPowerLevel,
    this.serviceUuids = const <String>[],
    this.appearance,
    this.discoveryModes = const <String>[],
    this.manufacturerData = const <ManufacturerEntry>[],
    this.serviceData = const <String, String>{},
    this.uris = const <String>[],
    this.advertisingInterval,
    this.leRole,
    this.broadcastName,
  }) : super(protocolType: BleProtocolType.sig);

  @override
  String get headlineTitle {
    if (deviceName != null && deviceName!.isNotEmpty) return deviceName!;
    if (broadcastName != null && broadcastName!.isNotEmpty) return broadcastName!;
    if (manufacturerData.isNotEmpty) return manufacturerData.first.companyName;
    return 'Unnamed BLE Peripheral';
  }
}


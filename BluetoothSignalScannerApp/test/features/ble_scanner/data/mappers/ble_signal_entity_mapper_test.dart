import 'package:ble_scanner_app/features/ble_scanner/data/dto/ble_signal_dto.dart';
import 'package:ble_scanner_app/features/ble_scanner/data/mappers/ble_signal_entity_mapper.dart';
import 'package:ble_scanner_app/features/ble_scanner/domain/entities/ble_signal_entity.dart';
import 'package:flutter_test/flutter_test.dart';

const String _testDeviceAddress = '11:22:33:44:55:66';
const int _testRssi = -64;
const int _testTimestamp = 1730001000;
const int _testTxPower = -18;
const int _testBatteryMv = 3100;
const double _testTempCelsius = 24.5;
const int _testAdvCount = 420;
const double _testUptimeSec = 3600.0;

void main() {
  group('BleSignalEntityMapper', _registerMapperTests);
}

void _registerMapperTests() {
  test('maps Eddystone URL DTO to EddystoneUrlSignal', _testEddystoneUrlMapping);
  test('maps Eddystone TLM DTO to EddystoneTlmSignal', _testEddystoneTlmMapping);
  test('maps AltBeacon DTO to AltBeaconSignal', _testAltBeaconMapping);
}

void _testEddystoneUrlMapping() {
  const mapper = BleSignalEntityMapper();
  final entity = mapper.mapToEntity(_createEddystoneUrlDto());
  expect(entity, isA<EddystoneUrlSignal>());
  expect((entity as EddystoneUrlSignal).url, 'https://kytrox.com');
}

void _testEddystoneTlmMapping() {
  const mapper = BleSignalEntityMapper();
  final entity = mapper.mapToEntity(_createEddystoneTlmDto());
  expect(entity, isA<EddystoneTlmSignal>());
  expect((entity as EddystoneTlmSignal).batteryMilliVolts, _testBatteryMv);
}

void _testAltBeaconMapping() {
  const mapper = BleSignalEntityMapper();
  final entity = mapper.mapToEntity(_createAltBeaconDto());
  expect(entity, isA<AltBeaconSignal>());
  expect((entity as AltBeaconSignal).beaconId, 'BEAC-01');
}

BleSignalDto _createEddystoneUrlDto() {
  return const BleSignalDto(
    type: 'EDDYSTONE_URL',
    deviceAddress: _testDeviceAddress,
    rssi: _testRssi,
    timestampMillis: _testTimestamp,
    beaconMetrics: BeaconMetricsDto(txPower: _testTxPower, url: 'https://kytrox.com'),
    sigMetadata: SigMetadataDto(),
  );
}

BleSignalDto _createEddystoneTlmDto() {
  return const BleSignalDto(
    type: 'EDDYSTONE_TLM',
    deviceAddress: _testDeviceAddress,
    rssi: _testRssi,
    timestampMillis: _testTimestamp,
    beaconMetrics: BeaconMetricsDto(
      batteryMilliVolts: _testBatteryMv,
      temperatureCelsius: _testTempCelsius,
      advertisementCount: _testAdvCount,
      uptimeSeconds: _testUptimeSec,
    ),
    sigMetadata: SigMetadataDto(),
  );
}

BleSignalDto _createAltBeaconDto() {
  return const BleSignalDto(
    type: 'ALT_BEACON',
    deviceAddress: _testDeviceAddress,
    rssi: _testRssi,
    timestampMillis: _testTimestamp,
    beaconMetrics: BeaconMetricsDto(
      manufacturerName: 'Radius Networks',
      beaconId: 'BEAC-01',
    ),
    sigMetadata: SigMetadataDto(),
  );
}


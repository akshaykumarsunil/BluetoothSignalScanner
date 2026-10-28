import 'package:ble_scanner_app/core/errors/ble_scan_exception.dart';
import 'package:ble_scanner_app/features/ble_scanner/data/dto/ble_signal_payload_keys.dart';
import 'package:ble_scanner_app/features/ble_scanner/data/mappers/ble_signal_dto_parser.dart';
import 'package:flutter_test/flutter_test.dart';

const String _sampleAddress = 'AA:BB:CC:DD:EE:FF';
const int _sampleRssi = -58;
const int _sampleTimestamp = 1730000000;
const int _sampleCompanyId = 76;

void main() {
  group('BleSignalDtoParser', _registerParserTests);
}

void _registerParserTests() {
  test('throws BleScanException when payload map is empty', _testEmptyPayloadThrows);
  test('parses SIG BLE payload map with manufacturer data', _testSigPayloadParsing);
}

void _testEmptyPayloadThrows() {
  const parser = BleSignalDtoParser();
  expect(
    () => parser.parseFromMap(<Object?, Object?>{}),
    throwsA(isA<BleScanException>()),
  );
}

void _testSigPayloadParsing() {
  const parser = BleSignalDtoParser();
  final dto = parser.parseFromMap(_buildSampleSigPayload());
  expect(dto.deviceAddress, _sampleAddress);
  expect(dto.rssi, _sampleRssi);
  expect(dto.sigMetadata.deviceName, 'Heart Rate Monitor');
  expect(dto.sigMetadata.manufacturerData.first.companyName, 'Apple, Inc.');
}

Map<Object?, Object?> _buildSampleSigPayload() {
  return <Object?, Object?>{
    BleSignalPayloadKeys.type: 'SIG',
    BleSignalPayloadKeys.deviceAddress: _sampleAddress,
    BleSignalPayloadKeys.rssi: _sampleRssi,
    BleSignalPayloadKeys.timestampMillis: _sampleTimestamp,
    BleSignalPayloadKeys.deviceName: 'Heart Rate Monitor',
    BleSignalPayloadKeys.manufacturerData: <Object?>[
      <Object?, Object?>{
        BleSignalPayloadKeys.companyId: _sampleCompanyId,
        BleSignalPayloadKeys.companyName: 'Apple, Inc.',
        BleSignalPayloadKeys.payload: '0215',
      },
    ],
  };
}


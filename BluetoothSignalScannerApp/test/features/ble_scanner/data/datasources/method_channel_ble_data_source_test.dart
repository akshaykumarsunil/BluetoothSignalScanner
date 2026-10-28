import 'package:ble_scanner_app/core/constants/ble_channel_constants.dart';
import 'package:ble_scanner_app/core/platform/target_platform_resolver.dart';
import 'package:ble_scanner_app/features/ble_scanner/data/datasources/method_channel_ble_data_source.dart';
import 'package:ble_scanner_app/features/ble_scanner/data/dto/ble_signal_payload_keys.dart';
import 'package:ble_scanner_app/features/ble_scanner/data/mappers/ble_signal_dto_parser.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

const MethodChannel _testChannel = MethodChannel(BleChannelConstants.channelName);
const int _testScanDuration = 5;
const int _mockRssi = -50;
const int _mockTimestamp = 1730002000;

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();
  group('MethodChannelBleDataSource', _registerDataSourceTests);
}

void _registerDataSourceTests() {
  tearDown(_clearMockMethodCallHandler);
  test('invokes Android native scan when running on Android', _testAndroidChannelScan);
  test('invokes iOS native scan when running on iOS', _testIosChannelScan);
}

void _clearMockMethodCallHandler() {
  TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
      .setMockMethodCallHandler(_testChannel, null);
}

Future<void> _testAndroidChannelScan() async {
  _installMockScanChannel();
  final dataSource = _createDataSource(TargetPlatform.android);
  final results = await dataSource.invokeNativeScan(durationSeconds: _testScanDuration);
  final engineLabel = await dataSource.fetchPlatformScannerInfo();
  expect(results.single.deviceAddress, 'AA:11:BB:22:CC:33');
  expect(engineLabel, contains('Android'));
}

Future<void> _testIosChannelScan() async {
  _installMockScanChannel();
  final dataSource = _createDataSource(TargetPlatform.iOS);
  final results = await dataSource.invokeNativeScan(durationSeconds: _testScanDuration);
  final engineLabel = await dataSource.fetchPlatformScannerInfo();
  expect(results.single.deviceAddress, 'AA:11:BB:22:CC:33');
  expect(engineLabel, contains('iOS'));
}

MethodChannelBleDataSource _createDataSource(TargetPlatform platform) {
  return MethodChannelBleDataSource(
    methodChannel: _testChannel,
    platformResolver: TargetPlatformResolver(platformProvider: () => platform),
    dtoParser: const BleSignalDtoParser(),
  );
}

void _installMockScanChannel() {
  TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
      .setMockMethodCallHandler(_testChannel, _handleMockMethodCall);
}

Future<Object?> _handleMockMethodCall(MethodCall call) async {
  if (call.method == BleChannelConstants.methodScanSignals) {
    return <Object?>[
      <Object?, Object?>{
        BleSignalPayloadKeys.type: 'SIG',
        BleSignalPayloadKeys.deviceAddress: 'AA:11:BB:22:CC:33',
        BleSignalPayloadKeys.rssi: _mockRssi,
        BleSignalPayloadKeys.timestampMillis: _mockTimestamp,
      },
    ];
  }
  return null;
}

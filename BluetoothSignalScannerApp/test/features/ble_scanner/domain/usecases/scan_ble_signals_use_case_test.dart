import 'package:ble_scanner_app/core/errors/ble_scan_exception.dart';
import 'package:ble_scanner_app/features/ble_scanner/domain/entities/ble_signal_entity.dart';
import 'package:ble_scanner_app/features/ble_scanner/domain/repositories/ble_signal_repository.dart';
import 'package:ble_scanner_app/features/ble_scanner/domain/usecases/scan_ble_signals_use_case.dart';
import 'package:flutter_test/flutter_test.dart';

const int _weakRssi = -85;
const int _strongRssi = -48;
const int _zeroDuration = 0;
const int _validDuration = 12;
const int _sampleTimestamp = 1730004000;

class _FakeBleSignalRepository implements BleSignalRepository {
  @override
  Future<String> resolveActiveScannerEngineLabel() async => 'Test Scanner';

  @override
  Future<List<BleSignalEntity>> scanBleSignals({required int durationSeconds}) async {
    return const <BleSignalEntity>[
      SigBleSignal(
        deviceAddress: '01:02:03:04:05:06',
        rssi: _weakRssi,
        timestampMillis: _sampleTimestamp,
        deviceName: 'Weak Sensor',
      ),
      SigBleSignal(
        deviceAddress: 'AA:BB:CC:DD:EE:FF',
        rssi: _strongRssi,
        timestampMillis: _sampleTimestamp,
        deviceName: 'Strong Sensor',
      ),
    ];
  }
}

void main() {
  group('ScanBleSignalsUseCase', _registerUseCaseTests);
}

void _registerUseCaseTests() {
  test('fails fast when scan duration is out of bounds', _testInvalidDurationFailsFast);
  test('sorts returned BLE signals by descending RSSI strength', _testSortingByRssi);
}

void _testInvalidDurationFailsFast() {
  final useCase = ScanBleSignalsUseCase(_FakeBleSignalRepository());
  expect(
    () => useCase.execute(durationSeconds: _zeroDuration),
    throwsA(isA<BleScanException>()),
  );
}

Future<void> _testSortingByRssi() async {
  final useCase = ScanBleSignalsUseCase(_FakeBleSignalRepository());
  final results = await useCase.execute(durationSeconds: _validDuration);
  expect(results.first.rssi, _strongRssi);
  expect(results.last.rssi, _weakRssi);
}


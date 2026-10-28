import 'package:ble_scanner_app/features/ble_scanner/domain/entities/ble_signal_entity.dart';
import 'package:ble_scanner_app/features/ble_scanner/domain/repositories/ble_signal_repository.dart';
import 'package:ble_scanner_app/features/ble_scanner/domain/usecases/resolve_scanner_engine_use_case.dart';
import 'package:ble_scanner_app/features/ble_scanner/domain/usecases/scan_ble_signals_use_case.dart';
import 'package:ble_scanner_app/features/ble_scanner/presentation/controllers/ble_scan_controller.dart';
import 'package:ble_scanner_app/features/ble_scanner/presentation/controllers/ble_scan_state.dart';
import 'package:flutter_test/flutter_test.dart';

const int _controllerTestRssi = -55;
const int _controllerTestTimestamp = 1730005000;
const int _updatedDurationSeconds = 20;

class _StubBleRepository implements BleSignalRepository {
  @override
  Future<String> resolveActiveScannerEngineLabel() async => 'Android Native Stub';

  @override
  Future<List<BleSignalEntity>> scanBleSignals({required int durationSeconds}) async {
    return const <BleSignalEntity>[
      EddystoneUrlSignal(
        deviceAddress: 'AA:BB:CC:11:22:33',
        rssi: _controllerTestRssi,
        timestampMillis: _controllerTestTimestamp,
        txPower: -12,
        url: 'https://flutter.dev',
      ),
    ];
  }
}

void main() {
  group('BleScanController', _registerControllerTests);
}

void _registerControllerTests() {
  test('transitions to completed state with signals on scan', _testControllerScanSuccess);
  test('updates selected scan duration when idle', _testDurationUpdate);
}

Future<void> _testControllerScanSuccess() async {
  final controller = _createController();
  await controller.initializePlatformInfo();
  await controller.startScan();
  expect(controller.state.status, BleScanStatus.completed);
  expect(controller.state.signals.length, 1);
  expect(controller.state.activeScannerEngineLabel, 'Android Native Stub');
}

void _testDurationUpdate() {
  final controller = _createController();
  controller.selectScanDuration(_updatedDurationSeconds);
  expect(controller.state.selectedDurationSeconds, _updatedDurationSeconds);
}

BleScanController _createController() {
  final repository = _StubBleRepository();
  return BleScanController(
    scanBleSignalsUseCase: ScanBleSignalsUseCase(repository),
    resolveScannerEngineUseCase: ResolveScannerEngineUseCase(repository),
  );
}


import 'package:ble_scanner_app/features/ble_scanner/domain/entities/ble_signal_entity.dart';
import 'package:ble_scanner_app/features/ble_scanner/domain/repositories/ble_signal_repository.dart';
import 'package:ble_scanner_app/features/ble_scanner/domain/usecases/resolve_scanner_engine_use_case.dart';
import 'package:ble_scanner_app/features/ble_scanner/domain/usecases/scan_ble_signals_use_case.dart';
import 'package:ble_scanner_app/features/ble_scanner/presentation/controllers/ble_scan_controller.dart';
import 'package:ble_scanner_app/main.dart';
import 'package:flutter_test/flutter_test.dart';

const int _uiSignalRssi = -52;
const int _uiSignalTimestamp = 1730006000;

class _FakeUiRepository implements BleSignalRepository {
  @override
  Future<String> resolveActiveScannerEngineLabel() async {
    return 'iOS Native (CBCentralManager via KMP XCFramework)';
  }

  @override
  Future<List<BleSignalEntity>> scanBleSignals({required int durationSeconds}) async {
    return const <BleSignalEntity>[
      SigBleSignal(
        deviceAddress: 'F0:E1:D2:C3:B4:A5',
        rssi: _uiSignalRssi,
        timestampMillis: _uiSignalTimestamp,
        deviceName: 'Kytrox Pulse Oximeter',
        appearance: 'Pulse Oximeter: Wrist Worn',
      ),
    ];
  }
}

void main() {
  testWidgets('renders parsed BLE scan results on main page after scan', _testHomePageScan);
}

Future<void> _testHomePageScan(WidgetTester tester) async {
  final controller = _buildFakeController();
  await tester.pumpWidget(BleScannerApp(controller: controller));
  await tester.pumpAndSettle();
  expect(find.text('No BLE Packets Scanned Yet'), findsOneWidget);
  await tester.tap(find.text('Scan BLE Packets'));
  await tester.pumpAndSettle();
  expect(find.text('Kytrox Pulse Oximeter'), findsOneWidget);
  expect(find.text('F0:E1:D2:C3:B4:A5'), findsOneWidget);
  expect(find.text('Pulse Oximeter: Wrist Worn'), findsOneWidget);
}

BleScanController _buildFakeController() {
  final repository = _FakeUiRepository();
  return BleScanController(
    scanBleSignalsUseCase: ScanBleSignalsUseCase(repository),
    resolveScannerEngineUseCase: ResolveScannerEngineUseCase(repository),
  );
}


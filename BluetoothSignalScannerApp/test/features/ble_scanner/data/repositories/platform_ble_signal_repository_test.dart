import 'package:ble_scanner_app/features/ble_scanner/data/datasources/ble_platform_data_source.dart';
import 'package:ble_scanner_app/features/ble_scanner/data/dto/ble_signal_dto.dart';
import 'package:ble_scanner_app/features/ble_scanner/data/mappers/ble_signal_entity_mapper.dart';
import 'package:ble_scanner_app/features/ble_scanner/data/repositories/platform_ble_signal_repository.dart';
import 'package:ble_scanner_app/features/ble_scanner/domain/entities/ble_signal_entity.dart';
import 'package:flutter_test/flutter_test.dart';

const int _repoDurationSeconds = 12;
const int _repoRssi = -62;
const int _repoTimestamp = 1730003000;

class _FakeBlePlatformDataSource implements BlePlatformDataSource {
  @override
  Future<String> fetchPlatformScannerInfo() async => 'Fake Native Engine';

  @override
  Future<List<BleSignalDto>> invokeNativeScan({required int durationSeconds}) async {
    return const <BleSignalDto>[
      BleSignalDto(
        type: 'EDDYSTONE_UID',
        deviceAddress: 'DE:AD:BE:EF:00:01',
        rssi: _repoRssi,
        timestampMillis: _repoTimestamp,
        beaconMetrics: BeaconMetricsDto(namespaceId: '001122', instanceId: 'AABBCC'),
        sigMetadata: SigMetadataDto(),
      ),
    ];
  }
}

void main() {
  group('PlatformBleSignalRepository', _registerRepositoryTests);
}

void _registerRepositoryTests() {
  test('maps data source DTOs into domain entities', _testScanRepositoryMapping);
}

Future<void> _testScanRepositoryMapping() async {
  final repository = PlatformBleSignalRepository(
    dataSource: _FakeBlePlatformDataSource(),
    entityMapper: const BleSignalEntityMapper(),
  );
  final entities = await repository.scanBleSignals(durationSeconds: _repoDurationSeconds);
  expect(entities.single, isA<EddystoneUidSignal>());
  expect((entities.single as EddystoneUidSignal).instanceId, 'AABBCC');
}


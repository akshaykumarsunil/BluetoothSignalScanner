import '../../domain/entities/ble_signal_entity.dart';
import '../../domain/repositories/ble_signal_repository.dart';
import '../datasources/ble_platform_data_source.dart';
import '../mappers/ble_signal_entity_mapper.dart';

class PlatformBleSignalRepository implements BleSignalRepository {
  final BlePlatformDataSource _dataSource;
  final BleSignalEntityMapper _entityMapper;

  const PlatformBleSignalRepository({
    required BlePlatformDataSource dataSource,
    required BleSignalEntityMapper entityMapper,
  })  : _dataSource = dataSource,
        _entityMapper = entityMapper;

  @override
  Future<List<BleSignalEntity>> scanBleSignals({
    required int durationSeconds,
  }) async {
    final signalDtos = await _dataSource.invokeNativeScan(
      durationSeconds: durationSeconds,
    );
    return signalDtos.map(_entityMapper.mapToEntity).toList(growable: false);
  }

  @override
  Future<String> resolveActiveScannerEngineLabel() {
    return _dataSource.fetchPlatformScannerInfo();
  }
}


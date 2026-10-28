import 'package:flutter/services.dart';

import '../../features/ble_scanner/data/datasources/ble_platform_data_source.dart';
import '../../features/ble_scanner/data/datasources/method_channel_ble_data_source.dart';
import '../../features/ble_scanner/data/mappers/ble_signal_dto_parser.dart';
import '../../features/ble_scanner/data/mappers/ble_signal_entity_mapper.dart';
import '../../features/ble_scanner/data/repositories/platform_ble_signal_repository.dart';
import '../../features/ble_scanner/domain/repositories/ble_signal_repository.dart';
import '../../features/ble_scanner/domain/usecases/resolve_scanner_engine_use_case.dart';
import '../../features/ble_scanner/domain/usecases/scan_ble_signals_use_case.dart';
import '../../features/ble_scanner/presentation/controllers/ble_scan_controller.dart';
import '../constants/ble_channel_constants.dart';
import '../platform/target_platform_resolver.dart';

class AppDependencyContainer {
  const AppDependencyContainer();

  BleScanController createBleScanController() {
    final repository = _createBleSignalRepository();
    return BleScanController(
      scanBleSignalsUseCase: ScanBleSignalsUseCase(repository),
      resolveScannerEngineUseCase: ResolveScannerEngineUseCase(repository),
    );
  }

  BleSignalRepository _createBleSignalRepository() {
    return PlatformBleSignalRepository(
      dataSource: _createPlatformDataSource(),
      entityMapper: const BleSignalEntityMapper(),
    );
  }

  BlePlatformDataSource _createPlatformDataSource() {
    return const MethodChannelBleDataSource(
      methodChannel: MethodChannel(BleChannelConstants.channelName),
      platformResolver: TargetPlatformResolver(),
      dtoParser: BleSignalDtoParser(),
    );
  }
}


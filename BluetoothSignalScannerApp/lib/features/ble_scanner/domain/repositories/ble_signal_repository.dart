import '../entities/ble_signal_entity.dart';

abstract interface class BleSignalRepository {
  Future<List<BleSignalEntity>> scanBleSignals({required int durationSeconds});

  Future<String> resolveActiveScannerEngineLabel();
}


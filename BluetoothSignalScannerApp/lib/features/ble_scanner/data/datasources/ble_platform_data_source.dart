import '../dto/ble_signal_dto.dart';

abstract interface class BlePlatformDataSource {
  Future<List<BleSignalDto>> invokeNativeScan({required int durationSeconds});

  Future<String> fetchPlatformScannerInfo();
}


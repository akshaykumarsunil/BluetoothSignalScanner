import '../../../../core/constants/ble_channel_constants.dart';
import '../../../../core/errors/ble_scan_exception.dart';
import '../entities/ble_signal_entity.dart';
import '../repositories/ble_signal_repository.dart';

class ScanBleSignalsUseCase {
  final BleSignalRepository _repository;

  const ScanBleSignalsUseCase(this._repository);

  Future<List<BleSignalEntity>> execute({
    int durationSeconds = BleChannelConstants.defaultScanDurationSeconds,
  }) async {
    _validateDuration(durationSeconds);
    final signals = await _repository.scanBleSignals(
      durationSeconds: durationSeconds,
    );
    return _sortSignalsByStrength(signals);
  }

  void _validateDuration(int durationSeconds) {
    final isTooShort = durationSeconds < BleChannelConstants.minScanDurationSeconds;
    final isTooLong = durationSeconds > BleChannelConstants.maxScanDurationSeconds;
    if (isTooShort || isTooLong) {
      throw const BleScanException(
        code: BleScanException.invalidDurationCode,
        message: 'Scan duration must be between 1 and 60 seconds.',
      );
    }
  }

  List<BleSignalEntity> _sortSignalsByStrength(List<BleSignalEntity> signals) {
    final sorted = List<BleSignalEntity>.of(signals);
    sorted.sort((first, second) => second.rssi.compareTo(first.rssi));
    return sorted;
  }
}


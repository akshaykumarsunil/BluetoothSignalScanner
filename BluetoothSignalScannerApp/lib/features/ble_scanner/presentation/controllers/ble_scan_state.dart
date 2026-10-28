import '../../../../core/constants/ble_channel_constants.dart';
import '../../domain/entities/ble_signal_entity.dart';

enum BleScanStatus {
  idle,
  scanning,
  completed,
  failure,
}

class BleScanState {
  final BleScanStatus status;
  final List<BleSignalEntity> signals;
  final int selectedDurationSeconds;
  final String activeScannerEngineLabel;
  final String? errorMessage;

  const BleScanState({
    this.status = BleScanStatus.idle,
    this.signals = const <BleSignalEntity>[],
    this.selectedDurationSeconds = BleChannelConstants.defaultScanDurationSeconds,
    this.activeScannerEngineLabel = '',
    this.errorMessage,
  });

  bool get isScanning => status == BleScanStatus.scanning;

  bool get hasError => errorMessage != null && errorMessage!.isNotEmpty;

  BleScanState copyWith({
    BleScanStatus? status,
    List<BleSignalEntity>? signals,
    int? selectedDurationSeconds,
    String? activeScannerEngineLabel,
    String? errorMessage,
    bool clearError = false,
  }) {
    return BleScanState(
      status: status ?? this.status,
      signals: signals ?? this.signals,
      selectedDurationSeconds: selectedDurationSeconds ?? this.selectedDurationSeconds,
      activeScannerEngineLabel: activeScannerEngineLabel ?? this.activeScannerEngineLabel,
      errorMessage: clearError ? null : (errorMessage ?? this.errorMessage),
    );
  }
}


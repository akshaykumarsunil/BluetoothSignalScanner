import 'package:flutter/foundation.dart';

import '../../../../core/constants/ble_channel_constants.dart';
import '../../../../core/errors/ble_scan_exception.dart';
import '../../domain/usecases/resolve_scanner_engine_use_case.dart';
import '../../domain/usecases/scan_ble_signals_use_case.dart';
import 'ble_scan_state.dart';

class BleScanController extends ChangeNotifier {
  final ScanBleSignalsUseCase _scanBleSignalsUseCase;
  final ResolveScannerEngineUseCase _resolveScannerEngineUseCase;

  BleScanState _state = const BleScanState();

  BleScanController({
    required ScanBleSignalsUseCase scanBleSignalsUseCase,
    required ResolveScannerEngineUseCase resolveScannerEngineUseCase,
  })  : _scanBleSignalsUseCase = scanBleSignalsUseCase,
        _resolveScannerEngineUseCase = resolveScannerEngineUseCase;

  BleScanState get state => _state;

  Future<void> initializePlatformInfo() async {
    try {
      final label = await _resolveScannerEngineUseCase.execute();
      _updateState(_state.copyWith(activeScannerEngineLabel: label));
    } on BleScanException catch (error) {
      _updateState(
        _state.copyWith(
          activeScannerEngineLabel: BleChannelConstants.unsupportedPlatformScannerName,
          errorMessage: error.message,
        ),
      );
    }
  }

  void selectScanDuration(int durationSeconds) {
    if (_state.isScanning) return;
    if (durationSeconds < BleChannelConstants.minScanDurationSeconds) return;
    _updateState(
      _state.copyWith(
        selectedDurationSeconds: durationSeconds,
        clearError: true,
      ),
    );
  }

  Future<void> startScan() async {
    if (_state.isScanning) return;
    _updateState(_state.copyWith(status: BleScanStatus.scanning, clearError: true));
    try {
      final discoveredSignals = await _scanBleSignalsUseCase.execute(
        durationSeconds: _state.selectedDurationSeconds,
      );
      _updateState(
        _state.copyWith(status: BleScanStatus.completed, signals: discoveredSignals),
      );
    } on Object catch (error) {
      _handleScanFailure(error);
    }
  }

  void _handleScanFailure(Object error) {
    final message = error is BleScanException ? error.message : error.toString();
    _updateState(
      _state.copyWith(
        status: BleScanStatus.failure,
        errorMessage: message,
      ),
    );
  }

  void _updateState(BleScanState nextState) {
    _state = nextState;
    notifyListeners();
  }
}


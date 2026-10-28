import '../repositories/ble_signal_repository.dart';

class ResolveScannerEngineUseCase {
  final BleSignalRepository _repository;

  const ResolveScannerEngineUseCase(this._repository);

  Future<String> execute() {
    return _repository.resolveActiveScannerEngineLabel();
  }
}


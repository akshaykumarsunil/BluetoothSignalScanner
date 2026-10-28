import 'package:flutter/services.dart';

import '../../../../core/constants/ble_channel_constants.dart';
import '../../../../core/errors/ble_scan_exception.dart';
import '../../../../core/platform/target_platform_resolver.dart';
import '../dto/ble_signal_dto.dart';
import '../mappers/ble_signal_dto_parser.dart';
import 'ble_platform_data_source.dart';

class MethodChannelBleDataSource implements BlePlatformDataSource {
  final MethodChannel _methodChannel;
  final TargetPlatformResolver _platformResolver;
  final BleSignalDtoParser _dtoParser;

  const MethodChannelBleDataSource({
    required MethodChannel methodChannel,
    required TargetPlatformResolver platformResolver,
    required BleSignalDtoParser dtoParser,
  })  : _methodChannel = methodChannel,
        _platformResolver = platformResolver,
        _dtoParser = dtoParser;

  @override
  Future<List<BleSignalDto>> invokeNativeScan({
    required int durationSeconds,
  }) async {
    _platformResolver.resolveMobilePlatform();
    final rawList = await _invokeScanChannel(durationSeconds);
    if (rawList == null) return const <BleSignalDto>[];
    return rawList
        .whereType<Map<Object?, Object?>>()
        .map(_dtoParser.parseFromMap)
        .toList(growable: false);
  }

  @override
  Future<String> fetchPlatformScannerInfo() async {
    final mobilePlatform = _platformResolver.resolveMobilePlatform();
    try {
      final label = await _methodChannel.invokeMethod<String>(
        BleChannelConstants.methodGetPlatformInfo,
      );
      return label ?? mobilePlatform.defaultScannerLabel;
    } on Object {
      return mobilePlatform.defaultScannerLabel;
    }
  }

  Future<List<Object?>?> _invokeScanChannel(int durationSeconds) async {
    try {
      return await _methodChannel.invokeMethod<List<Object?>>(
        BleChannelConstants.methodScanSignals,
        <String, Object>{BleChannelConstants.argDurationSeconds: durationSeconds},
      );
    } on PlatformException catch (error) {
      throw BleScanException(
        code: error.code,
        message: error.message ?? 'Native BLE scan failed.',
      );
    }
  }
}


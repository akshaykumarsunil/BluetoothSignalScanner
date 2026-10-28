import 'package:flutter/foundation.dart';

import '../constants/ble_channel_constants.dart';
import '../errors/ble_scan_exception.dart';

enum SupportedBleMobilePlatform {
  android(BleChannelConstants.androidFallbackScannerName),
  ios(BleChannelConstants.iosFallbackScannerName);

  final String defaultScannerLabel;

  const SupportedBleMobilePlatform(this.defaultScannerLabel);
}

class TargetPlatformResolver {
  final TargetPlatform Function() _platformProvider;

  const TargetPlatformResolver({
    TargetPlatform Function()? platformProvider,
  }) : _platformProvider = platformProvider ?? _defaultPlatformProvider;

  static TargetPlatform _defaultPlatformProvider() => defaultTargetPlatform;

  SupportedBleMobilePlatform resolveMobilePlatform() {
        final currentPlatform = _platformProvider();
        if (currentPlatform == TargetPlatform.android) {
          return SupportedBleMobilePlatform.android;
        }
        if (currentPlatform == TargetPlatform.iOS) {
          return SupportedBleMobilePlatform.ios;
        }
        throw const BleScanException(
          code: BleScanException.unsupportedPlatformCode,
          message: BleChannelConstants.unsupportedPlatformScannerName,
        );
      }
}


class BleChannelConstants {
  const BleChannelConstants._();

  static const String channelName = 'com.kytrox.blescanner';
  static const String methodScanSignals = 'scanSignals';
  static const String methodGetPlatformInfo = 'getPlatformScannerInfo';
  static const String argDurationSeconds = 'durationSeconds';

  static const int defaultScanDurationSeconds = 12;
  static const int minScanDurationSeconds = 1;
  static const int maxScanDurationSeconds = 60;

  static const List<int> presetScanDurationsSeconds = <int>[5, 12, 20];

  static const String androidFallbackScannerName =
      'Android Native (BluetoothLeScanner via KMP AAR)';
  static const String iosFallbackScannerName =
      'iOS Native (CBCentralManager via KMP XCFramework)';
  static const String unsupportedPlatformScannerName =
      'Unsupported Platform (Requires Android or iOS)';
}


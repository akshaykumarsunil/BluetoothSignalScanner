import 'package:ble_scanner_app/core/errors/ble_scan_exception.dart';
import 'package:ble_scanner_app/core/platform/target_platform_resolver.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  group('TargetPlatformResolver', _registerTargetPlatformResolverTests);
}

void _registerTargetPlatformResolverTests() {
  test('resolves Android when running on Android target', _testAndroidResolution);
  test('resolves iOS when running on iOS target', _testIosResolution);
  test('throws BleScanException on unsupported desktop platform', _testUnsupportedResolution);
}

void _testAndroidResolution() {
  final resolver = TargetPlatformResolver(
    platformProvider: () => TargetPlatform.android,
  );
  expect(resolver.resolveMobilePlatform(), SupportedBleMobilePlatform.android);
}

void _testIosResolution() {
  final resolver = TargetPlatformResolver(
    platformProvider: () => TargetPlatform.iOS,
  );
  expect(resolver.resolveMobilePlatform(), SupportedBleMobilePlatform.ios);
}

void _testUnsupportedResolution() {
  final resolver = TargetPlatformResolver(
    platformProvider: () => TargetPlatform.macOS,
  );
  expect(resolver.resolveMobilePlatform, throwsA(isA<BleScanException>()));
}


class UiConstants {
  const UiConstants._();

  static const String appTitle = 'BLE Signal Scanner';
  static const String scanButtonLabel = 'Scan BLE Packets';
  static const String scanningButtonLabel = 'Scanning Nearby Packets...';
  static const String emptyStateTitle = 'No BLE Packets Scanned Yet';
  static const String emptyStateSubtitle =
      'Tap "Scan BLE Packets" to capture and decode advertisements using the native KMP scanner.';

  static const double pageHorizontalPadding = 16.0;
  static const double pageVerticalPadding = 12.0;
  static const double cardBorderRadius = 14.0;
  static const double badgeBorderRadius = 8.0;
  static const double sectionSpacing = 12.0;
  static const double compactSpacing = 6.0;
  static const double tinySpacing = 4.0;
  static const double cardPadding = 16.0;
  static const double badgeHorizontalPadding = 10.0;
  static const double badgeVerticalPadding = 4.0;
  static const double cardElevation = 1.5;
  static const double emptyIconSize = 56.0;
  static const double headerIconSize = 20.0;
  static const double progressIndicatorSize = 18.0;
  static const double progressStrokeWidth = 2.2;

  static const int excellentRssiThresholdDbm = -60;
  static const int goodRssiThresholdDbm = -75;
  static const int fairRssiThresholdDbm = -88;

  static const int primaryColorHex = 0xFF0F52BA;
  static const int surfaceTintHex = 0xFFF4F7FC;
  static const int sigBadgeColorHex = 0xFF1565C0;
  static const int eddystoneBadgeColorHex = 0xFF00796B;
  static const int altBeaconBadgeColorHex = 0xFF6A1B9A;
  static const int excellentSignalColorHex = 0xFF2E7D32;
  static const int goodSignalColorHex = 0xFFEF6C00;
  static const int weakSignalColorHex = 0xFFC62828;
}


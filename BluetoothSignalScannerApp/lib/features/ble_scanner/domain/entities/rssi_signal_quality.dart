import '../../../../core/constants/ui_constants.dart';

enum RssiSignalQuality {
  excellent('Excellent'),
  good('Good'),
  fair('Fair'),
  weak('Weak');

  final String label;

  const RssiSignalQuality(this.label);

  static RssiSignalQuality fromRssi(int rssiDbm) {
    if (rssiDbm >= UiConstants.excellentRssiThresholdDbm) {
      return RssiSignalQuality.excellent;
    }
    if (rssiDbm >= UiConstants.goodRssiThresholdDbm) {
      return RssiSignalQuality.good;
    }
    if (rssiDbm >= UiConstants.fairRssiThresholdDbm) {
      return RssiSignalQuality.fair;
    }
    return RssiSignalQuality.weak;
  }
}


import 'package:flutter/material.dart';

import '../../../../core/constants/ui_constants.dart';
import '../../domain/entities/rssi_signal_quality.dart';

class RssiIndicatorBadge extends StatelessWidget {
  final int rssiDbm;
  final RssiSignalQuality signalQuality;

  const RssiIndicatorBadge({
    super.key,
    required this.rssiDbm,
    required this.signalQuality,
  });

  @override
  Widget build(BuildContext context) {
    final color = _resolveQualityColor();
    return Container(
      padding: const EdgeInsets.symmetric(
        horizontal: UiConstants.badgeHorizontalPadding,
        vertical: UiConstants.badgeVerticalPadding,
      ),
      decoration: _buildOutlineDecoration(color),
      child: Text(
        '$rssiDbm dBm • ${signalQuality.label}',
        style: TextStyle(color: color, fontWeight: FontWeight.w700),
      ),
    );
  }

  BoxDecoration _buildOutlineDecoration(Color color) {
    return BoxDecoration(
      border: Border.all(color: color),
      borderRadius: BorderRadius.circular(UiConstants.badgeBorderRadius),
    );
  }

  Color _resolveQualityColor() {
    if (signalQuality == RssiSignalQuality.excellent) {
      return const Color(UiConstants.excellentSignalColorHex);
    }
    if (signalQuality == RssiSignalQuality.good ||
        signalQuality == RssiSignalQuality.fair) {
      return const Color(UiConstants.goodSignalColorHex);
    }
    return const Color(UiConstants.weakSignalColorHex);
  }
}


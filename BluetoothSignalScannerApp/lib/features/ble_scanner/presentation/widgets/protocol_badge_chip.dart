import 'package:flutter/material.dart';

import '../../../../core/constants/ui_constants.dart';
import '../../domain/entities/ble_protocol_type.dart';

class ProtocolBadgeChip extends StatelessWidget {
  final BleProtocolType protocolType;

  const ProtocolBadgeChip({
    super.key,
    required this.protocolType,
  });

  @override
  Widget build(BuildContext context) {
    final badgeColor = _resolveBadgeColor();
    return Container(
      padding: const EdgeInsets.symmetric(
        horizontal: UiConstants.badgeHorizontalPadding,
        vertical: UiConstants.badgeVerticalPadding,
      ),
      decoration: _buildBadgeDecoration(badgeColor),
      child: Text(
        protocolType.displayName,
        style: const TextStyle(color: Colors.white, fontWeight: FontWeight.w600),
      ),
    );
  }

  BoxDecoration _buildBadgeDecoration(Color badgeColor) {
    return BoxDecoration(
      color: badgeColor,
      borderRadius: BorderRadius.circular(UiConstants.badgeBorderRadius),
    );
  }

  Color _resolveBadgeColor() {
    if (protocolType == BleProtocolType.sig) {
      return const Color(UiConstants.sigBadgeColorHex);
    }
    if (protocolType == BleProtocolType.altBeacon) {
      return const Color(UiConstants.altBeaconBadgeColorHex);
    }
    return const Color(UiConstants.eddystoneBadgeColorHex);
  }
}


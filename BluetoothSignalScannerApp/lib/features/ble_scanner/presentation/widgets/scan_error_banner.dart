import 'package:flutter/material.dart';

import '../../../../core/constants/ui_constants.dart';

class ScanErrorBanner extends StatelessWidget {
  final String errorMessage;

  const ScanErrorBanner({
    super.key,
    required this.errorMessage,
  });

  @override
  Widget build(BuildContext context) {
    final colorScheme = Theme.of(context).colorScheme;
    return Container(
      margin: const EdgeInsets.only(bottom: UiConstants.sectionSpacing),
      padding: const EdgeInsets.all(UiConstants.cardPadding),
      decoration: _buildBannerDecoration(colorScheme),
      child: _buildBannerRow(colorScheme),
    );
  }

  BoxDecoration _buildBannerDecoration(ColorScheme colorScheme) {
    return BoxDecoration(
      color: colorScheme.errorContainer,
      borderRadius: BorderRadius.circular(UiConstants.cardBorderRadius),
    );
  }

  Widget _buildBannerRow(ColorScheme colorScheme) {
    return Row(
      children: <Widget>[
        Icon(Icons.error_outline, color: colorScheme.onErrorContainer),
        const SizedBox(width: UiConstants.compactSpacing),
        Expanded(
          child: Text(
            errorMessage,
            style: TextStyle(color: colorScheme.onErrorContainer),
          ),
        ),
      ],
    );
  }
}


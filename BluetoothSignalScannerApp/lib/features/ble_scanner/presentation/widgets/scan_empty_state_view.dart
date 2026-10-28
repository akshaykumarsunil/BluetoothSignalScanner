import 'package:flutter/material.dart';

import '../../../../core/constants/ui_constants.dart';

class ScanEmptyStateView extends StatelessWidget {
  final bool isScanning;

  const ScanEmptyStateView({
    super.key,
    required this.isScanning,
  });

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(UiConstants.cardPadding),
        child: _buildEmptyContent(context),
      ),
    );
  }

  Widget _buildEmptyContent(BuildContext context) {
    return Column(
      mainAxisSize: MainAxisSize.min,
      children: <Widget>[
        const Icon(Icons.sensors_off_outlined, size: UiConstants.emptyIconSize),
        const SizedBox(height: UiConstants.sectionSpacing),
        _buildTitleText(context),
        const SizedBox(height: UiConstants.compactSpacing),
        _buildSubtitleText(context),
      ],
    );
  }

  Widget _buildTitleText(BuildContext context) {
    final text = isScanning ? UiConstants.scanningButtonLabel : UiConstants.emptyStateTitle;
    return Text(
      text,
      style: Theme.of(context).textTheme.titleMedium,
      textAlign: TextAlign.center,
    );
  }

  Widget _buildSubtitleText(BuildContext context) {
    return Text(
      UiConstants.emptyStateSubtitle,
      style: Theme.of(context).textTheme.bodyMedium,
      textAlign: TextAlign.center,
    );
  }
}

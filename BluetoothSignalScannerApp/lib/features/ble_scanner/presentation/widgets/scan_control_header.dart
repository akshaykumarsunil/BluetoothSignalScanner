import 'package:flutter/material.dart';

import '../../../../core/constants/ble_channel_constants.dart';
import '../../../../core/constants/ui_constants.dart';

class ScanControlHeader extends StatelessWidget {
  final String activeScannerLabel;
  final int selectedDurationSeconds;
  final bool isScanning;
  final int resultCount;
  final ValueChanged<int> onDurationSelected;
  final VoidCallback onScanPressed;

  const ScanControlHeader({
    super.key,
    required this.activeScannerLabel,
    required this.selectedDurationSeconds,
    required this.isScanning,
    required this.resultCount,
    required this.onDurationSelected,
    required this.onScanPressed,
  });

  @override
  Widget build(BuildContext context) {
    return Card(
      elevation: UiConstants.cardElevation,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(UiConstants.cardBorderRadius),
      ),
      child: Padding(
        padding: const EdgeInsets.all(UiConstants.cardPadding),
        child: _buildHeaderColumns(context),
      ),
    );
  }

  Widget _buildHeaderColumns(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: <Widget>[
        _buildPlatformRow(context),
        const SizedBox(height: UiConstants.sectionSpacing),
        _buildDurationSelectorRow(context),
        const SizedBox(height: UiConstants.sectionSpacing),
        _buildScanActionButton(),
      ],
    );
  }

  Widget _buildPlatformRow(BuildContext context) {
    final labelStyle = Theme.of(context).textTheme.bodyMedium?.copyWith(
          fontWeight: FontWeight.w600,
        );
    return Row(
      children: <Widget>[
        const Icon(Icons.bluetooth_searching, size: UiConstants.headerIconSize),
        const SizedBox(width: UiConstants.compactSpacing),
        Expanded(child: Text(activeScannerLabel, style: labelStyle)),
        Text('Packets: $resultCount'),
      ],
    );
  }

  Widget _buildDurationSelectorRow(BuildContext context) {
    return Wrap(
      spacing: UiConstants.compactSpacing,
      crossAxisAlignment: WrapCrossAlignment.center,
      children: <Widget>[
        Text('Scan Window:', style: Theme.of(context).textTheme.bodySmall),
        ...BleChannelConstants.presetScanDurationsSeconds.map(_buildDurationChip),
      ],
    );
  }

  Widget _buildDurationChip(int durationSeconds) {
    final isSelected = durationSeconds == selectedDurationSeconds;
    return ChoiceChip(
      label: Text('${durationSeconds}s'),
      selected: isSelected,
      onSelected: isScanning ? null : (_) => onDurationSelected(durationSeconds),
    );
  }

  Widget _buildScanActionButton() {
    return FilledButton.icon(
      onPressed: isScanning ? null : onScanPressed,
      icon: _buildScanButtonIcon(),
      label: Text(
        isScanning ? UiConstants.scanningButtonLabel : UiConstants.scanButtonLabel,
      ),
    );
  }

  Widget _buildScanButtonIcon() {
    if (!isScanning) return const Icon(Icons.radar);
    return const SizedBox(
      width: UiConstants.progressIndicatorSize,
      height: UiConstants.progressIndicatorSize,
      child: CircularProgressIndicator(
        strokeWidth: UiConstants.progressStrokeWidth,
      ),
    );
  }
}

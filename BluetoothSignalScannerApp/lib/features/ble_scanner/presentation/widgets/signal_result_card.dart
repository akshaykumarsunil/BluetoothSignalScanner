import 'package:flutter/material.dart';

import '../../../../core/constants/ui_constants.dart';
import '../../domain/entities/ble_signal_entity.dart';
import 'protocol_badge_chip.dart';
import 'rssi_indicator_badge.dart';
import 'signal_detail_row.dart';

class SignalResultCard extends StatelessWidget {
  final BleSignalEntity signal;

  const SignalResultCard({
    super.key,
    required this.signal,
  });

  static const int decimalPlacesCount = 1;

  @override
  Widget build(BuildContext context) {
    return Card(
      elevation: UiConstants.cardElevation,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(UiConstants.cardBorderRadius),
      ),
      child: Padding(
        padding: const EdgeInsets.all(UiConstants.cardPadding),
        child: _buildCardBody(context),
      ),
    );
  }

  Widget _buildCardBody(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: <Widget>[
        _buildTopBadgeRow(),
        const SizedBox(height: UiConstants.compactSpacing),
        _buildHeadlineText(context),
        SignalDetailRow(label: 'Device Address', value: signal.deviceAddress),
        ..._buildProtocolSpecificRows(signal),
      ],
    );
  }

  Widget _buildTopBadgeRow() {
    return Wrap(
      spacing: UiConstants.compactSpacing,
      runSpacing: UiConstants.tinySpacing,
      alignment: WrapAlignment.spaceBetween,
      children: <Widget>[
        ProtocolBadgeChip(protocolType: signal.protocolType),
        RssiIndicatorBadge(
          rssiDbm: signal.rssi,
          signalQuality: signal.signalQuality,
        ),
      ],
    );
  }

  Widget _buildHeadlineText(BuildContext context) {
    return Text(
      signal.headlineTitle,
      style: Theme.of(context).textTheme.titleMedium?.copyWith(
            fontWeight: FontWeight.bold,
          ),
    );
  }

  List<Widget> _buildProtocolSpecificRows(BleSignalEntity currentSignal) {
    return switch (currentSignal) {
      EddystoneUidSignal() => _buildEddystoneUidRows(currentSignal),
      EddystoneUrlSignal() => _buildEddystoneUrlRows(currentSignal),
      EddystoneTlmSignal() => _buildEddystoneTlmRows(currentSignal),
      AltBeaconSignal() => _buildAltBeaconRows(currentSignal),
      SigBleSignal() => _buildSigRows(currentSignal),
    };
  }

  List<Widget> _buildEddystoneUidRows(EddystoneUidSignal uidSignal) {
    return <Widget>[
      SignalDetailRow(label: 'Namespace', value: uidSignal.namespaceId),
      SignalDetailRow(label: 'Instance', value: uidSignal.instanceId),
      SignalDetailRow(label: 'Calibrated TX Power', value: '${uidSignal.txPower} dBm'),
    ];
  }

  List<Widget> _buildEddystoneUrlRows(EddystoneUrlSignal urlSignal) {
    return <Widget>[
      SignalDetailRow(label: 'Broadcast URL', value: urlSignal.url),
      SignalDetailRow(label: 'Calibrated TX Power', value: '${urlSignal.txPower} dBm'),
    ];
  }

  List<Widget> _buildEddystoneTlmRows(EddystoneTlmSignal tlmSignal) {
    final tempText = tlmSignal.temperatureCelsius.toStringAsFixed(decimalPlacesCount);
    final uptimeText = tlmSignal.uptimeSeconds.toStringAsFixed(decimalPlacesCount);
    return <Widget>[
      SignalDetailRow(label: 'Battery', value: '${tlmSignal.batteryMilliVolts} mV'),
      SignalDetailRow(label: 'Temperature', value: '$tempText °C'),
      SignalDetailRow(label: 'Adv Count', value: '${tlmSignal.advertisementCount}'),
      SignalDetailRow(label: 'Uptime', value: '${uptimeText}s'),
    ];
  }

  List<Widget> _buildAltBeaconRows(AltBeaconSignal altSignal) {
    return <Widget>[
      SignalDetailRow(label: 'Beacon ID', value: altSignal.beaconId),
      SignalDetailRow(label: 'Manufacturer', value: altSignal.manufacturerName),
      SignalDetailRow(label: 'Reference RSSI (1m)', value: '${altSignal.refRssi} dBm'),
      SignalDetailRow(label: 'Reserved Byte', value: '${altSignal.mfgReserved}'),
    ];
  }

  List<Widget> _buildSigRows(SigBleSignal sigSignal) {
    final rows = <Widget>[];
    _appendOptionalRow(rows, 'Appearance', sigSignal.appearance);
    _appendOptionalRow(rows, 'TX Power Level', sigSignal.txPowerLevel);
    _appendOptionalRow(rows, 'LE Role', sigSignal.leRole);
    _appendOptionalRow(rows, 'Adv Interval', sigSignal.advertisingInterval);
    _appendSigListRows(rows, sigSignal);
    _appendSigManufacturerRow(rows, sigSignal);
    return rows;
  }

  void _appendSigListRows(List<Widget> rows, SigBleSignal sigSignal) {
    if (sigSignal.discoveryModes.isNotEmpty) {
      rows.add(SignalDetailRow(label: 'Flags', value: sigSignal.discoveryModes.join(', ')));
    }
    if (sigSignal.serviceUuids.isNotEmpty) {
      rows.add(SignalDetailRow(label: 'Service UUIDs', value: sigSignal.serviceUuids.join(', ')));
    }
    if (sigSignal.uris.isNotEmpty) {
      rows.add(SignalDetailRow(label: 'URIs', value: sigSignal.uris.join(', ')));
    }
  }

  void _appendSigManufacturerRow(List<Widget> rows, SigBleSignal sigSignal) {
    if (sigSignal.manufacturerData.isEmpty) return;
    final summary = sigSignal.manufacturerData
        .map((entry) => '${entry.formattedHeader}: ${entry.payloadHex}')
        .join(' | ');
    rows.add(SignalDetailRow(label: 'Manufacturer Data', value: summary));
  }

  void _appendOptionalRow(List<Widget> rows, String label, String? value) {
    if (value == null || value.isEmpty) return;
    rows.add(SignalDetailRow(label: label, value: value));
  }
}

import 'package:flutter/material.dart';

import '../../../../core/constants/ui_constants.dart';

class SignalDetailRow extends StatelessWidget {
  final String label;
  final String value;

  const SignalDetailRow({
    super.key,
    required this.label,
    required this.value,
  });

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(top: UiConstants.tinySpacing),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          _buildLabelText(context),
          const SizedBox(width: UiConstants.compactSpacing),
          Expanded(child: _buildValueText(context)),
        ],
      ),
    );
  }

  Widget _buildLabelText(BuildContext context) {
    return Text(
      '$label:',
      style: Theme.of(context).textTheme.bodySmall?.copyWith(
            fontWeight: FontWeight.w700,
          ),
    );
  }

  Widget _buildValueText(BuildContext context) {
    return Text(
      value,
      style: Theme.of(context).textTheme.bodySmall,
    );
  }
}


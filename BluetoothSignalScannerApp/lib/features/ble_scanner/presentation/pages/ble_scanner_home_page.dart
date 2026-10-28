import 'package:flutter/material.dart';

import '../../../../core/constants/ui_constants.dart';
import '../../domain/entities/ble_signal_entity.dart';
import '../controllers/ble_scan_controller.dart';
import '../controllers/ble_scan_state.dart';
import '../widgets/scan_control_header.dart';
import '../widgets/scan_empty_state_view.dart';
import '../widgets/scan_error_banner.dart';
import '../widgets/signal_result_card.dart';

class BleScannerHomePage extends StatefulWidget {
  final BleScanController controller;

  const BleScannerHomePage({super.key, required this.controller});

  @override
  State<BleScannerHomePage> createState() => _BleScannerHomePageState();
}

class _BleScannerHomePageState extends State<BleScannerHomePage> {
  static const EdgeInsets _pagePadding = EdgeInsets.symmetric(
    horizontal: UiConstants.pageHorizontalPadding,
    vertical: UiConstants.pageVerticalPadding,
  );

  @override
  void initState() {
    super.initState();
    widget.controller.initializePlatformInfo();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text(UiConstants.appTitle)),
      body: SafeArea(
        child: ListenableBuilder(
          listenable: widget.controller,
          builder: (context, _) => _buildPageContent(widget.controller.state),
        ),
      ),
    );
  }

  Widget _buildPageContent(BleScanState state) {
    return Padding(
      padding: _pagePadding,
      child: Column(
        children: <Widget>[
          _buildHeader(state),
          const SizedBox(height: UiConstants.sectionSpacing),
          if (state.hasError) ScanErrorBanner(errorMessage: state.errorMessage!),
          Expanded(child: _buildResultsSection(state)),
        ],
      ),
    );
  }

  Widget _buildHeader(BleScanState state) {
    return ScanControlHeader(
      activeScannerLabel: state.activeScannerEngineLabel,
      selectedDurationSeconds: state.selectedDurationSeconds,
      isScanning: state.isScanning,
      resultCount: state.signals.length,
      onDurationSelected: widget.controller.selectScanDuration,
      onScanPressed: widget.controller.startScan,
    );
  }

  Widget _buildResultsSection(BleScanState state) {
    if (state.signals.isEmpty) {
      return ScanEmptyStateView(isScanning: state.isScanning);
    }
    return _buildSignalListView(state.signals);
  }

  Widget _buildSignalListView(List<BleSignalEntity> signals) {
    return ListView.separated(
      itemCount: signals.length,
      separatorBuilder: (_, _) => const SizedBox(height: UiConstants.compactSpacing),
      itemBuilder: (_, index) => SignalResultCard(signal: signals[index]),
    );
  }
}

import 'package:flutter/material.dart';

import 'core/constants/ui_constants.dart';
import 'core/di/app_dependency_container.dart';
import 'core/theme/app_theme_factory.dart';
import 'features/ble_scanner/presentation/controllers/ble_scan_controller.dart';
import 'features/ble_scanner/presentation/pages/ble_scanner_home_page.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  const container = AppDependencyContainer();
  runApp(
    BleScannerApp(
      controller: container.createBleScanController(),
      themeFactory: const AppThemeFactory(),
    ),
  );
}

class BleScannerApp extends StatelessWidget {
  final BleScanController controller;
  final AppThemeFactory themeFactory;

  const BleScannerApp({
    super.key,
    required this.controller,
    this.themeFactory = const AppThemeFactory(),
  });

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: UiConstants.appTitle,
      debugShowCheckedModeBanner: false,
      theme: themeFactory.createLightTheme(),
      home: BleScannerHomePage(controller: controller),
    );
  }
}

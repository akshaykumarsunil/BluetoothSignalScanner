import 'package:flutter/material.dart';

import '../constants/ui_constants.dart';

class AppThemeFactory {
  const AppThemeFactory();

  ThemeData createLightTheme() {
    final colorScheme = ColorScheme.fromSeed(
      seedColor: const Color(UiConstants.primaryColorHex),
      brightness: Brightness.light,
    );
    return ThemeData(
      useMaterial3: true,
      colorScheme: colorScheme,
      scaffoldBackgroundColor: const Color(UiConstants.surfaceTintHex),
      appBarTheme: _buildAppBarTheme(colorScheme),
    );
  }

  AppBarTheme _buildAppBarTheme(ColorScheme colorScheme) {
    return AppBarTheme(
      backgroundColor: colorScheme.primary,
      foregroundColor: colorScheme.onPrimary,
      centerTitle: false,
      elevation: 0,
    );
  }
}


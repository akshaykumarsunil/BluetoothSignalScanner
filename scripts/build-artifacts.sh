#!/usr/bin/env bash
set -euo pipefail

# Determine repository root relative to script location
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"

# Auto-detect JDK 25 if JAVA_HOME is not set
if [ -z "${JAVA_HOME:-}" ]; then
  if [ -d "/Applications/Android Studio.app/Contents/jbr/Contents/Home" ]; then
    export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
  elif command -v /usr/libexec/java_home >/dev/null 2>&1 && /usr/libexec/java_home -v 25 >/dev/null 2>&1; then
    export JAVA_HOME="$(/usr/libexec/java_home -v 25)"
  fi
fi

# Auto-detect Xcode DEVELOPER_DIR if not set
if [ -z "${DEVELOPER_DIR:-}" ]; then
  if [ -d "/Applications/Xcode.app/Contents/Developer" ]; then
    export DEVELOPER_DIR="/Applications/Xcode.app/Contents/Developer"
  fi
fi

# Resolve Flutter CLI binary
if command -v flutter >/dev/null 2>&1; then
  FLUTTER_BIN="flutter"
elif [ -x "/Users/akshaykumar/Development/flutter/bin/flutter" ]; then
  FLUTTER_BIN="/Users/akshaykumar/Development/flutter/bin/flutter"
else
  FLUTTER_BIN=""
fi

echo "==> Using JAVA_HOME: ${JAVA_HOME:-system default}"
echo "==> Using DEVELOPER_DIR: ${DEVELOPER_DIR:-system default}"
echo "==> [1/2] Building KMP BleSignalScanner Android (.aar) and iOS (.xcframework)..."
cd "${ROOT_DIR}"

./gradlew \
  :library:bundleAndroidMainAar \
  :library:assembleBleSignalScannerReleaseXCFramework \
  --stacktrace

if [ -n "${FLUTTER_BIN}" ] && [ -d "${ROOT_DIR}/BluetoothSignalScannerApp" ]; then
  echo "==> [2/2] Building Flutter Android (.apk) and iOS (.app)..."
  cd "${ROOT_DIR}/BluetoothSignalScannerApp"
  "${FLUTTER_BIN}" pub get
  "${FLUTTER_BIN}" build apk --release --target-platform android-arm64 --no-tree-shake-icons
  "${FLUTTER_BIN}" build ios --release --no-codesign --no-tree-shake-icons || \
    echo "Warning: iOS .app build skipped (verify Xcode iOS platform SDK is installed)."
fi

cd "${ROOT_DIR}"
echo "==> Build complete! Generated artifacts:"
echo "  1. KMP Android AAR:       $(ls -1 library/build/outputs/aar/*.aar 2>/dev/null || echo 'Not found')"
echo "  2. KMP iOS XCFramework:   $(ls -d library/build/XCFrameworks/release/BleSignalScanner.xcframework 2>/dev/null || echo 'Not found')"
echo "  3. Flutter Android APK:   $(ls -1 BluetoothSignalScannerApp/build/app/outputs/flutter-apk/app-release.apk 2>/dev/null || echo 'Not found')"
echo "  4. Flutter iOS App:       $(ls -d BluetoothSignalScannerApp/build/ios/iphoneos/Runner.app 2>/dev/null || echo 'Not found')"

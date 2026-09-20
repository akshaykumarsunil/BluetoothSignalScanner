#!/usr/bin/env bash
set -euo pipefail

# Determine repository root relative to script location
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"

# Auto-detect JAVA_HOME if not set (supports Homebrew OpenJDK on Apple Silicon and Intel)
if [ -z "${JAVA_HOME:-}" ]; then
  if [ -d "/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home" ]; then
    export JAVA_HOME="/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home"
  elif [ -d "/usr/local/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home" ]; then
    export JAVA_HOME="/usr/local/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home"
  elif command -v /usr/libexec/java_home >/dev/null 2>&1 && /usr/libexec/java_home -v 17 >/dev/null 2>&1; then
    export JAVA_HOME="$(/usr/libexec/java_home -v 17)"
  fi
fi

# Auto-detect Xcode DEVELOPER_DIR if not set
if [ -z "${DEVELOPER_DIR:-}" ]; then
  if [ -d "/Applications/Xcode.app/Contents/Developer" ]; then
    export DEVELOPER_DIR="/Applications/Xcode.app/Contents/Developer"
  fi
fi

echo "==> Using JAVA_HOME: ${JAVA_HOME:-system default}"
echo "==> Using DEVELOPER_DIR: ${DEVELOPER_DIR:-system default}"
echo "==> Building BleSignalScanner Android and iOS artifacts..."
cd "${ROOT_DIR}"

# Build Android AAR and iOS release XCFramework in one step
./gradlew \
  :library:bundleAndroidMainAar \
  :library:assembleBleSignalScannerReleaseXCFramework \
  --stacktrace

echo "==> Build complete!"
echo "Android AAR: $(ls -1 library/build/outputs/aar/*.aar 2>/dev/null || echo 'Not found')"
echo "iOS XCFramework: $(ls -d library/build/XCFrameworks/release/BleSignalScanner.xcframework 2>/dev/null || echo 'Not found')"

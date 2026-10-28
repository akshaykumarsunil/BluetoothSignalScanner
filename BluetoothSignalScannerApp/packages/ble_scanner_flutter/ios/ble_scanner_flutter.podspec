require 'fileutils'

Pod::Spec.new do |s|
  xcframework_link = File.join(__dir__, 'BleSignalScanner.xcframework')
  xcframework_target = '../../../../library/build/XCFrameworks/release/BleSignalScanner.xcframework'
  FileUtils.ln_sf(xcframework_target, xcframework_link) unless File.symlink?(xcframework_link)

  s.name             = 'ble_scanner_flutter'
  s.version          = '0.0.1'
  s.summary          = 'Flutter iOS bridge referencing the KMP :library module.'
  s.description      = <<-DESC
Flutter iOS plugin that links directly against the sibling KMP :library module without copying XCFrameworks.
                       DESC
  s.homepage         = 'https://github.com/kytrox/BluetoothSignalScanner'
  s.license          = { :type => 'MIT' }
  s.author           = { 'Kytrox' => 'dev@kytrox.com' }
  s.source           = { :path => '.' }
  s.source_files     = 'Classes/**/*.swift'
  s.dependency 'Flutter'
  s.platform         = :ios, '16.0'
  s.vendored_frameworks = 'BleSignalScanner.xcframework'
  s.frameworks       = 'CoreBluetooth', 'Foundation'

  s.script_phase = {
    :name => 'Compile KMP BleSignalScanner Module',
    :execution_position => :before_compile,
    :script => <<-SCRIPT
      REPO_ROOT="${PODS_TARGET_SRCROOT}/../../../.."
      cd "${REPO_ROOT}"
      ./gradlew :library:assembleBleSignalScannerReleaseXCFramework
    SCRIPT
  }

  s.pod_target_xcconfig = {
    'DEFINES_MODULE' => 'YES',
    'EXCLUDED_ARCHS[sdk=iphonesimulator*]' => 'i386 x86_64'
  }
  s.swift_version = '5.0'
end

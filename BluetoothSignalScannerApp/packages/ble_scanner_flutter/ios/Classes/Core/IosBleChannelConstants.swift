import Foundation

enum IosBleChannelConstants {
    static let channelName = "com.kytrox.blescanner"
    static let methodScanSignals = "scanSignals"
    static let methodGetPlatformInfo = "getPlatformScannerInfo"
    static let argDurationSeconds = "durationSeconds"
    static let defaultScanDurationSeconds: Int32 = 12
    static let minScanDurationSeconds: Int32 = 1
    static let errorInvalidArgument = "INVALID_ARGUMENT"
    static let errorScanFailed = "SCAN_FAILED"
    static let platformScannerLabel = "iOS Native (CBCentralManager via KMP XCFramework)"
}


import Flutter
import UIKit
import BleSignalScanner

public final class BleScannerFlutterPlugin: NSObject, FlutterPlugin {

    private let bleGateway: IosPlatformBleGateway

    public init(bleGateway: IosPlatformBleGateway) {
        self.bleGateway = bleGateway
        super.init()
    }

    public static func register(with registrar: FlutterPluginRegistrar) {
        let channel = FlutterMethodChannel(
            name: IosBleChannelConstants.channelName,
            binaryMessenger: registrar.messenger()
        )
        let gateway = IosKmpBleGateway(
            scannerFactory: BleScannerFactory(),
            payloadMapper: IosBleSignalPayloadMapper()
        )
        let instance = BleScannerFlutterPlugin(bleGateway: gateway)
        registrar.addMethodCallDelegate(instance, channel: channel)
    }

    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        if call.method == IosBleChannelConstants.methodGetPlatformInfo {
            result(IosBleChannelConstants.platformScannerLabel)
            return
        }
        if call.method == IosBleChannelConstants.methodScanSignals {
            handleScanSignals(call: call, result: result)
            return
        }
        result(FlutterMethodNotImplemented)
    }

    private func handleScanSignals(call: FlutterMethodCall, result: @escaping FlutterResult) {
        let durationSeconds = extractDurationSeconds(from: call)
        guard durationSeconds >= IosBleChannelConstants.minScanDurationSeconds else {
            result(buildInvalidDurationError())
            return
        }
        executeScanTask(durationSeconds: durationSeconds, result: result)
    }

    private func executeScanTask(durationSeconds: Int32, result: @escaping FlutterResult) {
        Task {
            do {
                let payloads = try await bleGateway.scanSerializedSignals(durationSeconds: durationSeconds)
                result(payloads)
            } catch {
                result(buildScanFailedError(from: error))
            }
        }
    }

    private func buildInvalidDurationError() -> FlutterError {
        return FlutterError(
            code: IosBleChannelConstants.errorInvalidArgument,
            message: "Scan duration must be positive",
            details: nil
        )
    }

    private func buildScanFailedError(from error: Error) -> FlutterError {
        return FlutterError(
            code: IosBleChannelConstants.errorScanFailed,
            message: error.localizedDescription,
            details: nil
        )
    }

    private func extractDurationSeconds(from call: FlutterMethodCall) -> Int32 {
        let arguments = call.arguments as? [String: Any]
        if let int32Value = arguments?[IosBleChannelConstants.argDurationSeconds] as? Int32 {
            return int32Value
        }
        if let intValue = arguments?[IosBleChannelConstants.argDurationSeconds] as? Int {
            return Int32(intValue)
        }
        return IosBleChannelConstants.defaultScanDurationSeconds
    }
}

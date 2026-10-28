import Foundation
import BleSignalScanner

public final class IosKmpBleGateway: IosPlatformBleGateway {

    private let scannerFactory: BleScannerFactory
    private let payloadMapper: IosBleSignalPayloadMapper

    public init(
        scannerFactory: BleScannerFactory,
        payloadMapper: IosBleSignalPayloadMapper
    ) {
        self.scannerFactory = scannerFactory
        self.payloadMapper = payloadMapper
    }

    public func scanSerializedSignals(durationSeconds: Int32) async throws -> [[String: Any]] {
        let scanner = scannerFactory.create()
        let signals = try await scanner.scan(durationSeconds: durationSeconds)
        return signals.map { payloadMapper.mapToPayload(signal: $0) }
    }
}


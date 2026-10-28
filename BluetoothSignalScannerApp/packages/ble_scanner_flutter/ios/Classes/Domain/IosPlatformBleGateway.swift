import Foundation

public protocol IosPlatformBleGateway {
    func scanSerializedSignals(durationSeconds: Int32) async throws -> [[String: Any]]
}


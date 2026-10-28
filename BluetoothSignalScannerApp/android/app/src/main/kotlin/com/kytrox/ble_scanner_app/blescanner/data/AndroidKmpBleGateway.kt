package com.kytrox.ble_scanner_app.blescanner.data

import com.kytrox.ble_scanner_app.blescanner.domain.PlatformBleGateway
import com.kytrox.ble_scanner_app.core.BleChannelConstants
import com.kytrox.blescanner.scanner.BleScannerFactory

class AndroidKmpBleGateway(
    private val scannerFactory: BleScannerFactory,
    private val payloadMapper: BleSignalPayloadMapper,
) : PlatformBleGateway {

    override suspend fun scanSerializedSignals(durationSeconds: Int): List<Map<String, Any?>> {
        require(durationSeconds >= BleChannelConstants.MIN_SCAN_DURATION_SECONDS) {
            "Scan duration must be at least ${BleChannelConstants.MIN_SCAN_DURATION_SECONDS} second"
        }
        val scanner = scannerFactory.create()
        val signals = scanner.scan(durationSeconds)
        return signals.map(payloadMapper::mapToPayload)
    }
}


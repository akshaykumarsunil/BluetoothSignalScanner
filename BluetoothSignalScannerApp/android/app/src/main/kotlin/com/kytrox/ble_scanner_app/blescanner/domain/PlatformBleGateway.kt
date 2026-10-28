package com.kytrox.ble_scanner_app.blescanner.domain

interface PlatformBleGateway {
    suspend fun scanSerializedSignals(durationSeconds: Int): List<Map<String, Any?>>
}


package com.kytrox.ble_scanner_app.blescanner.domain

interface BlePermissionChecker {
    suspend fun ensurePermissionsGranted(): Boolean
}


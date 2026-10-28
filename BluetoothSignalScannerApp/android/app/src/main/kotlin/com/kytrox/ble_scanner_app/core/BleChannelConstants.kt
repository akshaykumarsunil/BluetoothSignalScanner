package com.kytrox.ble_scanner_app.core

object BleChannelConstants {
    const val CHANNEL_NAME = "com.kytrox.blescanner"
    const val METHOD_SCAN_SIGNALS = "scanSignals"
    const val METHOD_GET_PLATFORM_INFO = "getPlatformScannerInfo"
    const val ARG_DURATION_SECONDS = "durationSeconds"
    const val DEFAULT_SCAN_DURATION_SECONDS = 12
    const val MIN_SCAN_DURATION_SECONDS = 1
    const val PERMISSION_REQUEST_CODE = 4201
    const val ERROR_INVALID_ARGUMENT = "INVALID_ARGUMENT"
    const val ERROR_PERMISSION_DENIED = "PERMISSION_DENIED"
    const val ERROR_SCAN_FAILED = "SCAN_FAILED"
    const val PLATFORM_SCANNER_LABEL = "Android Native (BluetoothLeScanner via KMP AAR)"
}


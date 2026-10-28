package com.kytrox.ble_scanner_app.blescanner.presentation

import com.kytrox.ble_scanner_app.blescanner.domain.BlePermissionChecker
import com.kytrox.ble_scanner_app.blescanner.domain.PlatformBleGateway
import com.kytrox.ble_scanner_app.core.BleChannelConstants
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class BleScanMethodCallHandler(
    private val bleGateway: PlatformBleGateway,
    private val permissionChecker: BlePermissionChecker,
    private val coroutineScope: CoroutineScope,
) : MethodChannel.MethodCallHandler {

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            BleChannelConstants.METHOD_SCAN_SIGNALS -> handleScanSignals(call, result)
            BleChannelConstants.METHOD_GET_PLATFORM_INFO -> result.success(BleChannelConstants.PLATFORM_SCANNER_LABEL)
            else -> result.notImplemented()
        }
    }

    private fun handleScanSignals(call: MethodCall, result: MethodChannel.Result) {
        val duration = extractDurationSeconds(call)
        if (duration < BleChannelConstants.MIN_SCAN_DURATION_SECONDS) {
            result.error(BleChannelConstants.ERROR_INVALID_ARGUMENT, "Duration must be positive", null)
            return
        }
        coroutineScope.launch {
            executeAuthorizedScan(duration, result)
        }
    }

    private suspend fun executeAuthorizedScan(duration: Int, result: MethodChannel.Result) {
        if (!permissionChecker.ensurePermissionsGranted()) {
            result.error(BleChannelConstants.ERROR_PERMISSION_DENIED, "Bluetooth permissions denied", null)
            return
        }
        runCatching { bleGateway.scanSerializedSignals(duration) }
            .onSuccess { signals -> result.success(signals) }
            .onFailure { error -> result.error(BleChannelConstants.ERROR_SCAN_FAILED, error.message, null) }
    }

    private fun extractDurationSeconds(call: MethodCall): Int {
        return call.argument<Int>(BleChannelConstants.ARG_DURATION_SECONDS)
            ?: BleChannelConstants.DEFAULT_SCAN_DURATION_SECONDS
    }
}


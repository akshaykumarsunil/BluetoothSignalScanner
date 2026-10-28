package com.kytrox.ble_scanner_app

import com.kytrox.ble_scanner_app.blescanner.data.AndroidKmpBleGateway
import com.kytrox.ble_scanner_app.blescanner.data.BleSignalPayloadMapper
import com.kytrox.ble_scanner_app.blescanner.presentation.ActivityPermissionCoordinator
import com.kytrox.ble_scanner_app.blescanner.presentation.BleScanMethodCallHandler
import com.kytrox.ble_scanner_app.core.BleChannelConstants
import com.kytrox.blescanner.scanner.BleScannerFactory
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

class MainActivity : FlutterActivity() {

    private val mainScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var permissionCoordinator: ActivityPermissionCoordinator? = null
    private var methodChannel: MethodChannel? = null

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        val coordinator = ActivityPermissionCoordinator(this)
        permissionCoordinator = coordinator
        val handler = createMethodCallHandler(coordinator)
        val channel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, BleChannelConstants.CHANNEL_NAME)
        channel.setMethodCallHandler(handler)
        methodChannel = channel
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        val handled = permissionCoordinator?.onRequestPermissionsResult(requestCode, grantResults) ?: false
        if (!handled) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onDestroy() {
        methodChannel?.setMethodCallHandler(null)
        mainScope.cancel()
        super.onDestroy()
    }

    private fun createMethodCallHandler(
        coordinator: ActivityPermissionCoordinator,
    ): BleScanMethodCallHandler {
        val gateway = AndroidKmpBleGateway(
            scannerFactory = BleScannerFactory(applicationContext),
            payloadMapper = BleSignalPayloadMapper(),
        )
        return BleScanMethodCallHandler(gateway, coordinator, mainScope)
    }
}

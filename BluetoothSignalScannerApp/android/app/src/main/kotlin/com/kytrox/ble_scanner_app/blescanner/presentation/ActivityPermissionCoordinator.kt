package com.kytrox.ble_scanner_app.blescanner.presentation

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.kytrox.ble_scanner_app.blescanner.domain.BlePermissionChecker
import com.kytrox.ble_scanner_app.core.BleChannelConstants
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class ActivityPermissionCoordinator(
    private val activity: Activity,
) : BlePermissionChecker {

    private var pendingContinuation: CancellableContinuation<Boolean>? = null

    override suspend fun ensurePermissionsGranted(): Boolean {
        val missingPermissions = findMissingPermissions()
        if (missingPermissions.isEmpty()) return true
        return requestMissingPermissions(missingPermissions)
    }

    fun onRequestPermissionsResult(requestCode: Int, grantResults: IntArray): Boolean {
        if (requestCode != BleChannelConstants.PERMISSION_REQUEST_CODE) return false
        val allGranted = grantResults.isNotEmpty() &&
            grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        pendingContinuation?.resume(allGranted)
        pendingContinuation = null
        return true
    }

    private suspend fun requestMissingPermissions(permissions: Array<String>): Boolean {
        return suspendCancellableCoroutine { continuation ->
            pendingContinuation = continuation
            ActivityCompat.requestPermissions(
                activity,
                permissions,
                BleChannelConstants.PERMISSION_REQUEST_CODE,
            )
        }
    }

    private fun findMissingPermissions(): Array<String> {
        return requiredPermissions().filterNot(::isPermissionGranted).toTypedArray()
    }

    private fun isPermissionGranted(permission: String): Boolean {
        val status = ContextCompat.checkSelfPermission(activity, permission)
        return status == PackageManager.PERMISSION_GRANTED
    }

    private fun requiredPermissions(): List<String> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return listOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION,
            )
        }
        return listOf(Manifest.permission.ACCESS_FINE_LOCATION)
    }
}


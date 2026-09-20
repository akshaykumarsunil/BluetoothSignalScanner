package com.kytrox.blescanner.domain

/**
 * Manufacturer-specific data extracted from a BLE advertisement.
 * Company name is resolved from the Bluetooth SIG assigned numbers.
 */
public data class ManufacturerData(
    val companyId: Int,
    val companyName: String,
    val payload: String,
)

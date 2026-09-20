package com.kytrox.blescanner.parser

/**
 * Platform-agnostic representation of a raw BLE advertisement.
 * Platform scanners populate this DTO; parsers consume it.
 */
public data class RawAdvertisement(
    val deviceAddress: String,
    val rssi: Int,
    val timestampMillis: Long,
    val deviceName: String?,
    val txPowerLevel: Int?,
    val serviceUuids: List<String>,
    val serviceData: Map<String, ByteArray>,
    val manufacturerData: Map<Int, ByteArray>,
    val flags: Int?,
    val appearance: Int?,
)

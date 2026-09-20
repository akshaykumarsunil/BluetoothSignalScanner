package com.kytrox.blescanner.domain

/**
 * BLE advertisement flags decoded from the GAP Flags AD type (0x01).
 * Each value represents a single bit in the flags bitmask.
 */
public enum class DiscoveryMode(public val displayName: String) {
    LIMITED_DISCOVERABLE("LE Limited Discoverable Mode"),
    GENERAL_DISCOVERABLE("LE General Discoverable Mode"),
    BR_EDR_NOT_SUPPORTED("BR/EDR Not Supported"),
    LE_AND_BR_EDR_CONTROLLER("Simultaneous LE and BR/EDR (Controller)"),
    LE_AND_BR_EDR_HOST("Simultaneous LE and BR/EDR (Host)"),
}

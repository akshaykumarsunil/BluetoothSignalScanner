package com.kytrox.ble_scanner_app.blescanner.data.dto

enum class SignalProtocolType(val wireValue: String) {
    EDDYSTONE_UID("EDDYSTONE_UID"),
    EDDYSTONE_URL("EDDYSTONE_URL"),
    EDDYSTONE_TLM("EDDYSTONE_TLM"),
    ALT_BEACON("ALT_BEACON"),
    SIG("SIG"),
}


package com.kytrox.blescanner.domain

/**
 * Resolves Bluetooth SIG Appearance values (16-bit) to
 * human-readable category descriptions.
 *
 * Reference: Bluetooth SIG Assigned Numbers — Appearance Values.
 */
internal object AppearanceLookup {

    private val CATEGORY_MAP: Map<Int, String> = buildCategoryMap()

    internal fun resolve(code: Int): String {
        return CATEGORY_MAP[code]
            ?: resolveByCategory(code)
    }

    private fun resolveByCategory(code: Int): String {
        val category = code and 0xFFC0
        val subcategory = code and 0x003F
        val categoryName = CATEGORY_MAP[category] ?: return "Unknown (0x${code.toString(16).padStart(4, '0')})"
        return "$categoryName (sub: $subcategory)"
    }

    @Suppress("MagicNumber")
    private fun buildCategoryMap(): Map<Int, String> {
        return mapOf(
            0x0000 to "Unknown",
            0x0040 to "Phone",
            0x0080 to "Computer",
            0x00C0 to "Watch",
            0x00C1 to "Watch: Sports Watch",
            0x0100 to "Clock",
            0x0140 to "Display",
            0x0180 to "Remote Control",
            0x01C0 to "Eye-glasses",
            0x0200 to "Tag",
            0x0240 to "Keyring",
            0x0280 to "Media Player",
            0x02C0 to "Barcode Scanner",
            0x0300 to "Thermometer",
            0x0301 to "Thermometer: Ear",
            0x0340 to "Heart Rate Sensor",
            0x0341 to "Heart Rate Sensor: Heart Rate Belt",
            0x0380 to "Blood Pressure",
            0x0381 to "Blood Pressure: Arm",
            0x0382 to "Blood Pressure: Wrist",
            0x03C0 to "Human Interface Device",
            0x03C1 to "Human Interface Device: Keyboard",
            0x03C2 to "Human Interface Device: Mouse",
            0x03C3 to "Human Interface Device: Joystick",
            0x03C4 to "Human Interface Device: Gamepad",
            0x0400 to "Glucose Meter",
            0x0440 to "Running Walking Sensor",
            0x0441 to "Running Walking Sensor: In-Shoe",
            0x0442 to "Running Walking Sensor: On-Shoe",
            0x0443 to "Running Walking Sensor: On-Hip",
            0x0480 to "Cycling",
            0x0481 to "Cycling: Computer",
            0x0482 to "Cycling: Speed Sensor",
            0x0483 to "Cycling: Cadence Sensor",
            0x0484 to "Cycling: Power Sensor",
            0x0485 to "Cycling: Speed and Cadence Sensor",
            0x04C0 to "Pulse Oximeter",
            0x04C1 to "Pulse Oximeter: Fingertip",
            0x04C2 to "Pulse Oximeter: Wrist Worn",
            0x0500 to "Weight Scale",
            0x0540 to "Personal Mobility Device",
            0x0541 to "Personal Mobility Device: Powered Wheelchair",
            0x0542 to "Personal Mobility Device: Mobility Scooter",
            0x0580 to "Continuous Glucose Monitor",
            0x05C0 to "Insulin Pump",
            0x0600 to "Medication Delivery",
            0x0640 to "Spirometer",
            0x0680 to "Indoor Positioning",
            0x06C0 to "Outdoor Positioning",
            0x0700 to "Environmental Sensor",
            0x0740 to "Light Fixture",
            0x0780 to "Fan",
            0x07C0 to "HVAC",
            0x0800 to "Heating",
            0x0840 to "Access Control",
            0x0880 to "Motorized Device",
            0x08C0 to "Power Device",
            0x0900 to "Light Source",
            0x0940 to "Window Covering",
            0x0980 to "Audio Sink",
            0x09C0 to "Audio Source",
            0x0A00 to "Motorized Vehicle",
            0x0A40 to "Domestic Appliance",
            0x0A80 to "Wearable Audio Device",
            0x0A81 to "Wearable Audio Device: Earbud",
            0x0A82 to "Wearable Audio Device: Headset",
            0x0A83 to "Wearable Audio Device: Headphones",
            0x0AC0 to "Aircraft",
            0x0B00 to "AV Equipment",
            0x0B40 to "Display Equipment",
            0x0B80 to "Hearing Aid",
            0x0B81 to "Hearing Aid: In-Ear",
            0x0B82 to "Hearing Aid: Behind-Ear",
            0x0BC0 to "Gaming",
            0x0C00 to "Signage",
        )
    }
}

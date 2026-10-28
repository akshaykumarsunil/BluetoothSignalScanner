package com.kytrox.blescanner.domain

/**
 * Official Bluetooth SIG Common Data Types (Advertising Data / AD Types)
 * defined in Assigned Numbers (Core Specification Supplement).
 */
public enum class AdType(
    public val code: Int,
    public val typeName: String,
) {
    FLAGS(0x01, "Flags"),
    INCOMPLETE_SERVICE_UUIDS_16_BIT(0x02, "Incomplete List of 16-bit Service or Service Class UUIDs"),
    COMPLETE_SERVICE_UUIDS_16_BIT(0x03, "Complete List of 16-bit Service or Service Class UUIDs"),
    INCOMPLETE_SERVICE_UUIDS_32_BIT(0x04, "Incomplete List of 32-bit Service or Service Class UUIDs"),
    COMPLETE_SERVICE_UUIDS_32_BIT(0x05, "Complete List of 32-bit Service or Service Class UUIDs"),
    INCOMPLETE_SERVICE_UUIDS_128_BIT(0x06, "Incomplete List of 128-bit Service or Service Class UUIDs"),
    COMPLETE_SERVICE_UUIDS_128_BIT(0x07, "Complete List of 128-bit Service or Service Class UUIDs"),
    SHORTENED_LOCAL_NAME(0x08, "Shortened Local Name"),
    COMPLETE_LOCAL_NAME(0x09, "Complete Local Name"),
    TX_POWER_LEVEL(0x0A, "Tx Power Level"),
    CLASS_OF_DEVICE(0x0D, "Class of Device"),
    SIMPLE_PAIRING_HASH_C192(0x0E, "Simple Pairing Hash C-192"),
    SIMPLE_PAIRING_RANDOMIZER_R192(0x0F, "Simple Pairing Randomizer R-192"),
    DEVICE_ID_OR_TK_VALUE(0x10, "Device ID / Security Manager TK Value"),
    SECURITY_MANAGER_OOB_FLAGS(0x11, "Security Manager Out of Band Flags"),
    PERIPHERAL_CONNECTION_INTERVAL_RANGE(0x12, "Peripheral Connection Interval Range"),
    SERVICE_SOLICITATION_UUIDS_16_BIT(0x14, "List of 16-bit Service Solicitation UUIDs"),
    SERVICE_SOLICITATION_UUIDS_128_BIT(0x15, "List of 128-bit Service Solicitation UUIDs"),
    SERVICE_DATA_16_BIT(0x16, "Service Data - 16-bit UUID"),
    PUBLIC_TARGET_ADDRESS(0x17, "Public Target Address"),
    RANDOM_TARGET_ADDRESS(0x18, "Random Target Address"),
    APPEARANCE(0x19, "Appearance"),
    ADVERTISING_INTERVAL(0x1A, "Advertising Interval"),
    LE_BLUETOOTH_DEVICE_ADDRESS(0x1B, "LE Bluetooth Device Address"),
    LE_ROLE(0x1C, "LE Role"),
    SIMPLE_PAIRING_HASH_C256(0x1D, "Simple Pairing Hash C-256"),
    SIMPLE_PAIRING_RANDOMIZER_R256(0x1E, "Simple Pairing Randomizer R-256"),
    SERVICE_SOLICITATION_UUIDS_32_BIT(0x1F, "List of 32-bit Service Solicitation UUIDs"),
    SERVICE_DATA_32_BIT(0x20, "Service Data - 32-bit UUID"),
    SERVICE_DATA_128_BIT(0x21, "Service Data - 128-bit UUID"),
    LE_SECURE_CONNECTIONS_CONFIRMATION(0x22, "LE Secure Connections Confirmation Value"),
    LE_SECURE_CONNECTIONS_RANDOM(0x23, "LE Secure Connections Random Value"),
    URI(0x24, "URI"),
    INDOOR_POSITIONING(0x25, "Indoor Positioning"),
    TRANSPORT_DISCOVERY_DATA(0x26, "Transport Discovery Data"),
    LE_SUPPORTED_FEATURES(0x27, "LE Supported Features"),
    CHANNEL_MAP_UPDATE_INDICATION(0x28, "Channel Map Update Indication"),
    PB_ADV(0x29, "PB-ADV"),
    MESH_MESSAGE(0x2A, "Mesh Message"),
    MESH_BEACON(0x2B, "Mesh Beacon"),
    BIG_INFO(0x2C, "BIGInfo"),
    BROADCAST_CODE(0x2D, "Broadcast_Code"),
    RESOLVABLE_SET_IDENTIFIER(0x2E, "Resolvable Set Identifier"),
    ADVERTISING_INTERVAL_LONG(0x2F, "Advertising Interval - long"),
    BROADCAST_NAME(0x30, "Broadcast_Name"),
    ENCRYPTED_ADVERTISING_DATA(0x31, "Encrypted Advertising Data"),
    PERIODIC_ADVERTISING_RESPONSE_TIMING(0x32, "Periodic Advertising Response Timing Information"),
    ELECTRONIC_SHELF_LABEL(0x34, "Electronic Shelf Label"),
    THREE_D_INFORMATION_DATA(0x3D, "3D Information Data"),
    MANUFACTURER_SPECIFIC_DATA(0xFF, "Manufacturer Specific Data");

    public companion object {
        private val BY_CODE: Map<Int, AdType> = entries.associateBy { it.code }

        public fun fromCode(code: Int): AdType? {
            return BY_CODE[code]
        }
    }
}


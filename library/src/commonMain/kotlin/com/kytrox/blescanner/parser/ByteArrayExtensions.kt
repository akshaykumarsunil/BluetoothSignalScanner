package com.kytrox.blescanner.parser

private const val HEX_RADIX = 16
private const val BYTE_MASK = 0xFF
private const val SIGNED_BYTE_THRESHOLD = 128
private const val SIGNED_BYTE_OFFSET = 256
private const val UUID_BYTE_LENGTH = 16
private const val SHORT_UUID_BYTE_LENGTH = 2
private const val UUID_32_BYTE_LENGTH = 4
private const val MAC_ADDRESS_BYTE_LENGTH = 6
private const val BITS_PER_BYTE = 8
private const val HEX_PAD_WIDTH = 2
private const val HEX_CHARS_PER_BYTE = 2
private const val UUID_SEGMENT_1_END = 8
private const val UUID_SEGMENT_2_END = 12
private const val UUID_SEGMENT_3_END = 16
private const val UUID_SEGMENT_4_END = 20
private const val BLUETOOTH_BASE_UUID_SUFFIX = "-0000-1000-8000-00805f9b34fb"

internal fun ByteArray.toHexString(): String {
    return joinToString("") { byte ->
        (byte.toInt() and BYTE_MASK)
            .toString(HEX_RADIX)
            .padStart(HEX_PAD_WIDTH, '0')
    }
}

internal fun String.hexToByteArray(): ByteArray {
    val sanitized = sanitizeHexInput(this)
    if (sanitized.isEmpty() || sanitized.length % HEX_CHARS_PER_BYTE != 0) {
        return ByteArray(0)
    }
    return decodeHexPairs(sanitized)
}

private fun sanitizeHexInput(input: String): String {
    return input
        .replace("0x", "", ignoreCase = true)
        .filter { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' }
}

private fun decodeHexPairs(sanitized: String): ByteArray {
    val byteCount = sanitized.length / HEX_CHARS_PER_BYTE
    return ByteArray(byteCount) { index ->
        val start = index * HEX_CHARS_PER_BYTE
        sanitized.substring(start, start + HEX_CHARS_PER_BYTE).toInt(HEX_RADIX).toByte()
    }
}

internal fun ByteArray.readUInt16BE(offset: Int): Int {
    return ((this[offset].toInt() and BYTE_MASK) shl BITS_PER_BYTE) or
        (this[offset + 1].toInt() and BYTE_MASK)
}

internal fun ByteArray.readUInt16LE(offset: Int): Int {
    return (this[offset].toInt() and BYTE_MASK) or
        ((this[offset + 1].toInt() and BYTE_MASK) shl BITS_PER_BYTE)
}

internal fun ByteArray.readInt8(offset: Int): Int {
    val unsigned = this[offset].toInt() and BYTE_MASK
    if (unsigned >= SIGNED_BYTE_THRESHOLD) {
        return unsigned - SIGNED_BYTE_OFFSET
    }
    return unsigned
}

internal fun ByteArray.toUuid(): String {
    if (size == SHORT_UUID_BYTE_LENGTH) {
        return "0000${toHexString()}$BLUETOOTH_BASE_UUID_SUFFIX"
    }
    if (size != UUID_BYTE_LENGTH) {
        return toHexString()
    }
    return buildUuidString(toHexString())
}

internal fun ByteArray.le16BitUuidAt(offset: Int): String {
    val reversed = byteArrayOf(this[offset + 1], this[offset])
    return "0000${reversed.toHexString()}$BLUETOOTH_BASE_UUID_SUFFIX"
}

internal fun ByteArray.le32BitUuidAt(offset: Int): String {
    val reversed = byteArrayOf(
        this[offset + 3],
        this[offset + 2],
        this[offset + 1],
        this[offset],
    )
    return "${reversed.toHexString()}$BLUETOOTH_BASE_UUID_SUFFIX"
}

internal fun ByteArray.formatMacAddressAt(offset: Int): String {
    if (offset + MAC_ADDRESS_BYTE_LENGTH > size) return toHexString()
    return (0 until MAC_ADDRESS_BYTE_LENGTH).joinToString(":") { index ->
        (this[offset + index].toInt() and BYTE_MASK)
            .toString(HEX_RADIX)
            .padStart(HEX_PAD_WIDTH, '0')
            .uppercase()
    }
}

private fun buildUuidString(hex: String): String {
    return buildString {
        append(hex.substring(0, UUID_SEGMENT_1_END))
        append('-')
        append(hex.substring(UUID_SEGMENT_1_END, UUID_SEGMENT_2_END))
        append('-')
        append(hex.substring(UUID_SEGMENT_2_END, UUID_SEGMENT_3_END))
        append('-')
        append(hex.substring(UUID_SEGMENT_3_END, UUID_SEGMENT_4_END))
        append('-')
        append(hex.substring(UUID_SEGMENT_4_END))
    }
}

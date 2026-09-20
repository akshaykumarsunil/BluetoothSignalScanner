package com.kytrox.blescanner.parser

private const val HEX_RADIX = 16
private const val BYTE_MASK = 0xFF
private const val SIGNED_BYTE_THRESHOLD = 128
private const val SIGNED_BYTE_OFFSET = 256
private const val UUID_BYTE_LENGTH = 16
private const val SHORT_UUID_BYTE_LENGTH = 2
private const val BITS_PER_BYTE = 8
private const val HEX_PAD_WIDTH = 2
private const val UUID_SEGMENT_1_END = 8
private const val UUID_SEGMENT_2_END = 12
private const val UUID_SEGMENT_3_END = 16
private const val UUID_SEGMENT_4_END = 20

internal fun ByteArray.toHexString(): String {
    return joinToString("") { byte ->
        (byte.toInt() and BYTE_MASK)
            .toString(HEX_RADIX)
            .padStart(HEX_PAD_WIDTH, '0')
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
        return "0000${toHexString()}-0000-1000-8000-00805f9b34fb"
    }
    if (size != UUID_BYTE_LENGTH) {
        return toHexString()
    }
    val hex = toHexString()
    return buildUuidString(hex)
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

package com.kytrox.blescanner.parser

import com.kytrox.blescanner.domain.UriSchemeLookup

private const val BYTE_MASK = 0xFF
private const val UTF8_SINGLE_BYTE_MAX = 0x7F
private const val UTF8_TWO_BYTE_PREFIX_MASK = 0xE0
private const val UTF8_TWO_BYTE_PREFIX = 0xC0
private const val UTF8_CONTINUATION_MASK = 0xC0
private const val UTF8_CONTINUATION_PREFIX = 0x80
private const val UTF8_LEAD_BITS_MASK = 0x1F
private const val UTF8_TRAIL_BITS_MASK = 0x3F
private const val UTF8_CONTINUATION_SHIFT = 6
private const val SINGLE_BYTE_SCHEME_OFFSET = 1
private const val TWO_BYTE_SCHEME_OFFSET = 2

internal object UriDecoder {

    internal fun decode(bytes: ByteArray): String {
        if (bytes.isEmpty()) return ""
        if (isTwoByteUtf8Scheme(bytes)) {
            return decodeWithTwoByteScheme(bytes)
        }
        return decodeWithSingleByteScheme(bytes)
    }

    private fun isTwoByteUtf8Scheme(bytes: ByteArray): Boolean {
        if (bytes.size < TWO_BYTE_SCHEME_OFFSET) return false
        val first = bytes[0].toInt() and BYTE_MASK
        val second = bytes[1].toInt() and BYTE_MASK
        val hasLead = (first and UTF8_TWO_BYTE_PREFIX_MASK) == UTF8_TWO_BYTE_PREFIX
        val hasTrail = (second and UTF8_CONTINUATION_MASK) == UTF8_CONTINUATION_PREFIX
        return hasLead && hasTrail
    }

    private fun decodeWithTwoByteScheme(bytes: ByteArray): String {
        val first = bytes[0].toInt() and UTF8_LEAD_BITS_MASK
        val second = bytes[1].toInt() and UTF8_TRAIL_BITS_MASK
        val codePoint = (first shl UTF8_CONTINUATION_SHIFT) or second
        val scheme = UriSchemeLookup.resolve(codePoint) ?: ""
        val body = bytes.copyOfRange(TWO_BYTE_SCHEME_OFFSET, bytes.size).decodeToString()
        return "$scheme$body"
    }

    private fun decodeWithSingleByteScheme(bytes: ByteArray): String {
        val codePoint = bytes[0].toInt() and BYTE_MASK
        val scheme = UriSchemeLookup.resolve(codePoint)
        if (scheme == null && codePoint <= UTF8_SINGLE_BYTE_MAX) {
            return bytes.decodeToString()
        }
        val resolvedScheme = scheme ?: ""
        val body = bytes.copyOfRange(SINGLE_BYTE_SCHEME_OFFSET, bytes.size).decodeToString()
        return "$resolvedScheme$body"
    }
}


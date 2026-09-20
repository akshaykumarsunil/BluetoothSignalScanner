package com.kytrox.blescanner.parser

/**
 * Decodes Eddystone-URL scheme bytes and HTTP expansion codes.
 *
 * Reference: github.com/google/eddystone/tree/master/eddystone-url
 */
internal object EddystoneUrlDecoder {

    private val URL_SCHEMES: Map<Int, String> = mapOf(
        0x00 to "http://www.",
        0x01 to "https://www.",
        0x02 to "http://",
        0x03 to "https://",
    )

    private val URL_SUFFIXES: Map<Int, String> = mapOf(
        0x00 to ".com/",
        0x01 to ".org/",
        0x02 to ".edu/",
        0x03 to ".net/",
        0x04 to ".info/",
        0x05 to ".biz/",
        0x06 to ".gov/",
        0x07 to ".com",
        0x08 to ".org",
        0x09 to ".edu",
        0x0A to ".net",
        0x0B to ".info",
        0x0C to ".biz",
        0x0D to ".gov",
    )

    internal fun decodeScheme(schemeByte: Int): String {
        return URL_SCHEMES[schemeByte] ?: ""
    }

    internal fun decodeUrl(schemeAndEncodedBytes: ByteArray): String {
        if (schemeAndEncodedBytes.isEmpty()) return ""

        val schemeByte = schemeAndEncodedBytes[0].toInt() and 0xFF
        val scheme = decodeScheme(schemeByte)

        return buildString {
            append(scheme)
            appendEncodedCharacters(schemeAndEncodedBytes)
        }
    }

    private fun StringBuilder.appendEncodedCharacters(
        bytes: ByteArray,
    ) {
        for (i in 1 until bytes.size) {
            val code = bytes[i].toInt() and 0xFF
            val suffix = URL_SUFFIXES[code]
            if (suffix != null) {
                append(suffix)
            } else {
                append(code.toChar())
            }
        }
    }
}

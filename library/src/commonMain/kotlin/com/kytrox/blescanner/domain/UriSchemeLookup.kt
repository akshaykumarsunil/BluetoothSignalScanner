package com.kytrox.blescanner.domain

/**
 * Resolves Bluetooth SIG URI scheme code points to scheme prefixes
 * for decoding AD type 0x24 (URI).
 */
internal object UriSchemeLookup {

    private val SCHEME_MAP: Map<Int, String> = CatalogParser.parseSingle(
        UriSchemeCatalog.RAW_ENTRIES,
    )

    internal fun resolve(schemeCodePoint: Int): String? {
        return SCHEME_MAP[schemeCodePoint]
    }
}


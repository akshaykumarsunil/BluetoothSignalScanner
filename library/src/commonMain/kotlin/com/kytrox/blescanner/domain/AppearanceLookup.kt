package com.kytrox.blescanner.domain

private const val CATEGORY_MASK = 0xFFC0
private const val SUBCATEGORY_MASK = 0x003F
private const val HEX_RADIX = 16
private const val HEX_PAD_LENGTH = 4

/**
 * Resolves Bluetooth SIG Appearance values (16-bit) to
 * human-readable category and subcategory descriptions.
 */
internal object AppearanceLookup {

    private val CATEGORY_MAP: Map<Int, String> = CatalogParser.parseSingle(
        AppearanceCatalog.RAW_ENTRIES,
    )

    internal fun resolve(code: Int): String {
        return CATEGORY_MAP[code] ?: resolveByCategory(code)
    }

    private fun resolveByCategory(code: Int): String {
        val category = code and CATEGORY_MASK
        val subcategory = code and SUBCATEGORY_MASK
        val categoryName = CATEGORY_MAP[category] ?: return formatUnknownAppearance(code)
        return "$categoryName (sub: $subcategory)"
    }

    private fun formatUnknownAppearance(code: Int): String {
        val hexCode = code.toString(HEX_RADIX).padStart(HEX_PAD_LENGTH, '0')
        return "Unknown (0x$hexCode)"
    }
}

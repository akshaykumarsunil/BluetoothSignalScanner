package com.kytrox.blescanner.domain

private const val HEX_RADIX = 16
private const val HEX_PAD_LENGTH = 4

/**
 * Resolves Bluetooth SIG Company Identifiers (16-bit), Member UUIDs,
 * and SDO UUIDs to their registered organization names.
 */
internal object CompanyIdentifierLookup {

    private val COMPANY_MAP: Map<Int, String> = CatalogParser.parseChunks(
        CompanyIdentifierCatalog.CHUNKS,
    )

    internal fun resolve(companyId: Int): String {
        return COMPANY_MAP[companyId] ?: formatUnknownCompany(companyId)
    }

    private fun formatUnknownCompany(companyId: Int): String {
        val hexCode = companyId.toString(HEX_RADIX).padStart(HEX_PAD_LENGTH, '0')
        return "Unknown (0x$hexCode)"
    }
}

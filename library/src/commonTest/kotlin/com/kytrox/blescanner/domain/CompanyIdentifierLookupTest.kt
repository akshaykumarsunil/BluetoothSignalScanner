package com.kytrox.blescanner.domain

import kotlin.test.Test
import kotlin.test.assertEquals

private const val ERICSSON_ID = 0x0000
private const val APPLE_ID = 0x004C
private const val NORDIC_ID = 0x0059
private const val RADIUS_NETWORKS_ID = 0x0118
private const val ZEBRA_COMPANY_ID = 0x01F1
private const val SOTI_COMPANY_ID = 0x113C
private const val ZEBRA_MEMBER_UUID_1 = 0xFD66
private const val ZEBRA_MEMBER_UUID_2 = 0xFE79
private const val GOOGLE_EDDYSTONE_UUID = 0xFEAA
private const val MATTER_SDO_UUID = 0xFFF6
private const val UNASSIGNED_ID = 0x9999

class CompanyIdentifierLookupTest {

    @Test
    fun resolvesOfficialCompanyIdentifiersFromFullCatalog() {
        assertEquals("Ericsson AB", CompanyIdentifierLookup.resolve(ERICSSON_ID))
        assertEquals("Apple, Inc.", CompanyIdentifierLookup.resolve(APPLE_ID))
        assertEquals("Nordic Semiconductor ASA", CompanyIdentifierLookup.resolve(NORDIC_ID))
        assertEquals("Radius Networks, Inc.", CompanyIdentifierLookup.resolve(RADIUS_NETWORKS_ID))
        assertEquals("Zebra Technologies Corporation", CompanyIdentifierLookup.resolve(ZEBRA_COMPANY_ID))
        assertEquals("SOTI INC.", CompanyIdentifierLookup.resolve(SOTI_COMPANY_ID))
    }

    @Test
    fun resolvesMemberAndSdoIdentifiersAcrossAssignedNumbersFolders() {
        assertEquals("Zebra Technologies Corporation", CompanyIdentifierLookup.resolve(ZEBRA_MEMBER_UUID_1))
        assertEquals("Zebra Technologies", CompanyIdentifierLookup.resolve(ZEBRA_MEMBER_UUID_2))
        assertEquals("Google LLC", CompanyIdentifierLookup.resolve(GOOGLE_EDDYSTONE_UUID))
        assertEquals("Matter Profile ID", CompanyIdentifierLookup.resolve(MATTER_SDO_UUID))
    }

    @Test
    fun formatsUnknownCompanyId() {
        assertEquals("Unknown (0x9999)", CompanyIdentifierLookup.resolve(UNASSIGNED_ID))
    }
}

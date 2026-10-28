package com.kytrox.blescanner.domain

import kotlin.test.Test
import kotlin.test.assertEquals

private const val HEART_RATE_SENSOR_CODE = 0x0340
private const val SMARTWATCH_CODE = 0x00C2
private const val DESKTOP_WORKSTATION_CODE = 0x0081

class AppearanceLookupTest {

    @Test
    fun resolvesCategoriesAndSubcategoriesFromFullCatalog() {
        assertEquals("Heart Rate Sensor", AppearanceLookup.resolve(HEART_RATE_SENSOR_CODE))
        assertEquals("Watch: Smartwatch", AppearanceLookup.resolve(SMARTWATCH_CODE))
        assertEquals("Computer: Desktop Workstation", AppearanceLookup.resolve(DESKTOP_WORKSTATION_CODE))
    }
}


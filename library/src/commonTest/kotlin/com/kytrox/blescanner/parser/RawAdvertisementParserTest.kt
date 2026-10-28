package com.kytrox.blescanner.parser

import com.kytrox.blescanner.domain.AdType
import com.kytrox.blescanner.domain.BleSignal
import com.kytrox.blescanner.domain.DiscoveryMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

private const val TEST_ADDRESS = "11:22:33:44:55:66"
private const val TEST_RSSI = -55
private const val TEST_TIMESTAMP = 2000L
private const val ZEBRA_COMPANY_ID = 0x01F1

// 28 bytes (56 hex chars) Zebra custom beacon data:
// 0x21 (128-bit UUID Service Data, 18 bytes): 11 21 a1b2c3d4e5f60718293a4b5c6d7e8f90
// 0xFF (Manufacturer Specific Data, 10 bytes): 09 ff f101 deadbeefcafe
private const val ZEBRA_CUSTOM_BEACON_56_HEX =
    "1121a1b2c3d4e5f60718293a4b5c6d7e8f9009fff101deadbeefcafe"

// Full 31-byte (62 hex chars) legacy BLE advertisement = 3-byte Flags (020106) + 28-byte custom data
private const val ZEBRA_FULL_31_BYTE_PACKET_HEX =
    "0201061121a1b2c3d4e5f60718293a4b5c6d7e8f9009fff101deadbeefcafe"

class RawAdvertisementParserTest {

    @Test
    fun parsesZebra56HexCustomBeaconDataIntoRawAdvertisement() {
        assertEquals(MAX_CUSTOM_BEACON_HEX_CHARS, ZEBRA_CUSTOM_BEACON_56_HEX.length)
        val raw = RawAdvertisementParser.parseHex(
            deviceAddress = TEST_ADDRESS,
            rssi = TEST_RSSI,
            timestampMillis = TEST_TIMESTAMP,
            hexPayload = ZEBRA_CUSTOM_BEACON_56_HEX,
        )
        assertEquals(MAX_CUSTOM_BEACON_BYTES, raw.rawBytes.size)
        assertEquals(1, raw.serviceData128Bit.size)
        assertEquals(1, raw.manufacturerSpecificData.size)
        assertEquals("f101deadbeefcafe", raw.manufacturerSpecificData.first().toHexString())
    }

    @Test
    fun parsesFull31ByteLegacyAdvertisementAndMapsToBleSignalSig() {
        val raw = RawAdvertisementParser.parseHex(TEST_ADDRESS, TEST_RSSI, TEST_TIMESTAMP, ZEBRA_FULL_31_BYTE_PACKET_HEX)
        assertEquals(MAX_LEGACY_ADVERTISEMENT_BYTES, raw.rawBytes.size)
        val signal = AdvertisementParser.parse(raw)
        assertIs<BleSignal.Sig>(signal)
        assertTrue(signal.discoveryModes.contains(DiscoveryMode.GENERAL_DISCOVERABLE))
        assertEquals("a1b2c3d4-e5f6-0718-293a-4b5c6d7e8f90", signal.serviceUuids.first())
        assertEquals(ZEBRA_COMPANY_ID, signal.manufacturerSpecificData.first().companyId)
        assertEquals("Zebra Technologies Corporation", signal.manufacturerSpecificData.first().companyName)
        assertEquals("deadbeefcafe", signal.manufacturerSpecificData.first().payload)
    }

    @Test
    fun routesMultipleAdTypesCorrectly() {
        val hex = "02010604085a6562021c00053043617374"
        val raw = RawAdvertisementParser.parseHex(TEST_ADDRESS, TEST_RSSI, TEST_TIMESTAMP, hex)
        val signal = AdvertisementParser.parse(raw)

        assertIs<BleSignal.Sig>(signal)
        assertNotNull(raw.flags)
        assertEquals("Zeb", signal.shortenedLocalName)
        assertEquals("Peripheral Only", signal.leRole)
        assertEquals("Cast", signal.broadcastName)
        assertTrue(signal.adStructures.containsKey(AdType.FLAGS))
    }
}

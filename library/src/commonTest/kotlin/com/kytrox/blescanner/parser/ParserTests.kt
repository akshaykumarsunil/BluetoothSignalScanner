package com.kytrox.blescanner.parser

import com.kytrox.blescanner.domain.BleSignal
import com.kytrox.blescanner.domain.DiscoveryMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private const val DEFAULT_ADDRESS = "AA:BB:CC:DD:EE:FF"
private const val DEFAULT_RSSI = -70
private const val DEFAULT_TIMESTAMP = 1000L
private const val NORDIC_COMPANY_ID = 0x0059
private const val EXPECTED_REF_RSSI = -61
private const val EXPECTED_MFG_RESERVED = 0x42
private const val EXPECTED_UID_TX_POWER = -24
private const val EXPECTED_URL_TX_POWER = -16
private const val EXPECTED_BATTERY_MV = 3000
private const val EXPECTED_TEMP_CELSIUS = 23.5f
private const val EXPECTED_ADV_COUNT = 256L
private const val EXPECTED_UPTIME_SECONDS = 10.0f
private const val FLOAT_TOLERANCE = 0.01f
private const val EXPECTED_UINT16_BE = 3000
private const val EXPECTED_UINT16_LE = 0x004C
private const val EXPECTED_DISCOVERY_MODES_COUNT = 2
private const val FLAGS_GENERAL_AND_NO_BREDR = 0x06

private const val ALTBEACON_PACKET_HEX =
    "0201061bff5900beac0102030405060708090a0b0c0d0e0f1011121314c342"
private const val EDDYSTONE_UID_PACKET_HEX =
    "0201061716aafe00e80102030405060708090a0b0c0d0e0f100000"
private const val EDDYSTONE_URL_PACKET_HEX =
    "0201060d16aafe10f001676f6f676c6500"
private const val EDDYSTONE_TLM_PACKET_HEX =
    "0201061116aafe20000bb817800000010000000064"
private const val SIG_FULL_PACKET_HEX =
    "020106050954657374020af403030d180319400305ff4c00010204160d180a"

class AltBeaconParserTest {

    @Test
    fun parsesValidAltBeaconFromRawPacket() {
        val raw = parseHexAdvertisement(ALTBEACON_PACKET_HEX)
        val result = AltBeaconParser.parse(raw)

        assertNotNull(result)
        assertEquals(NORDIC_COMPANY_ID, result.manufacturerId)
        assertEquals("Nordic Semiconductor ASA", result.manufacturerName)
        assertEquals("0102030405060708090a0b0c0d0e0f1011121314", result.beaconId)
        assertEquals(EXPECTED_REF_RSSI, result.refRssi)
        assertEquals(EXPECTED_MFG_RESERVED, result.mfgReserved)
    }

    @Test
    fun returnsNullForNonAltBeacon() {
        val raw = parseHexAdvertisement("06ff4c00021501")
        val result = AltBeaconParser.parse(raw)
        assertNull(result)
    }

    @Test
    fun returnsNullForTooShortData() {
        val raw = parseHexAdvertisement("05ff5900beac")
        val result = AltBeaconParser.parse(raw)
        assertNull(result)
    }
}

class EddystoneParserTest {

    @Test
    fun parsesEddystoneUid() {
        val raw = parseHexAdvertisement(EDDYSTONE_UID_PACKET_HEX)
        val result = EddystoneParser.parse(raw)

        assertIs<BleSignal.Eddystone.Uid>(result)
        assertEquals(EXPECTED_UID_TX_POWER, result.txPower)
        assertEquals("0102030405060708090a", result.namespace)
        assertEquals("0b0c0d0e0f10", result.instance)
    }

    @Test
    fun parsesEddystoneUrl() {
        val raw = parseHexAdvertisement(EDDYSTONE_URL_PACKET_HEX)
        val result = EddystoneParser.parse(raw)

        assertIs<BleSignal.Eddystone.Url>(result)
        assertEquals(EXPECTED_URL_TX_POWER, result.txPower)
        assertEquals("https://www.google.com/", result.url)
    }

    @Test
    fun parsesEddystoneTlm() {
        val raw = parseHexAdvertisement(EDDYSTONE_TLM_PACKET_HEX)
        val result = EddystoneParser.parse(raw)

        assertIs<BleSignal.Eddystone.Tlm>(result)
        assertEquals(0, result.version)
        assertEquals(EXPECTED_BATTERY_MV, result.batteryMilliVolts)
        assertEquals(EXPECTED_TEMP_CELSIUS, result.temperatureCelsius, FLOAT_TOLERANCE)
        assertEquals(EXPECTED_ADV_COUNT, result.advertisementCount)
        assertEquals(EXPECTED_UPTIME_SECONDS, result.uptimeSeconds, FLOAT_TOLERANCE)
    }

    @Test
    fun returnsNullForNonEddystone() {
        val raw = parseHexAdvertisement("020106")
        val result = EddystoneParser.parse(raw)
        assertNull(result)
    }
}

class SigParserTest {

    @Test
    fun parsesFlags() {
        val modes = SigParser.parseFlags(FLAGS_GENERAL_AND_NO_BREDR)
        assertEquals(EXPECTED_DISCOVERY_MODES_COUNT, modes.size)
        assertTrue(modes.contains(DiscoveryMode.GENERAL_DISCOVERABLE))
        assertTrue(modes.contains(DiscoveryMode.BR_EDR_NOT_SUPPORTED))
    }

    @Test
    fun parsesAllFieldsFromRawBytes() {
        val raw = parseHexAdvertisement(SIG_FULL_PACKET_HEX)
        val result = SigParser.parse(raw)

        assertEquals("Test", result.deviceName)
        assertEquals("-12 dBm", result.txPowerLevel)
        assertEquals("0000180d-0000-1000-8000-00805f9b34fb", result.serviceUuids.first())
        assertEquals("Heart Rate Sensor", result.appearance)
        assertEquals(EXPECTED_DISCOVERY_MODES_COUNT, result.discoveryModes.size)
        assertEquals("Apple, Inc.", result.manufacturerSpecificData.first().companyName)
        assertEquals("0102", result.manufacturerSpecificData.first().payload)
        assertEquals("0a", result.serviceData.values.first())
    }

    @Test
    fun handlesEmptyPacket() {
        val raw = parseHexAdvertisement("")
        val result = SigParser.parse(raw)

        assertNull(result.deviceName)
        assertNull(result.txPowerLevel)
        assertNull(result.appearance)
        assertTrue(result.discoveryModes.isEmpty())
        assertTrue(result.manufacturerSpecificData.isEmpty())
    }
}

class AdvertisementParserTest {

    @Test
    fun altBeaconTakesPriority() {
        val result = AdvertisementParser.parseHex(
            deviceAddress = DEFAULT_ADDRESS,
            rssi = DEFAULT_RSSI,
            timestampMillis = DEFAULT_TIMESTAMP,
            hexPayload = ALTBEACON_PACKET_HEX,
        )
        assertIs<BleSignal.AltBeacon>(result)
    }

    @Test
    fun eddystoneTakesPriorityOverSig() {
        val result = AdvertisementParser.parseHex(
            deviceAddress = DEFAULT_ADDRESS,
            rssi = DEFAULT_RSSI,
            timestampMillis = DEFAULT_TIMESTAMP,
            hexPayload = EDDYSTONE_UID_PACKET_HEX,
        )
        assertIs<BleSignal.Eddystone.Uid>(result)
    }

    @Test
    fun fallsBackToSig() {
        val result = AdvertisementParser.parseHex(
            deviceAddress = DEFAULT_ADDRESS,
            rssi = DEFAULT_RSSI,
            timestampMillis = DEFAULT_TIMESTAMP,
            hexPayload = "05094e6f6465",
        )
        assertIs<BleSignal.Sig>(result)
        assertEquals("Node", result.deviceName)
    }
}

class ByteArrayExtensionsTest {

    @Test
    fun toHexStringConvertsBytesToLowercaseHex() {
        val bytes = byteArrayOf(0x0A, 0xBF.toByte(), 0x00, 0xFF.toByte())
        assertEquals("0abf00ff", bytes.toHexString())
    }

    @Test
    fun hexToByteArrayParsesHexString() {
        val bytes = "0abf00ff".hexToByteArray()
        assertEquals("0abf00ff", bytes.toHexString())
    }

    @Test
    fun readUInt16BEReadsCorrectly() {
        val bytes = byteArrayOf(0x0B, 0xB8.toByte())
        assertEquals(EXPECTED_UINT16_BE, bytes.readUInt16BE(0))
    }

    @Test
    fun readUInt16LEReadsCorrectly() {
        val bytes = byteArrayOf(0x4C, 0x00)
        assertEquals(EXPECTED_UINT16_LE, bytes.readUInt16LE(0))
    }

    @Test
    fun readInt8ReadsSignedValues() {
        val bytes = byteArrayOf(0xC3.toByte())
        assertEquals(EXPECTED_REF_RSSI, bytes.readInt8(0))
    }

    @Test
    fun toUuidFormatsFullUuid() {
        val bytes = "0102030405060708090a0b0c0d0e0f10".hexToByteArray()
        assertEquals("01020304-0506-0708-090a-0b0c0d0e0f10", bytes.toUuid())
    }

    @Test
    fun toUuidExpandsShortUuid() {
        val bytes = byteArrayOf(0xFE.toByte(), 0xAA.toByte())
        assertEquals("0000feaa-0000-1000-8000-00805f9b34fb", bytes.toUuid())
    }
}

class EddystoneUrlDecoderTest {

    @Test
    fun decodesHttpsWwwWithComSuffix() {
        val bytes = "01676f6f676c6500".hexToByteArray()
        assertEquals("https://www.google.com/", EddystoneUrlDecoder.decodeUrl(bytes))
    }

    @Test
    fun decodesHttpWithOrgSuffix() {
        val bytes = "026578616d706c6501".hexToByteArray()
        assertEquals("http://example.org/", EddystoneUrlDecoder.decodeUrl(bytes))
    }

    @Test
    fun handlesEmptyInput() {
        assertEquals("", EddystoneUrlDecoder.decodeUrl(byteArrayOf()))
    }
}

private fun parseHexAdvertisement(hexPayload: String): RawAdvertisement {
    return RawAdvertisementParser.parseHex(
        deviceAddress = DEFAULT_ADDRESS,
        rssi = DEFAULT_RSSI,
        timestampMillis = DEFAULT_TIMESTAMP,
        hexPayload = hexPayload,
    )
}

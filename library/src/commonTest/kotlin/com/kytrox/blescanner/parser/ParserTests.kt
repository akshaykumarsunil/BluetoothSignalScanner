package com.kytrox.blescanner.parser

import com.kytrox.blescanner.domain.BleSignal
import com.kytrox.blescanner.domain.DiscoveryMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AltBeaconParserTest {

    @Test
    fun parsesValidAltBeacon() {
        // 0xBEAC marker at bytes 0-1, followed by 20-byte ID, ref RSSI, mfg reserved
        val manufacturerPayload = byteArrayOf(
            0xBE.toByte(), 0xAC.toByte(),                             // beacon code
            0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,          // beacon ID (20 bytes)
            0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10,
            0x11, 0x12, 0x13, 0x14,
            0xC3.toByte(),                                             // ref RSSI = -61
            0x42,                                                      // mfg reserved = 0x42
        )
        val raw = createRawAdvertisement(
            manufacturerData = mapOf(0x0059 to manufacturerPayload),
        )

        val result = AltBeaconParser.parse(raw)

        assertNotNull(result)
        assertEquals(0x0059, result.manufacturerId)
        assertEquals("Nordic Semiconductor ASA", result.manufacturerName)
        assertEquals("0102030405060708090a0b0c0d0e0f1011121314", result.beaconId)
        assertEquals(-61, result.refRssi)
        assertEquals(0x42, result.mfgReserved)
    }

    @Test
    fun returnsNullForNonAltBeacon() {
        val raw = createRawAdvertisement(
            manufacturerData = mapOf(0x004C to byteArrayOf(0x02, 0x15, 0x01)),
        )

        val result = AltBeaconParser.parse(raw)

        assertNull(result)
    }

    @Test
    fun returnsNullForTooShortData() {
        val raw = createRawAdvertisement(
            manufacturerData = mapOf(0x0059 to byteArrayOf(0xBE.toByte(), 0xAC.toByte())),
        )

        val result = AltBeaconParser.parse(raw)

        assertNull(result)
    }
}

class EddystoneParserTest {

    private val eddystoneUuid = "0000feaa-0000-1000-8000-00805f9b34fb"

    @Test
    fun parsesEddystoneUid() {
        val data = byteArrayOf(
            0x00,                                                       // frame type UID
            0xE8.toByte(),                                              // tx power = -24
            0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, // namespace (10 bytes)
            0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10,                       // instance (6 bytes)
            0x00, 0x00,                                                 // RFU
        )
        val raw = createRawAdvertisement(
            serviceData = mapOf(eddystoneUuid to data),
        )

        val result = EddystoneParser.parse(raw)

        assertIs<BleSignal.Eddystone.Uid>(result)
        assertEquals(-24, result.txPower)
        assertEquals("0102030405060708090a", result.namespace)
        assertEquals("0b0c0d0e0f10", result.instance)
    }

    @Test
    fun parsesEddystoneUrl() {
        // https://www.google.com/
        val data = byteArrayOf(
            0x10,                                                       // frame type URL
            0xF0.toByte(),                                              // tx power = -16
            0x01,                                                       // scheme: https://www.
            'g'.code.toByte(), 'o'.code.toByte(), 'o'.code.toByte(),
            'g'.code.toByte(), 'l'.code.toByte(), 'e'.code.toByte(),
            0x00,                                                       // suffix: .com/
        )
        val raw = createRawAdvertisement(
            serviceData = mapOf(eddystoneUuid to data),
        )

        val result = EddystoneParser.parse(raw)

        assertIs<BleSignal.Eddystone.Url>(result)
        assertEquals(-16, result.txPower)
        assertEquals("https://www.google.com/", result.url)
    }

    @Test
    fun parsesEddystoneTlm() {
        val data = byteArrayOf(
            0x20,                                                       // frame type TLM
            0x00,                                                       // version 0
            0x0B, 0xB8.toByte(),                                        // battery: 3000 mV
            0x17, 0x80.toByte(),                                        // temp: 23.5°C (8.8 fixed)
            0x00, 0x00, 0x01, 0x00,                                     // adv count: 256
            0x00, 0x00, 0x00, 0x64,                                     // uptime: 100 (0.1s units) = 10s
        )
        val raw = createRawAdvertisement(
            serviceData = mapOf(eddystoneUuid to data),
        )

        val result = EddystoneParser.parse(raw)

        assertIs<BleSignal.Eddystone.Tlm>(result)
        assertEquals(0, result.version)
        assertEquals(3000, result.batteryMilliVolts)
        assertEquals(23.5f, result.temperatureCelsius, 0.01f)
        assertEquals(256L, result.advertisementCount)
        assertEquals(10.0f, result.uptimeSeconds, 0.01f)
    }

    @Test
    fun returnsNullForNonEddystone() {
        val raw = createRawAdvertisement(serviceData = emptyMap())

        val result = EddystoneParser.parse(raw)

        assertNull(result)
    }
}

class SigParserTest {

    @Test
    fun parsesFlags() {
        val modes = SigParser.parseFlags(0x06)

        assertEquals(2, modes.size)
        assertTrue(modes.contains(DiscoveryMode.GENERAL_DISCOVERABLE))
        assertTrue(modes.contains(DiscoveryMode.BR_EDR_NOT_SUPPORTED))
    }

    @Test
    fun parsesAllFields() {
        val raw = createRawAdvertisement(
            deviceName = "TestDevice",
            txPowerLevel = -12,
            serviceUuids = listOf("0000180d-0000-1000-8000-00805f9b34fb"),
            flags = 0x06,
            appearance = 0x0340,
            manufacturerData = mapOf(0x004C to byteArrayOf(0x01, 0x02)),
            serviceData = mapOf("0000180d-0000-1000-8000-00805f9b34fb" to byteArrayOf(0x0A)),
        )

        val result = SigParser.parse(raw)

        assertEquals("TestDevice", result.deviceName)
        assertEquals("-12 dBm", result.txPowerLevel)
        assertEquals(1, result.serviceUuids.size)
        assertEquals("Heart Rate Sensor", result.appearance)
        assertEquals(2, result.discoveryModes.size)
        assertEquals(1, result.manufacturerSpecificData.size)
        assertEquals("Apple, Inc.", result.manufacturerSpecificData[0].companyName)
        assertEquals("0102", result.manufacturerSpecificData[0].payload)
        assertEquals("0a", result.serviceData.values.first())
    }

    @Test
    fun handlesNullFields() {
        val raw = createRawAdvertisement()

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
        val altBeaconPayload = byteArrayOf(
            0xBE.toByte(), 0xAC.toByte(),
            0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
            0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10,
            0x11, 0x12, 0x13, 0x14,
            0xC3.toByte(), 0x00,
        )
        val raw = createRawAdvertisement(
            manufacturerData = mapOf(0x0059 to altBeaconPayload),
        )

        val result = AdvertisementParser.parse(raw)

        assertIs<BleSignal.AltBeacon>(result)
    }

    @Test
    fun eddystoneTakesPriorityOverSig() {
        val eddystoneData = byteArrayOf(
            0x00, 0xE8.toByte(),
            0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A,
            0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10,
            0x00, 0x00,
        )
        val raw = createRawAdvertisement(
            serviceData = mapOf("0000feaa-0000-1000-8000-00805f9b34fb" to eddystoneData),
        )

        val result = AdvertisementParser.parse(raw)

        assertIs<BleSignal.Eddystone.Uid>(result)
    }

    @Test
    fun fallsBackToSig() {
        val raw = createRawAdvertisement(deviceName = "GenericDevice")

        val result = AdvertisementParser.parse(raw)

        assertIs<BleSignal.Sig>(result)
        assertEquals("GenericDevice", (result as BleSignal.Sig).deviceName)
    }
}

class ByteArrayExtensionsTest {

    @Test
    fun toHexStringConvertsBytesToLowercaseHex() {
        val bytes = byteArrayOf(0x0A, 0xBF.toByte(), 0x00, 0xFF.toByte())
        assertEquals("0abf00ff", bytes.toHexString())
    }

    @Test
    fun readUInt16BEReadsCorrectly() {
        val bytes = byteArrayOf(0x0B, 0xB8.toByte())
        assertEquals(3000, bytes.readUInt16BE(0))
    }

    @Test
    fun readUInt16LEReadsCorrectly() {
        val bytes = byteArrayOf(0x4C, 0x00)
        assertEquals(0x004C, bytes.readUInt16LE(0))
    }

    @Test
    fun readInt8ReadsSignedValues() {
        val bytes = byteArrayOf(0xC3.toByte())
        assertEquals(-61, bytes.readInt8(0))
    }

    @Test
    fun toUuidFormatsFullUuid() {
        val bytes = byteArrayOf(
            0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
            0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10,
        )
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
        val bytes = byteArrayOf(
            0x01,
            'g'.code.toByte(), 'o'.code.toByte(), 'o'.code.toByte(),
            'g'.code.toByte(), 'l'.code.toByte(), 'e'.code.toByte(),
            0x00,
        )
        assertEquals("https://www.google.com/", EddystoneUrlDecoder.decodeUrl(bytes))
    }

    @Test
    fun decodesHttpWithOrgSuffix() {
        val bytes = byteArrayOf(
            0x02,
            'e'.code.toByte(), 'x'.code.toByte(), 'a'.code.toByte(),
            'm'.code.toByte(), 'p'.code.toByte(), 'l'.code.toByte(),
            'e'.code.toByte(),
            0x01,
        )
        assertEquals("http://example.org/", EddystoneUrlDecoder.decodeUrl(bytes))
    }

    @Test
    fun handlesEmptyInput() {
        assertEquals("", EddystoneUrlDecoder.decodeUrl(byteArrayOf()))
    }
}

// --- Test helpers ---

private fun createRawAdvertisement(
    deviceAddress: String = "AA:BB:CC:DD:EE:FF",
    rssi: Int = -70,
    timestampMillis: Long = 1000L,
    deviceName: String? = null,
    txPowerLevel: Int? = null,
    serviceUuids: List<String> = emptyList(),
    serviceData: Map<String, ByteArray> = emptyMap(),
    manufacturerData: Map<Int, ByteArray> = emptyMap(),
    flags: Int? = null,
    appearance: Int? = null,
): RawAdvertisement {
    return RawAdvertisement(
        deviceAddress = deviceAddress,
        rssi = rssi,
        timestampMillis = timestampMillis,
        deviceName = deviceName,
        txPowerLevel = txPowerLevel,
        serviceUuids = serviceUuids,
        serviceData = serviceData,
        manufacturerData = manufacturerData,
        flags = flags,
        appearance = appearance,
    )
}

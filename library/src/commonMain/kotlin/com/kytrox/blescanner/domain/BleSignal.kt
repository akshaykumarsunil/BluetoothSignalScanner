package com.kytrox.blescanner.domain

/**
 * Represents a parsed BLE advertisement signal.
 *
 * Each subtype corresponds to a specific beacon protocol or
 * a standard Bluetooth SIG GAP advertisement.
 */
public sealed interface BleSignal {

    public val rssi: Int
    public val deviceAddress: String
    public val timestampMillis: Long

    /**
     * Eddystone beacon advertisement (Google).
     * Identified by service data UUID 0xFEAA.
     */
    public sealed interface Eddystone : BleSignal {

        /** Eddystone-UID frame: broadcasts a fixed namespace + instance identifier. */
        public data class Uid(
            val txPower: Int,
            val namespace: String,
            val instance: String,
            override val rssi: Int,
            override val deviceAddress: String,
            override val timestampMillis: Long,
        ) : Eddystone

        /** Eddystone-URL frame: broadcasts a compressed URL. */
        public data class Url(
            val txPower: Int,
            val url: String,
            override val rssi: Int,
            override val deviceAddress: String,
            override val timestampMillis: Long,
        ) : Eddystone

        /** Eddystone-TLM frame: broadcasts telemetry data. */
        public data class Tlm(
            val version: Int,
            val batteryMilliVolts: Int,
            val temperatureCelsius: Float,
            val advertisementCount: Long,
            val uptimeSeconds: Float,
            override val rssi: Int,
            override val deviceAddress: String,
            override val timestampMillis: Long,
        ) : Eddystone
    }

    /**
     * AltBeacon advertisement (Radius Networks open standard).
     * Identified by beacon code 0xBEAC in manufacturer data.
     */
    public data class AltBeacon(
        val manufacturerId: Int,
        val manufacturerName: String,
        val beaconId: String,
        val refRssi: Int,
        val mfgReserved: Int,
        override val rssi: Int,
        override val deviceAddress: String,
        override val timestampMillis: Long,
    ) : BleSignal

    /**
     * Standard Bluetooth SIG GAP advertisement.
     * All fields are parsed to human-readable formats.
     */
    public data class Sig(
        val deviceName: String?,
        val txPowerLevel: String?,
        val serviceUuids: List<String>,
        val appearance: String?,
        val discoveryModes: List<DiscoveryMode>,
        val manufacturerSpecificData: List<ManufacturerData>,
        val serviceData: Map<String, String>,
        override val rssi: Int,
        override val deviceAddress: String,
        override val timestampMillis: Long,
    ) : BleSignal
}

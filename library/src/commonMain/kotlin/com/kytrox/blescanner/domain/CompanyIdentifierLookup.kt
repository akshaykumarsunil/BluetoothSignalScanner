package com.kytrox.blescanner.domain

/**
 * Resolves Bluetooth SIG Company Identifiers (16-bit) to
 * their registered company names.
 *
 * Reference: Bluetooth SIG Assigned Numbers — Company Identifiers.
 */
internal object CompanyIdentifierLookup {

    private val COMPANY_MAP: Map<Int, String> = buildCompanyMap()

    internal fun resolve(companyId: Int): String {
        return COMPANY_MAP[companyId]
            ?: "Unknown (0x${companyId.toString(16).padStart(4, '0')})"
    }

    @Suppress("MagicNumber")
    private fun buildCompanyMap(): Map<Int, String> {
        return mapOf(
            0x0000 to "Ericsson Technology Licensing",
            0x0001 to "Nokia Mobile Phones",
            0x0002 to "Intel Corp.",
            0x0003 to "IBM Corp.",
            0x0004 to "Toshiba Corp.",
            0x0005 to "3Com",
            0x0006 to "Microsoft",
            0x000A to "Qualcomm",
            0x000D to "Texas Instruments",
            0x000F to "Broadcom",
            0x001D to "Qualcomm Technologies International (QTIL)",
            0x004C to "Apple, Inc.",
            0x0059 to "Nordic Semiconductor ASA",
            0x005A to "Mitel Semiconductor",
            0x006B to "Samsung Electronics Co. Ltd.",
            0x0075 to "Samsung Electronics Co. Ltd.",
            0x0078 to "Nike, Inc.",
            0x0087 to "Garmin International, Inc.",
            0x008C to "Bose Corporation",
            0x00E0 to "Google",
            0x00FF to "Realtek Semiconductor Corp.",
            0x0106 to "Sony Corporation",
            0x010F to "Xiaomi Communications Co., Ltd.",
            0x0131 to "Huawei Technologies Co., Ltd.",
            0x0157 to "Anhui Huami Information Technology Co., Ltd.",
            0x015D to "Shenzhen Goodix Technology Co., Ltd.",
            0x0171 to "Amazon.com Services, LLC",
            0x018D to "Tile, Inc.",
            0x01A7 to "LEGO System A/S",
            0x01C6 to "Zebra Technologies Corporation",
            0x0201 to "Harman International Industries, Inc.",
            0x0224 to "Meta Platforms, Inc.",
            0x02A5 to "Espressif Systems (Shanghai) Co., Ltd.",
            0x02E5 to "Govee Moments, LLC",
            0x0310 to "Bosch Sensortec GmbH",
            0x0312 to "Bang & Olufsen A/S",
            0x035D to "Shenzhen Intellirocks Tech Co., Ltd.",
            0x038F to "Daikin Industries, Ltd.",
            0x03DA to "Mitsubishi Electric Corporation",
            0x0488 to "Dyson Technology Limited",
            0x0499 to "Ruuvi Innovations Ltd.",
            0x0583 to "Sensirion AG",
            0x05A7 to "Tuya Global Inc.",
            0x0822 to "Aqara",
            0x09E1 to "SwitchBot",
            0xEC88 to "Shenzhen Intellirocks Tech Co., Ltd.",
        )
    }
}

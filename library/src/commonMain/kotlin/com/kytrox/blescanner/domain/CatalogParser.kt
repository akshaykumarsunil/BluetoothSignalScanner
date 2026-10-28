package com.kytrox.blescanner.domain

private const val HEX_RADIX = 16
private const val ENTRY_DELIMITER = '='
private const val EXPECTED_PARTS = 2

internal object CatalogParser {

    internal fun parseChunks(chunks: List<String>): Map<Int, String> {
        return buildMap {
            for (chunk in chunks) {
                parseChunkInto(chunk, this)
            }
        }
    }

    internal fun parseSingle(rawEntries: String): Map<Int, String> {
        return buildMap {
            parseChunkInto(rawEntries, this)
        }
    }

    private fun parseChunkInto(chunk: String, destination: MutableMap<Int, String>) {
        for (line in chunk.lineSequence()) {
            parseLineInto(line.trim(), destination)
        }
    }

    private fun parseLineInto(trimmedLine: String, destination: MutableMap<Int, String>) {
        if (trimmedLine.isEmpty()) return
        val parts = trimmedLine.split(ENTRY_DELIMITER, limit = EXPECTED_PARTS)
        if (parts.size < EXPECTED_PARTS) return
        val key = parts[0].toIntOrNull(HEX_RADIX) ?: return
        destination[key] = parts[1]
    }
}


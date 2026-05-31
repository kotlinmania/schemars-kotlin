// port-lint: source encoding.rs
package io.github.kotlinmania.schemars

private fun Char.isHexDigit(): Boolean = this in '0'..'9' || this in 'a'..'f' || this in 'A'..'F'

/** Encodes a string for insertion into a JSON Pointer in URI fragment representation. */
fun encodeRefName(name: String): String {
    fun needsEncoding(byte: Int): Boolean =
        when (byte) {
            // `~` and `/` need encoding for JSON Pointer
            // See https://datatracker.ietf.org/doc/html/rfc6901#section-3
            '~'.code, '/'.code -> true
            // These chars (and `~`) are valid in URL fragment
            // See https://datatracker.ietf.org/doc/html/rfc3986/#section-3.5
            '!'.code, '$'.code, '_'.code -> false
            in '&'.code..';'.code -> false
            '='.code -> false
            in '?'.code..'Z'.code -> false
            in 'a'.code..'z'.code -> false
            // Everything else needs percent-encoding
            else -> true
        }

    val bytes = name.encodeToByteArray()
    val unsignedBytes = IntArray(bytes.size) { bytes[it].toInt() and 0xFF }
    if (unsignedBytes.none { needsEncoding(it) }) {
        return name
    }

    val buf = StringBuilder()
    for (byte in unsignedBytes) {
        when {
            byte == '~'.code -> buf.append("~0")
            byte == '/'.code -> buf.append("~1")
            needsEncoding(byte) -> {
                buf.append('%')
                buf.append(byte.toString(16).uppercase().padStart(2, '0'))
            }
            else -> buf.append(byte.toChar())
        }
    }
    return buf.toString()
}

/**
 * Percent-decodes the given string, returning `null` if it results in invalid UTF-8.
 * A `%` that is not followed by two hex digits is treated as a literal `%`.
 */
fun percentDecode(s: String): String? {
    if (!s.contains('%')) {
        return s
    }

    val buf = ArrayList<Byte>()
    val segments = s.split('%')
    val first = segments.firstOrNull() ?: ""
    for (b in first.encodeToByteArray()) buf.add(b)

    for (segment in segments.drop(1)) {
        val decodedByte: Int? =
            if (segment.length >= 2 &&
                segment[0].isHexDigit() &&
                segment[1].isHexDigit()
            ) {
                segment.substring(0, 2).toInt(16)
            } else {
                null
            }
        if (decodedByte != null) {
            buf.add(decodedByte.toByte())
            for (b in segment.substring(2).encodeToByteArray()) buf.add(b)
        } else {
            buf.add('%'.code.toByte())
            for (b in segment.encodeToByteArray()) buf.add(b)
        }
    }

    val bytes = ByteArray(buf.size) { buf[it] }
    return try {
        bytes.decodeToString(throwOnInvalidSequence = true)
    } catch (_: CharacterCodingException) {
        null
    }
}

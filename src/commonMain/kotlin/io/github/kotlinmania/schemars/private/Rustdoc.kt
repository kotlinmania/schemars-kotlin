// port-lint: source _private/rustdoc.rs
package io.github.kotlinmania.schemars.private

/**
 * Splits a doc comment into a `(title, description)` pair.
 *
 * If the first non-whitespace line starts with `#`, that line (with leading `#`s and surrounding
 * whitespace stripped) is the title and the remainder is the description. Otherwise the title
 * is empty and the entire doc is the description.
 */
fun getTitleAndDescription(doc: String): Pair<String, String> {
    val docBytes = trimAscii(doc)

    return if (docBytes.isNotEmpty() && docBytes[0] == '#') {
        val titleEndIndex = strchr(docBytes, '\n') ?: docBytes.length

        val title = trimAscii(trimStart(docBytes.substring(0, titleEndIndex), '#'))
        val description = trimAscii(docBytes.substring(titleEndIndex, docBytes.length))

        title to description
    } else {
        "" to docBytes
    }
}

private fun strchr(bytes: String, chr: Char): Int? {
    val i = bytes.indexOf(chr)
    return if (i < 0) null else i
}

private fun trimStart(bytes: String, chr: Char): String {
    var i = 0
    while (i < bytes.length && bytes[i] == chr) i++
    return bytes.substring(i)
}

private fun trimAscii(bytes: String): String {
    var start = 0
    while (start < bytes.length && bytes[start].isAsciiWhitespace()) start++
    var end = bytes.length
    while (end > start && bytes[end - 1].isAsciiWhitespace()) end--
    return bytes.substring(start, end)
}

private fun Char.isAsciiWhitespace(): Boolean = when (this) {
    ' ', '\t', '\n', '', '\r' -> true
    else -> false
}

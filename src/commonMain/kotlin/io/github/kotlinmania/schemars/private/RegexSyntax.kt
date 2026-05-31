// port-lint: source _private/regex_syntax.rs
package io.github.kotlinmania.schemars.private

// Copyright (c) 2014 The Rust Project Developers — MIT licence applies to this routine; see
// the LICENSE file at the repository root.

fun escape(text: String): String {
    val quoted = StringBuilder()
    escapeInto(text, quoted)
    return quoted.toString()
}

private fun escapeInto(
    text: String,
    buf: StringBuilder,
) {
    buf.ensureCapacity(buf.length + text.length)
    for (c in text) {
        if (isMetaCharacter(c)) {
            buf.append('\\')
        }
        buf.append(c)
    }
}

private fun isMetaCharacter(c: Char): Boolean =
    when (c) {
        '\\', '.', '+', '*', '?', '(', ')', '|', '[', ']', '{', '}', '^', '$',
        '#', '&', '-', '~',
        -> true
        else -> false
    }

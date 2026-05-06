// port-lint: ignore
// Subset of `serde_json::Value` used by schemars. To be replaced wholesale once
// `serde-json-kotlin` matures and exposes the full `Value` / `Map` / `Number` API.
package io.github.kotlinmania.schemars

/**
 * A JSON value. Mirrors the variants of `serde_json::Value` so the schemars port can
 * reproduce its mutation patterns (`as_object_mut`, in-place `insert`/`remove`/`entry`,
 * etc.) without depending on the full upstream serde_json crate.
 *
 * Object insertion order is preserved (matches `serde_json::Map`'s `LinkedHashMap`-backed
 * default). Arrays use [MutableList].
 */
sealed class Value {
    /** JSON `null`. */
    data object Null : Value()

    /** JSON boolean. */
    data class Bool(val value: Boolean) : Value()

    /** JSON number. Stores the underlying [kotlin.Number]; equality is value-based. */
    data class Number(val value: kotlin.Number) : Value() {
        companion object {
            fun fromInt(n: Int): Number = Number(n)
            fun fromLong(n: Long): Number = Number(n)
            fun fromDouble(n: Double): Number = Number(n)
            fun fromUInt(n: UInt): Number = Number(n.toLong())
            fun fromULong(n: ULong): Number = Number(n.toLong())
        }

        /** Whether the underlying number fits in an unsigned 64-bit integer. */
        fun asULong(): ULong? = when (val v = value) {
            is Int -> if (v >= 0) v.toULong() else null
            is Long -> if (v >= 0L) v.toULong() else null
            is Short -> if (v >= 0) v.toULong() else null
            is Byte -> if (v >= 0) v.toULong() else null
            else -> null
        }

        /** Whether the underlying number fits in a signed 64-bit integer. */
        fun asLong(): Long? = when (val v = value) {
            is Int -> v.toLong()
            is Long -> v
            is Short -> v.toLong()
            is Byte -> v.toLong()
            else -> null
        }

        /** Whether the underlying number fits in a 64-bit float. */
        fun asDouble(): Double? = value.toDouble()
    }

    /** JSON string. */
    data class Str(val value: String) : Value()

    /** JSON array. Backed by a [MutableList] so the upstream `as_array_mut` semantics work. */
    data class Array(val items: MutableList<Value>) : Value() {
        constructor() : this(mutableListOf())
    }

    /** JSON object. Backed by a [LinkedHashMap] so insertion order is preserved. */
    data class Object(val entries: MutableMap<String, Value>) : Value() {
        constructor() : this(linkedMapOf())
    }

    /** Returns `true` if this value is JSON `null`. */
    fun isNull(): Boolean = this === Null

    /** If this value is a [Bool], return its underlying boolean; otherwise `null`. */
    fun asBool(): Boolean? = (this as? Bool)?.value

    /** If this value is a [Str], return its underlying string; otherwise `null`. */
    fun asStr(): String? = (this as? Str)?.value

    /** If this value is an [Array], return its mutable list; otherwise `null`. */
    fun asArray(): MutableList<Value>? = (this as? Array)?.items

    /** If this value is an [Object], return its mutable map; otherwise `null`. */
    fun asObject(): MutableMap<String, Value>? = (this as? Object)?.entries

    /**
     * If this value is an [Object], look up `key`. If this value is an [Array] and `key` is
     * a numeric string, look up by index. Otherwise return `null`.
     *
     * Mirrors `serde_json::Value::get(&self, index: I)` for the `&str` overload.
     */
    fun get(key: String): Value? = when (this) {
        is Object -> entries[key]
        is Array -> key.toIntOrNull()?.let { items.getOrNull(it) }
        else -> null
    }

    /**
     * Look up a value by JSON Pointer (RFC 6901).
     *
     * Returns `null` if the pointer doesn't resolve. The empty pointer returns this value.
     * A non-empty pointer must begin with `/`.
     */
    fun pointer(pointer: String): Value? {
        if (pointer.isEmpty()) return this
        if (!pointer.startsWith('/')) return null
        var cur: Value = this
        for (token in pointer.substring(1).split('/')) {
            val decoded = token.replace("~1", "/").replace("~0", "~")
            cur = when (cur) {
                is Object -> cur.entries[decoded] ?: return null
                is Array -> {
                    val idx = decoded.toIntOrNull() ?: return null
                    cur.items.getOrNull(idx) ?: return null
                }
                else -> return null
            }
        }
        return cur
    }

    companion object {
        /** Coerce a Kotlin value to [Value]. Mirrors the upstream `From` impls. */
        fun of(v: Any?): Value = when (v) {
            null -> Null
            is Value -> v
            is Schema -> v.asValue()
            is Boolean -> Bool(v)
            is Int -> Number.fromInt(v)
            is Long -> Number.fromLong(v)
            is Short -> Number.fromInt(v.toInt())
            is Byte -> Number.fromInt(v.toInt())
            is UInt -> Number.fromUInt(v)
            is ULong -> Number.fromULong(v)
            is Double -> Number.fromDouble(v)
            is Float -> Number.fromDouble(v.toDouble())
            is String -> Str(v)
            is List<*> -> Array(v.mapTo(mutableListOf()) { of(it) })
            is Map<*, *> -> Object(
                v.entries.fold(linkedMapOf<String, Value>()) { acc, e ->
                    acc[e.key.toString()] = of(e.value); acc
                },
            )
            else -> error("Unsupported Value source: ${v::class}")
        }
    }
}

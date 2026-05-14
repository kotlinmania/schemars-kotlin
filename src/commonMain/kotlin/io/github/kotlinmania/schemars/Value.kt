// port-lint: ignore  -- shared in-package JSON value type used by Schema.
package io.github.kotlinmania.schemars

/** A JSON value: null, bool, number, string, array, or object with insertion-order entries. */
sealed class Value {
    /** JSON `null`. */
    data object Null : Value()

    /** JSON boolean. */
    data class Bool(val value: Boolean) : Value()

    /** JSON number. */
    data class Number(val value: kotlin.Number) : Value() {
        companion object {
            fun fromInt(n: Int): Number = Number(n)
            fun fromLong(n: Long): Number = Number(n)
            fun fromDouble(n: Double): Number = Number(n)
            fun fromUInt(n: UInt): Number = Number(n.toLong())
            fun fromULong(n: ULong): Number = Number(n.toLong())
        }

        /** Returns the underlying number as an unsigned 64-bit integer, if it fits. */
        fun asULong(): ULong? = when (val v = value) {
            is Int -> if (v >= 0) v.toULong() else null
            is Long -> if (v >= 0L) v.toULong() else null
            is Short -> if (v >= 0) v.toULong() else null
            is Byte -> if (v >= 0) v.toULong() else null
            else -> null
        }

        /** Returns the underlying number as a signed 64-bit integer, if it fits. */
        fun asLong(): Long? = when (val v = value) {
            is Int -> v.toLong()
            is Long -> v
            is Short -> v.toLong()
            is Byte -> v.toLong()
            else -> null
        }

        /** Returns the underlying number as a 64-bit float. */
        fun asDouble(): Double? = value.toDouble()
    }

    /** JSON string. */
    data class Str(val value: String) : Value()

    /** JSON array. */
    data class Array(val items: MutableList<Value>) : Value() {
        constructor() : this(mutableListOf())
    }

    /** JSON object whose entries preserve insertion order. */
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
        /** Coerce a Kotlin value to [Value]. */
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

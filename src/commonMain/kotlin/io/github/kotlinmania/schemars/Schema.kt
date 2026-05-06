// port-lint: source schema.rs
package io.github.kotlinmania.schemars

/*
JSON Schema types.
*/

/**
 * A JSON Schema.
 *
 * This wraps a JSON [Value] that must be either an [object][Value.Object] or a
 * [bool][Value.Bool].
 *
 * A custom JSON schema can be created using the [jsonSchema] builder:
 * ```
 * import io.github.kotlinmania.schemars.Schema
 * import io.github.kotlinmania.schemars.jsonSchema
 *
 * val mySchema: Schema = jsonSchema {
 *     "type" to listOf("object", "null")
 * }
 * ```
 *
 * Because a `Schema` is a thin wrapper around a `Value`, you can also use [Schema.tryFrom] to
 * create a `Schema` from an existing `Value`. This operation is fallible, because only
 * [objects][Value.Object] and [bools][Value.Bool] can be converted in this way.
 *
 * Similarly, you can use [Schema.from] to (infallibly) create a `Schema` from an existing
 * `Map<String, Value>` or `Boolean`.
 */
class Schema private constructor(private var innerRef: Value) {
    /** The currently wrapped JSON value. Mutable to support [ensureObject]'s bool→object upgrade. */
    val inner: Value
        get() = innerRef

    companion object {
        /**
         * Creates a new schema object with a single string property `"${'$'}ref"`.
         *
         * The given reference string should be a URI reference. This will usually be a JSON Pointer
         * in [URI Fragment representation](https://tools.ietf.org/html/rfc6901#section-6).
         */
        fun newRef(reference: String): Schema {
            val map = linkedMapOf<String, Value>()
            map["\$ref"] = Value.Str(reference)
            return Schema(Value.Object(map))
        }

        /** Construct a `Schema` from an object map of properties. */
        fun from(o: MutableMap<String, Value>): Schema = Schema(Value.Object(o))

        /** Construct a `Schema` from a bool. */
        fun from(b: Boolean): Schema = Schema(Value.Bool(b))

        /** Construct an empty (always-passes) schema, equivalent to `{}`. */
        fun default(): Schema = Schema(Value.Object(linkedMapOf()))

        /**
         * Try to construct a `Schema` from a `Value`. Returns the wrapped schema on success or
         * a [SchemaConversionException] describing the unexpected type.
         */
        fun tryFrom(value: Value): Result<Schema> {
            validate(value)?.let { return Result.failure(it) }
            return Result.success(Schema(value))
        }

        internal fun validate(value: Value): SchemaConversionException? {
            val unexpected: String = when (value) {
                is Value.Bool, is Value.Object -> return null
                is Value.Null -> "unit"
                is Value.Number -> when (value.value) {
                    is Int, is Long, is Short, is Byte -> "integer ${value.asLong()}"
                    is Double, is Float -> "float ${value.asDouble()}"
                    else -> "number ${value.value}"
                }
                is Value.Str -> "string ${value.value}"
                is Value.Array -> "sequence"
            }
            return SchemaConversionException("invalid type: $unexpected, expected object or boolean")
        }
    }

    /** Borrows the `Schema`'s underlying JSON value. */
    fun asValue(): Value = innerRef

    /** If the `Schema`'s underlying JSON value is a bool, returns the bool value. */
    fun asBool(): Boolean? = innerRef.asBool()

    /**
     * If the `Schema`'s underlying JSON value is an object, returns the object as a mutable
     * `Map` of properties. (Kotlin doesn't distinguish `&` from `&mut` references; the upstream
     * split between [asObject] and [asObjectMut] is preserved as separate methods for parity.)
     */
    fun asObject(): MutableMap<String, Value>? = innerRef.asObject()

    /** Mutable variant of [asObject]; same return value. */
    fun asObjectMut(): MutableMap<String, Value>? = innerRef.asObject()

    /**
     * Consumes this schema and returns its object map.
     *
     * Mirrors the upstream `Result<Map<String, Value>, bool>` — a [TryToObjectResult.Ok]
     * carries the map, a [TryToObjectResult.Err] carries the underlying bool.
     */
    fun tryToObject(): TryToObjectResult = when (val v = innerRef) {
        is Value.Object -> TryToObjectResult.Ok(v.entries)
        is Value.Bool -> TryToObjectResult.Err(v.value)
        else -> error("Schema inner value should always be Object or Bool")
    }

    /**
     * If this schema wraps an object, returns the mutable map. Otherwise returns the underlying
     * bool via [TryToObjectResult.Err].
     */
    fun tryAsObjectMut(): TryToObjectResult = when (val v = innerRef) {
        is Value.Object -> TryToObjectResult.Ok(v.entries)
        is Value.Bool -> TryToObjectResult.Err(v.value)
        else -> error("Schema inner value should always be Object or Bool")
    }

    /** Returns the `Schema`'s underlying JSON value. */
    fun toValue(): Value = innerRef

    /**
     * Converts the `Schema` (if it wraps a bool value) into an equivalent object schema, then
     * returns its mutable map.
     *
     * `true` is transformed into an empty schema `{}`, which successfully validates against all
     * possible values. `false` is transformed into the schema `{"not": {}}`, which does not
     * successfully validate against any value.
     */
    fun ensureObject(): MutableMap<String, Value> {
        val b = asBool()
        if (b != null) {
            val map = linkedMapOf<String, Value>()
            if (!b) {
                map["not"] = Value.Object(linkedMapOf())
            }
            innerRef = Value.Object(map)
        }
        return innerRef.asObject() ?: error("Schema value should be of type Object.")
    }

    /**
     * Inserts a property into the schema, replacing any previous value.
     *
     * If the schema wraps a bool value, it will first be converted into an equivalent object
     * schema via [ensureObject]. Returns the previous value, if any.
     */
    fun insert(k: String, v: Value): Value? {
        val obj = ensureObject()
        return obj.put(k, v)
    }

    /**
     * If the `Schema`'s underlying JSON value is an object, gets a reference to that object's
     * value for the given key if it exists. Always returns `null` for bool schemas.
     */
    fun get(key: String): Value? = innerRef.asObject()?.get(key)

    /** Mutable variant of [get]; same return value. */
    fun getMut(key: String): Value? = innerRef.asObject()?.get(key)

    /**
     * If the `Schema`'s underlying JSON value is an object, looks up a value within the schema
     * by a JSON Pointer.
     *
     * If the given pointer begins with a `#`, then the rest of the value is assumed to be in
     * "URI Fragment Identifier Representation", and will be percent-decoded accordingly.
     *
     * For more information on JSON Pointer, read [RFC6901](https://tools.ietf.org/html/rfc6901).
     *
     * This always returns `null` for bool schemas.
     */
    fun pointer(pointer: String): Value? {
        return if (pointer.startsWith("#")) {
            val decoded = percentDecode(pointer.substring(1)) ?: return null
            innerRef.pointer(decoded)
        } else {
            innerRef.pointer(pointer)
        }
    }

    /** Mutable variant of [pointer]; same lookup semantics. */
    fun pointerMut(pointer: String): Value? = pointer(pointer)

    /**
     * If the `Schema`'s underlying JSON value is an object, removes and returns its value for the
     * given key. Always returns `null` for bool schemas, without modifying them.
     */
    fun remove(key: String): Value? = innerRef.asObject()?.remove(key)

    /**
     * Returns `true` if the schema's `type` keyword names [ty]. The keyword may be a string
     * (single type) or an array of strings (union types).
     */
    internal fun hasType(ty: String): Boolean {
        return when (val t = innerRef.asObject()?.get("type")) {
            is Value.Array -> t.items.any { it.asStr() == ty }
            is Value.Str -> t.value == ty
            else -> false
        }
    }

    override fun equals(other: Any?): Boolean = when {
        other === this -> true
        other is Schema -> other.innerRef == innerRef
        other is Boolean -> asBool() == other
        other is Map<*, *> -> {
            val obj = innerRef.asObject() ?: return false
            obj == other
        }
        other is Value -> innerRef == other
        else -> false
    }

    override fun hashCode(): Int = innerRef.hashCode()

    override fun toString(): String = "Schema($innerRef)"
}

/**
 * A schema-conversion error raised when [Schema.tryFrom] receives a value that is neither an
 * object nor a bool.
 */
class SchemaConversionException(message: String) : RuntimeException(message)

/** Result of [Schema.tryToObject] / [Schema.tryAsObjectMut]. */
sealed class TryToObjectResult {
    data class Ok(val entries: MutableMap<String, Value>) : TryToObjectResult()
    data class Err(val bool: Boolean) : TryToObjectResult()
}

/** Convert a bool to a `Schema` using `Schema.from(this)`. */
fun Boolean.toSchema(): Schema = Schema.from(this)

/** Convert an object map to a `Schema` using `Schema.from(this)`. */
fun MutableMap<String, Value>.toSchema(): Schema = Schema.from(this)

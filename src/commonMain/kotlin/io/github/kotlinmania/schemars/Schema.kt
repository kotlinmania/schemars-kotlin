// port-lint: source schema.rs
package io.github.kotlinmania.schemars

import io.github.kotlinmania.schemars.generate.SchemaGenerator

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
class Schema private constructor(private var innerRef: Value) : JsonSchema {
    internal val inner: Value
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
        internal fun from(o: MutableMap<String, Value>): Schema = Schema(Value.Object(o))

        /** Construct a `Schema` from a bool. */
        fun from(b: Boolean): Schema = Schema(Value.Bool(b))

        /** Construct an empty (always-passes) schema, equivalent to `{}`. */
        fun default(): Schema = Schema(Value.Object(linkedMapOf()))

        /**
         * Try to construct a `Schema` from a `Value`. Returns the wrapped schema on success or
         * a [SchemaConversionException] describing the unexpected type.
         */
        internal fun tryFrom(value: Value): SchemaResult {
            validate(value)?.let { return SchemaResult.err(it) }
            return SchemaResult.ok(Schema(value))
        }

        /**
         * Mirrors the upstream Rust `Deserialize` impl on [Schema]: validates that the underlying
         * [Value] is either an [Object][Value.Object] or a [Bool][Value.Bool], and wraps it in a
         * [Schema]. The Rust impl rejects every other JSON shape — null, number, string, array —
         * via `serde::de::Error::invalid_type`; the Kotlin port surfaces the same rejection via
         * [SchemaResult.error] carrying a [SchemaConversionError].
         */
        internal fun deserialize(value: Value): SchemaResult = tryFrom(value)

        internal fun validate(value: Value): SchemaConversionError? {
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
            return SchemaConversionError("invalid type: $unexpected, expected object or boolean")
        }
    }

    /** Borrows the `Schema`'s underlying JSON value. */
    internal fun asValue(): Value = innerRef

    /** If the `Schema`'s underlying JSON value is a bool, returns the bool value. */
    fun asBool(): Boolean? = innerRef.asBool()

    /**
     * If the `Schema`'s underlying JSON value is an object, borrows the object as a `Map` of
     * properties.
     */
    internal fun asObject(): MutableMap<String, Value>? = innerRef.asObject()

    /**
     * If the `Schema`'s underlying JSON value is an object, mutably borrows the object as a `Map`
     * of properties.
     */
    internal fun asObjectMut(): MutableMap<String, Value>? = innerRef.asObject()

    internal fun tryToObject(): TryToObjectResult = when (val v = innerRef) {
        is Value.Object -> TryToObjectResult.Ok(v.entries)
        is Value.Bool -> TryToObjectResult.Err(v.value)
        else -> error("Schema inner value should always be Object or Bool")
    }

    internal fun tryAsObjectMut(): TryToObjectResult = when (val v = innerRef) {
        is Value.Object -> TryToObjectResult.Ok(v.entries)
        is Value.Bool -> TryToObjectResult.Err(v.value)
        else -> error("Schema inner value should always be Object or Bool")
    }

    /** Returns the `Schema`'s underlying JSON value. */
    internal fun toValue(): Value = innerRef

    /**
     * Converts the `Schema` (if it wraps a bool value) into an equivalent object schema, then
     * returns its mutable map.
     *
     * `true` is transformed into an empty schema `{}`, which successfully validates against all
     * possible values. `false` is transformed into the schema `{"not": {}}`, which does not
     * successfully validate against any value.
     */
    internal fun ensureObject(): MutableMap<String, Value> {
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
    internal fun insert(k: String, v: Value): Value? {
        val obj = ensureObject()
        return obj.put(k, v)
    }

    /**
     * If the `Schema`'s underlying JSON value is an object, gets a reference to that object's
     * value for the given key if it exists.
     *
     * This always returns `null` for bool schemas.
     */
    internal fun get(key: String): Value? = innerRef.asObject()?.get(key)

    /**
     * If the `Schema`'s underlying JSON value is an object, gets a mutable reference to that
     * object's value for the given key if it exists.
     *
     * This always returns `null` for bool schemas.
     */
    internal fun getMut(key: String): Value? = innerRef.asObject()?.get(key)

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
    internal fun pointer(pointer: String): Value? {
        return if (pointer.startsWith("#")) {
            val decoded = percentDecode(pointer.substring(1)) ?: return null
            innerRef.pointer(decoded)
        } else {
            innerRef.pointer(pointer)
        }
    }

    /**
     * If the `Schema`'s underlying JSON value is an object, looks up a value by a JSON Pointer
     * and returns a mutable reference to that value.
     *
     * If the given pointer begins with a `#`, then the rest of the value is assumed to be in
     * "URI Fragment Identifier Representation", and will be percent-decoded accordingly.
     *
     * For more information on JSON Pointer, read [RFC6901](https://tools.ietf.org/html/rfc6901).
     *
     * This always returns `null` for bool schemas.
     */
    internal fun pointerMut(pointer: String): Value? = pointer(pointer)

    /**
     * If the `Schema`'s underlying JSON value is an object, removes and returns its value for the
     * given key. Always returns `null` for bool schemas, without modifying them.
     */
    internal fun remove(key: String): Value? = innerRef.asObject()?.remove(key)

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

    /**
     * Mirrors the upstream Rust `Serialize` impl on [Schema]: returns a re-ordered [Value]
     * tree in which JSON-Schema-significant keys appear in the conventional order
     * `[$id, $schema, title, description, type, format, properties]` first, all custom keys
     * next, and `[$defs, definitions]` last. Nested subschemas are re-ordered recursively
     * via [OrderedKeywordWrapper]. Upstream feeds this re-ordering into a `serde::Serializer`
     * to produce a JSON string; the Kotlin port leaves the string-encoding step to whatever
     * downstream JSON writer the consumer already uses against the [Value] tree.
     */
    internal fun serialize(): Value = OrderedKeywordWrapper.from(innerRef).serialize()

    /**
     * Mirrors the upstream `impl JsonSchema for Schema`. Rust's trait functions are type-level
     * (`Schema::schema_name()`); the Kotlin [JsonSchema] interface is instance-based, so the
     * port answers the same questions from any [Schema] instance. The schema produced by
     * [jsonSchema] describes the JSON-Schema-of-a-JSON-Schema: a value that is either an
     * object or a boolean.
     */
    override fun schemaName(): String = "Schema"

    override fun schemaId(): String = "schemars::Schema"

    override fun jsonSchema(generator: SchemaGenerator): Schema = jsonSchema {
        this["type"] = listOf("object", "boolean")
    }
}

/**
 * The order of properties in a JSON Schema object is insignificant, but a small set of keys
 * are explicitly ordered to make schemas easier for a human to read. All other properties are
 * ordered either lexicographically (by default) or by insertion order (if `preserve_order` is
 * enabled, which on the Kotlin port is always the case because [Value.Object] preserves
 * insertion order via [linkedMapOf]).
 *
 * [noReorder] is `true` when the wrapped value is expected to be an object that is NOT a
 * schema but whose property values *are* expected to be schemas — `properties`,
 * `patternProperties`, `dependentSchemas`, `$defs`, `definitions`. In that case the direct
 * properties keep their insertion order, but the nested subschemas are still re-ordered.
 *
 * When [noReorder] is `false`, the wrapped value is expected to be one of:
 * - a JSON schema object
 * - an array of JSON schemas
 * - a JSON primitive value (null / string / number / bool)
 *
 * If any of these expectations are not met, the value is still serialized in a valid way,
 * but the property ordering may be unclear.
 */
internal class OrderedKeywordWrapper private constructor(
    private val value: Value,
    private val noReorder: Boolean,
) {
    companion object {
        internal val ORDERED_KEYWORDS_START: List<String> = listOf(
            "\$id",
            "\$schema",
            "title",
            "description",
            "type",
            "format",
            "properties",
        )
        internal val ORDERED_KEYWORDS_END: List<String> = listOf(
            "\$defs",
            "definitions",
        )

        internal fun from(value: Value): OrderedKeywordWrapper =
            OrderedKeywordWrapper(value = value, noReorder = false)
    }

    internal fun serialize(): Value = when (val v = value) {
        is Value.Array -> Value.Array(
            v.items.mapTo(mutableListOf()) { OrderedKeywordWrapper.from(it).serialize() },
        )
        is Value.Object -> if (noReorder) {
            // Upstream: `Value::Object(object) if self.no_reorder`. Direct properties keep
            // insertion order; nested subschema values still get re-ordered via the default
            // (non-no-reorder) wrapper.
            val out = linkedMapOf<String, Value>()
            for ((key, sub) in v.entries) {
                out[key] = OrderedKeywordWrapper.from(sub).serialize()
            }
            Value.Object(out)
        } else {
            // Upstream: `Value::Object(object)` (the default branch). Emit
            // [ORDERED_KEYWORDS_START] first, then every other key, then
            // [ORDERED_KEYWORDS_END] — all through [serializeSchemaProperty] so the
            // examples / default / `x-` prefix exceptions are preserved.
            val out = linkedMapOf<String, Value>()
            for (key in ORDERED_KEYWORDS_START) {
                v.entries[key]?.let { sub -> serializeSchemaProperty(out, key, sub) }
            }
            for ((key, sub) in v.entries) {
                if (key !in ORDERED_KEYWORDS_START && key !in ORDERED_KEYWORDS_END) {
                    serializeSchemaProperty(out, key, sub)
                }
            }
            for (key in ORDERED_KEYWORDS_END) {
                v.entries[key]?.let { sub -> serializeSchemaProperty(out, key, sub) }
            }
            Value.Object(out)
        }
        // Upstream: `Value::Null | Value::Bool(_) | Value::Number(_) | Value::String(_) =>
        // self.value.serialize(serializer)` — every primitive flows through unchanged.
        is Value.Null, is Value.Bool, is Value.Number, is Value.Str -> v
    }

    /**
     * Mirrors the upstream nested helper `serialize_schema_property`. Keys whose values are
     * not themselves schemas (`examples`, `default`, every `x-`-prefixed custom key) pass
     * through unchanged. Keys whose values are objects-of-schemas (`properties`,
     * `patternProperties`, `dependentSchemas`, `$defs`, `definitions`) wrap their value in
     * a [noReorder]-`true` [OrderedKeywordWrapper] so the immediate map's keys keep their
     * insertion order but the nested subschemas are still re-ordered. Every other key is
     * treated as a schema value and re-ordered.
     */
    private fun serializeSchemaProperty(
        out: MutableMap<String, Value>,
        key: String,
        value: Value,
    ) {
        if (key == "examples" || key == "default" || key.startsWith("x-")) {
            // Value(s) of `examples`/`default` are plain values, not schemas. Also don't
            // re-order values of custom properties.
            out[key] = value
        } else {
            val nestedNoReorder = key == "properties" ||
                key == "patternProperties" ||
                key == "dependentSchemas" ||
                key == "\$defs" ||
                key == "definitions"
            out[key] = OrderedKeywordWrapper(value, nestedNoReorder).serialize()
        }
    }
}

/**
 * A schema-conversion error payload raised when [Schema.tryFrom] receives a value that is neither an
 * object nor a bool. This is separated from the Exception to avoid Swift Export Unchecked Cast bugs.
 */
class SchemaConversionError internal constructor(val message: String) {
    override fun toString(): String = "SchemaConversionError($message)"
}

/** The internal exception thrown when getOrThrow() fails. */
internal class SchemaConversionException(val error: SchemaConversionError) : RuntimeException(error.message)

/** A custom Result type for [Schema.tryFrom] to avoid exporting Kotlin's built-in Result to Swift */
class SchemaResult internal constructor(
    val value: Schema?,
    val error: SchemaConversionError?
) {
    init {
        require((value == null) != (error == null)) {
            "SchemaResult must carry exactly one of value or error (got value=$value, error=$error)"
        }
    }

    companion object {
        internal fun ok(value: Schema) = SchemaResult(value, null)
        internal fun err(error: SchemaConversionError) = SchemaResult(null, error)
    }

    fun isSuccess(): Boolean = value != null
    fun isFailure(): Boolean = error != null

    fun getOrThrow(): Schema = when {
        value != null -> value
        error != null -> throw SchemaConversionException(error)
        else -> error("SchemaResult class invariant violated: both value and error are null")
    }

    fun getOrNull(): Schema? = value
    fun exceptionOrNull(): SchemaConversionError? = error
}

/** Result of [Schema.tryToObject] / [Schema.tryAsObjectMut]. */
internal sealed class TryToObjectResult {
    data class Ok(val entries: MutableMap<String, Value>) : TryToObjectResult()
    data class Err(val bool: Boolean) : TryToObjectResult()
}

/** Convert a bool to a `Schema` using `Schema.from(this)`. */
fun Boolean.toSchema(): Schema = Schema.from(this)

/** Convert an object map to a `Schema` using `Schema.from(this)`. */
internal fun MutableMap<String, Value>.toSchema(): Schema = Schema.from(this)

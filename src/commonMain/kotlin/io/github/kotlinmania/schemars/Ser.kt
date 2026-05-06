// port-lint: source ser.rs
package io.github.kotlinmania.schemars

import io.github.kotlinmania.schemars.jsonschemaimpls.BoolSchema
import io.github.kotlinmania.schemars.jsonschemaimpls.CharSchema
import io.github.kotlinmania.schemars.jsonschemaimpls.SeqSchema
import io.github.kotlinmania.schemars.jsonschemaimpls.StringSchema
import io.github.kotlinmania.schemars.jsonschemaimpls.U8Schema
import io.github.kotlinmania.schemars.jsonschemaimpls.UnitSchema
import io.github.kotlinmania.schemars.jsonschemaimpls.ValueSchema
import io.github.kotlinmania.schemars.private.allowNull

/**
 * Schema-emitting Serializer.
 *
 * Upstream this implements `serde::Serializer` so any `T: Serialize` can be turned into a
 * [Schema] describing its wire shape. Kotlin doesn't ship a `Serialize` trait, so callers
 * dispatch to the matching `serialize*` method explicitly. Users wiring `kotlinx.serialization`
 * call into the same surface from the encoder side.
 */
class Serializer(
    internal val generator: SchemaGenerator,
    internal val includeTitle: Boolean,
) {
    /** `forward_to_subschema_for!(serialize_bool, bool)`. */
    fun serializeBool(@Suppress("UNUSED_PARAMETER") value: Boolean): Schema =
        generator.subschemaFor(BoolSchema)

    /** `forward_to_subschema_for!(serialize_char, char)`. */
    fun serializeChar(@Suppress("UNUSED_PARAMETER") value: Char): Schema =
        generator.subschemaFor(CharSchema)

    /** `forward_to_subschema_for!(serialize_str, &str)`. */
    fun serializeStr(@Suppress("UNUSED_PARAMETER") value: String): Schema =
        generator.subschemaFor(StringSchema)

    /** `forward_to_subschema_for!(serialize_bytes, &[u8])`. */
    fun serializeBytes(@Suppress("UNUSED_PARAMETER") value: ByteArray): Schema =
        generator.subschemaFor(SeqSchema(U8Schema))

    private fun integer(): Schema = jsonSchema { this["type"] = "integer" }
    private fun number(): Schema = jsonSchema { this["type"] = "number" }

    fun serializeI8(@Suppress("UNUSED_PARAMETER") value: Byte): Schema = integer()
    fun serializeI16(@Suppress("UNUSED_PARAMETER") value: Short): Schema = integer()
    fun serializeI32(@Suppress("UNUSED_PARAMETER") value: Int): Schema = integer()
    fun serializeI64(@Suppress("UNUSED_PARAMETER") value: Long): Schema = integer()
    fun serializeI128(@Suppress("UNUSED_PARAMETER") value: Long): Schema = integer()
    fun serializeU8(@Suppress("UNUSED_PARAMETER") value: UByte): Schema = integer()
    fun serializeU16(@Suppress("UNUSED_PARAMETER") value: UShort): Schema = integer()
    fun serializeU32(@Suppress("UNUSED_PARAMETER") value: UInt): Schema = integer()
    fun serializeU64(@Suppress("UNUSED_PARAMETER") value: ULong): Schema = integer()
    fun serializeU128(@Suppress("UNUSED_PARAMETER") value: ULong): Schema = integer()
    fun serializeF32(@Suppress("UNUSED_PARAMETER") value: Float): Schema = number()
    fun serializeF64(@Suppress("UNUSED_PARAMETER") value: Double): Schema = number()

    /** `collect_str` upstream. */
    fun collectStr(@Suppress("UNUSED_PARAMETER") value: Any?): Schema =
        generator.subschemaFor(StringSchema)

    /** `collect_map` upstream — produces `{ type: object, additionalProperties: ... }`. */
    fun collectMap(iter: Iterable<Pair<Any?, (Serializer) -> Schema>>): Schema {
        var acc: Schema? = null
        for ((_, v) in iter) {
            if (acc == jsonSchema(true)) break
            val schema = v(Serializer(generator, includeTitle = false))
            acc = when {
                acc == null -> schema
                acc != schema -> jsonSchema(true)
                else -> acc
            }
        }
        val valueSchema = acc ?: jsonSchema(true)
        return jsonSchema {
            this["type"] = "object"
            this["additionalProperties"] = valueSchema
        }
    }

    /** `serialize_none` — schemars uses the `Value` schema (matches anything). */
    fun serializeNone(): Schema = generator.subschemaFor(ValueSchema)

    /** `serialize_unit` — `serde`'s unit forwards to `serialize_none`. */
    fun serializeUnit(): Schema = serializeNone()

    /** `serialize_some(value)` — runs [body] to produce the inner schema then allows null. */
    fun serializeSome(body: (Serializer) -> Schema): Schema {
        val schema = body(Serializer(generator, includeTitle = false))
        allowNull(generator, schema)
        return schema
    }

    fun serializeUnitStruct(@Suppress("UNUSED_PARAMETER") name: String): Schema =
        generator.subschemaFor(UnitSchema)

    fun serializeUnitVariant(
        @Suppress("UNUSED_PARAMETER") name: String,
        @Suppress("UNUSED_PARAMETER") variantIndex: UInt,
        @Suppress("UNUSED_PARAMETER") variant: String,
    ): Schema = jsonSchema(true)

    fun serializeNewtypeStruct(name: String, body: (Serializer) -> Schema): Schema {
        val schema = body(this)
        if (includeTitle && name.isNotEmpty()) {
            schema.insert("title", Value.Str(name))
        }
        return schema
    }

    fun serializeNewtypeVariant(
        @Suppress("UNUSED_PARAMETER") name: String,
        @Suppress("UNUSED_PARAMETER") variantIndex: UInt,
        @Suppress("UNUSED_PARAMETER") variant: String,
        @Suppress("UNUSED_PARAMETER") body: (Serializer) -> Schema,
    ): Schema = jsonSchema(true)

    fun serializeSeq(@Suppress("UNUSED_PARAMETER") len: Int? = null): SerializeSeq =
        SerializeSeq(generator)

    fun serializeTuple(len: Int): SerializeTuple =
        SerializeTuple(generator, ArrayList(len), title = "")

    fun serializeTupleStruct(name: String, len: Int): SerializeTuple {
        val title = if (includeTitle) name else ""
        return SerializeTuple(generator, ArrayList(len), title)
    }

    /** Tuple-variant schemas always collapse to `true` (any value matches). */
    fun serializeTupleVariant(
        @Suppress("UNUSED_PARAMETER") name: String,
        @Suppress("UNUSED_PARAMETER") variantIndex: UInt,
        @Suppress("UNUSED_PARAMETER") variant: String,
        @Suppress("UNUSED_PARAMETER") len: Int,
    ): SerializeVariant = SerializeVariant()

    fun serializeMap(@Suppress("UNUSED_PARAMETER") len: Int? = null): SerializeMap =
        SerializeMap(generator, linkedMapOf(), currentKey = null, title = "")

    fun serializeStruct(name: String, @Suppress("UNUSED_PARAMETER") len: Int): SerializeMap {
        val title = if (includeTitle) name else ""
        return SerializeMap(generator, linkedMapOf(), currentKey = null, title = title)
    }

    /** Struct-variant schemas always collapse to `true` (any value matches). */
    fun serializeStructVariant(
        @Suppress("UNUSED_PARAMETER") name: String,
        @Suppress("UNUSED_PARAMETER") variantIndex: UInt,
        @Suppress("UNUSED_PARAMETER") variant: String,
        @Suppress("UNUSED_PARAMETER") len: Int,
    ): SerializeVariant = SerializeVariant()
}

/** `impl SerializeSeq for SerializeSeq` — accumulates element schemas. */
class SerializeSeq internal constructor(
    private val generator: SchemaGenerator,
    private val items: MutableList<Schema> = mutableListOf(),
) {
    fun serializeElement(body: (Serializer) -> Schema) {
        if (items.firstOrNull() == jsonSchema(true)) {
            // Schema already allows any value, so no point in extending it
            return
        }
        val schema = body(Serializer(generator, includeTitle = false))
        if (schema == jsonSchema(true)) {
            items.clear()
            items.add(schema)
        } else if (!items.contains(schema)) {
            items.add(schema)
        }
    }

    fun endSchema(): Schema {
        val itemsSchema: Any = when (items.size) {
            0 -> true
            1 -> items[0]
            else -> mapOf("anyOf" to items)
        }
        return jsonSchema {
            this["type"] = "array"
            this["items"] = itemsSchema
        }
    }
}

/** `impl SerializeTuple for SerializeTuple` (and `SerializeTupleStruct`). */
class SerializeTuple internal constructor(
    private val generator: SchemaGenerator,
    private val items: MutableList<Schema>,
    private val title: String,
) {
    fun serializeElement(body: (Serializer) -> Schema) {
        items.add(body(Serializer(generator, includeTitle = false)))
    }

    /** Alias used by `SerializeTupleStruct`. */
    fun serializeField(body: (Serializer) -> Schema): Unit = serializeElement(body)

    fun endSchema(): Schema {
        val len = items.size
        val schema = jsonSchema {
            this["type"] = "array"
            this["prefixItems"] = items.toList()
            this["maxItems"] = len
            this["minItems"] = len
        }

        if (title.isNotEmpty()) {
            schema.ensureObject()["title"] = Value.Str(title)
        }
        return schema
    }
}

/** `impl SerializeMap for SerializeMap` (and `SerializeStruct`). */
class SerializeMap internal constructor(
    private val generator: SchemaGenerator,
    private val properties: MutableMap<String, Value>,
    private var currentKey: String? = null,
    private val title: String,
) {
    fun serializeKey(body: (Serializer) -> Schema) {
        // FIXME this is too lenient - we should return an error if serde_json doesn't allow
        // T to be a key of a map. We approximate by stringifying the schema's underlying value.
        val schema = body(Serializer(generator, includeTitle = false))
        val asValue = schema.asValue()
        currentKey = (asValue as? Value.Str)?.value ?: asValue.toString().trim('"')
    }

    fun serializeValue(body: (Serializer) -> Schema) {
        val key = currentKey ?: ""
        currentKey = null
        val schema = body(Serializer(generator, includeTitle = false))
        properties[key] = schema.asValue()
    }

    /** Struct field — sets key and value in one call. */
    fun serializeField(key: String, body: (Serializer) -> Schema) {
        val schema = body(Serializer(generator, includeTitle = false))
        properties[key] = schema.asValue()
    }

    fun endSchema(): Schema {
        val schema = jsonSchema {
            this["type"] = "object"
            this["properties"] = Value.Object(properties)
        }
        if (title.isNotEmpty()) {
            schema.ensureObject()["title"] = Value.Str(title)
        }
        return schema
    }
}

/** Tuple/Struct-variant collector — schemars emits `true` regardless of fields. */
class SerializeVariant internal constructor() {
    fun serializeField(@Suppress("UNUSED_PARAMETER") body: (Serializer) -> Schema) { /* no-op */ }
    fun serializeField(
        @Suppress("UNUSED_PARAMETER") key: String,
        @Suppress("UNUSED_PARAMETER") body: (Serializer) -> Schema,
    ) { /* no-op */ }

    fun endSchema(): Schema = jsonSchema(true)
}

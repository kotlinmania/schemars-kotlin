// port-lint: source ser.rs
package io.github.kotlinmania.schemars

import io.github.kotlinmania.schemars.generate.SchemaGenerator
import io.github.kotlinmania.schemars.jsonschemaimpls.BoolSchema
import io.github.kotlinmania.schemars.jsonschemaimpls.CharSchema
import io.github.kotlinmania.schemars.jsonschemaimpls.SeqSchema
import io.github.kotlinmania.schemars.jsonschemaimpls.StringSchema
import io.github.kotlinmania.schemars.jsonschemaimpls.U8Schema
import io.github.kotlinmania.schemars.jsonschemaimpls.UnitSchema
import io.github.kotlinmania.schemars.jsonschemaimpls.ValueSchema
import io.github.kotlinmania.schemars.private.allowNull

internal class Serializer(
    internal val generator: SchemaGenerator,
    internal val includeTitle: Boolean,
)

internal fun Serializer.serializeI8(): Schema = jsonSchema { this["type"] = "integer" }

internal fun Serializer.serializeI16(): Schema = jsonSchema { this["type"] = "integer" }

internal fun Serializer.serializeI32(): Schema = jsonSchema { this["type"] = "integer" }

internal fun Serializer.serializeI64(): Schema = jsonSchema { this["type"] = "integer" }

internal fun Serializer.serializeI128(): Schema = jsonSchema { this["type"] = "integer" }

internal fun Serializer.serializeU8(): Schema = jsonSchema { this["type"] = "integer" }

internal fun Serializer.serializeU16(): Schema = jsonSchema { this["type"] = "integer" }

internal fun Serializer.serializeU32(): Schema = jsonSchema { this["type"] = "integer" }

internal fun Serializer.serializeU64(): Schema = jsonSchema { this["type"] = "integer" }

internal fun Serializer.serializeU128(): Schema = jsonSchema { this["type"] = "integer" }

internal fun Serializer.serializeF32(): Schema = jsonSchema { this["type"] = "number" }

internal fun Serializer.serializeF64(): Schema = jsonSchema { this["type"] = "number" }

internal fun Serializer.serializeBool(): Schema = generator.subschemaFor(BoolSchema)

internal fun Serializer.serializeChar(): Schema = generator.subschemaFor(CharSchema)

internal fun Serializer.serializeStr(): Schema = generator.subschemaFor(StringSchema)

internal fun Serializer.serializeBytes(): Schema = generator.subschemaFor(SeqSchema(U8Schema))

internal fun Serializer.collectStr(): Schema = generator.subschemaFor(StringSchema)

internal fun Serializer.collectMap(iter: Iterable<Pair<Any?, (Serializer) -> Schema>>): Schema {
    var acc: Schema? = null
    for ((_, v) in iter) {
        if (acc == jsonSchema(true)) break
        val schema = v(Serializer(generator, includeTitle = false))
        acc =
            when {
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

internal fun Serializer.serializeNone(): Schema = generator.subschemaFor(ValueSchema)

internal fun Serializer.serializeUnit(): Schema = serializeNone()

internal fun Serializer.serializeSome(body: (Serializer) -> Schema): Schema {
    val schema = body(Serializer(generator, includeTitle = false))
    allowNull(generator, schema)
    return schema
}

internal fun Serializer.serializeUnitStruct(): Schema = generator.subschemaFor(UnitSchema)

internal fun Serializer.serializeUnitVariant(): Schema = jsonSchema(true)

internal fun Serializer.serializeNewtypeStruct(
    name: String,
    body: (Serializer) -> Schema,
): Schema {
    val schema = body(this)
    if (includeTitle && name.isNotEmpty()) {
        schema.insert("title", Value.Str(name))
    }
    return schema
}

internal fun Serializer.serializeNewtypeVariant(): Schema = jsonSchema(true)

internal fun Serializer.serializeSeq(): SerializeSeq = SerializeSeq(generator)

internal fun Serializer.serializeTuple(len: Int): SerializeTuple = SerializeTuple(generator, ArrayList(len), title = "")

internal fun Serializer.serializeTupleStruct(
    name: String,
    len: Int,
): SerializeTuple {
    val title = if (includeTitle) name else ""
    return SerializeTuple(generator, ArrayList(len), title)
}

internal fun Serializer.serializeTupleVariant(): SerializeVariant = SerializeVariant()

internal fun Serializer.serializeMap(): SerializeMap = SerializeMap(generator, linkedMapOf(), currentKey = null, title = "")

internal fun Serializer.serializeStruct(name: String): SerializeMap {
    val title = if (includeTitle) name else ""
    return SerializeMap(generator, linkedMapOf(), currentKey = null, title = title)
}

internal fun Serializer.serializeStructVariant(): SerializeVariant = SerializeVariant()

internal class SerializeSeq internal constructor(
    private val generator: SchemaGenerator,
    private val items: MutableList<Schema> = mutableListOf(),
) {
    fun serializeElement(body: (Serializer) -> Schema) {
        if (items.firstOrNull() == jsonSchema(true)) {
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
        val itemsSchema: Any =
            when (items.size) {
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

internal class SerializeTuple internal constructor(
    private val generator: SchemaGenerator,
    private val items: MutableList<Schema>,
    private val title: String,
) {
    fun serializeElement(body: (Serializer) -> Schema) {
        items.add(body(Serializer(generator, includeTitle = false)))
    }

    fun serializeField(body: (Serializer) -> Schema): Unit = serializeElement(body)

    fun endSchema(): Schema {
        val len = items.size
        val schema =
            jsonSchema {
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

internal class SerializeMap internal constructor(
    private val generator: SchemaGenerator,
    private val properties: MutableMap<String, Value>,
    private var currentKey: String? = null,
    private val title: String,
) {
    fun serializeKey(body: (Serializer) -> Schema) {
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

    fun serializeField(
        key: String,
        body: (Serializer) -> Schema,
    ) {
        val schema = body(Serializer(generator, includeTitle = false))
        properties[key] = schema.asValue()
    }

    fun endSchema(): Schema {
        val schema =
            jsonSchema {
                this["type"] = "object"
                this["properties"] = Value.Object(properties)
            }
        if (title.isNotEmpty()) {
            schema.ensureObject()["title"] = Value.Str(title)
        }
        return schema
    }
}

internal class SerializeVariant internal constructor() {
    fun serializeField(body: (Serializer) -> Schema) {
        body(Serializer(SerializeVariantSink, includeTitle = false))
    }

    fun serializeField(
        key: String,
        body: (Serializer) -> Schema,
    ) {
        require(key.isNotEmpty()) { "struct-variant field name must be non-empty" }
        body(Serializer(SerializeVariantSink, includeTitle = false))
    }

    fun endSchema(): Schema = jsonSchema(true)

    companion object {
        // regardless of variant content). The body callbacks still get invoked so that callers
        // exercising a recursive serialization tree run their side-effects; the produced schema
        // is discarded by routing it through a sink that holds the original generator unchanged.
        private val SerializeVariantSink: SchemaGenerator
            get() =
                io.github.kotlinmania.schemars.generate.SchemaSettings
                    .default()
                    .intoGenerator()
    }
}

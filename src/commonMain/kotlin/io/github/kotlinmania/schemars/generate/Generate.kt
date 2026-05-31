// port-lint: source generate.rs
package io.github.kotlinmania.schemars.generate

import io.github.kotlinmania.schemars.JsonSchema
import io.github.kotlinmania.schemars.MetaSchemas
import io.github.kotlinmania.schemars.Schema
import io.github.kotlinmania.schemars.Value
import io.github.kotlinmania.schemars.encodeRefName
import io.github.kotlinmania.schemars.transform.AddNullable
import io.github.kotlinmania.schemars.transform.RemoveRefSiblings
import io.github.kotlinmania.schemars.transform.ReplaceBoolSchemas
import io.github.kotlinmania.schemars.transform.ReplaceConstValue
import io.github.kotlinmania.schemars.transform.ReplacePrefixItems
import io.github.kotlinmania.schemars.transform.ReplaceUnevaluatedProperties
import io.github.kotlinmania.schemars.transform.SetSingleExample
import io.github.kotlinmania.schemars.transform.Transform

/*
JSON Schema generator and settings.

This module is useful if you want more control over how the schema is generated than the
[schemaFor] function gives you.
There are two main types in this module:
* [SchemaSettings], which defines what JSON Schema features should be used when generating schemas
  (for example, how `Option`s should be represented).
* [SchemaGenerator], which manages the generation of a schema document.
*/

/**
 * Settings to customize how Schemas are generated.
 *
 * The default settings currently conform to JSON Schema 2020-12, but this is liable to change in a
 * future version of Schemars if support for other JSON Schema versions is added.
 * If you rely on generated schemas conforming to draft 2020-12, consider using the
 * [SchemaSettings.draft202012] method.
 */
data class SchemaSettings(
    /**
     * A JSON pointer to the expected location of referenceable subschemas within the resulting
     * root schema.
     *
     * A single leading `#` and/or single trailing `/` are ignored.
     *
     * Defaults to `"/${'$'}defs"`.
     */
    var definitionsPath: String,
    /**
     * The URI of the meta-schema describing the structure of the generated schemas.
     *
     * Defaults to [MetaSchemas.DRAFT2020_12] (`https://json-schema.org/draft/2020-12/schema`).
     */
    var metaSchema: String?,
    /**
     * A list of [Transform]s that get applied to generated root schemas.
     *
     * Defaults to an empty list (no transforms).
     */
    var transforms: List<Transform>,
    /**
     * Inline all subschemas instead of using references.
     *
     * Some references may still be generated in schemas for recursive types.
     *
     * Defaults to `false`.
     */
    var inlineSubschemas: Boolean,
    /**
     * Whether the generated schemas should describe how types are serialized or *de*serialized.
     *
     * Defaults to [Contract.Deserialize].
     */
    var contract: Contract,
    /**
     * Whether to include enum variant names in their schema's `title` when using the untagged
     * enum representation.
     *
     * Defaults to `false`.
     */
    var untaggedEnumVariantTitles: Boolean,
) {
    companion object {
        /**
         * The default settings currently conform to JSON Schema 2020-12, but this is liable to
         * change in a future version of Schemars if support for other JSON Schema versions is
         * added. If you rely on generated schemas conforming to draft 2020-12, consider using
         * [draft202012] instead.
         */
        fun default(): SchemaSettings = draft202012()

        /** Creates `SchemaSettings` that conform to JSON Schema Draft 7. */
        fun draft07(): SchemaSettings =
            SchemaSettings(
                definitionsPath = "/definitions",
                metaSchema = MetaSchemas.DRAFT07,
                transforms =
                    listOf(
                        ReplaceUnevaluatedProperties(),
                        RemoveRefSiblings(),
                        ReplacePrefixItems(),
                    ),
                inlineSubschemas = false,
                contract = Contract.Deserialize,
                untaggedEnumVariantTitles = false,
            )

        /** Creates `SchemaSettings` that conform to JSON Schema 2019-09. */
        fun draft201909(): SchemaSettings =
            SchemaSettings(
                definitionsPath = "/\$defs",
                metaSchema = MetaSchemas.DRAFT2019_09,
                transforms = mutableListOf(ReplacePrefixItems()),
                inlineSubschemas = false,
                contract = Contract.Deserialize,
                untaggedEnumVariantTitles = false,
            )

        /** Creates `SchemaSettings` that conform to JSON Schema 2020-12. */
        fun draft202012(): SchemaSettings =
            SchemaSettings(
                definitionsPath = "/\$defs",
                metaSchema = MetaSchemas.DRAFT2020_12,
                transforms = listOf(),
                inlineSubschemas = false,
                contract = Contract.Deserialize,
                untaggedEnumVariantTitles = false,
            )

        /** Creates `SchemaSettings` that conform to OpenAPI 3.0. */
        fun openapi3(): SchemaSettings =
            SchemaSettings(
                definitionsPath = "/components/schemas",
                metaSchema = MetaSchemas.OPENAPI3,
                transforms =
                    listOf(
                        ReplaceUnevaluatedProperties(),
                        ReplaceBoolSchemas(skipAdditionalProperties = true),
                        AddNullable(),
                        RemoveRefSiblings(),
                        SetSingleExample(),
                        ReplaceConstValue(),
                        ReplacePrefixItems(),
                    ),
                inlineSubschemas = false,
                contract = Contract.Deserialize,
                untaggedEnumVariantTitles = false,
            )
    }

    /** Modifies the `SchemaSettings` by calling the given function. */
    internal fun with(configureFn: (SchemaSettings) -> Unit): SchemaSettings {
        configureFn(this)
        return this
    }

    /**
     * Appends the given transform to the list of [transforms] for these `SchemaSettings`.
     */
    fun withTransform(transform: Transform): SchemaSettings {
        val newTransforms = transforms.toMutableList()
        newTransforms.add(transform)
        return copy(transforms = newTransforms)
    }

    /** Creates a new [SchemaGenerator] using these settings. */
    fun intoGenerator(): SchemaGenerator = SchemaGenerator(this)

    /** Updates the settings to generate schemas describing how types are **deserialized**. */
    fun forDeserialize(): SchemaSettings {
        contract = Contract.Deserialize
        return this
    }

    /** Updates the settings to generate schemas describing how types are **serialized**. */
    fun forSerialize(): SchemaSettings {
        contract = Contract.Serialize
        return this
    }
}

/**
 * A setting to specify whether generated schemas should describe how types are serialized or
 * *de*serialized.
 */
enum class Contract {
    Deserialize,
    Serialize,
    ;

    /** Returns true if `this` is the [Deserialize] contract. */
    fun isDeserialize(): Boolean = this == Deserialize

    /** Returns true if `this` is the [Serialize] contract. */
    fun isSerialize(): Boolean = this == Serialize
}

internal data class SchemaUid(
    val name: String,
    val contract: Contract,
) : Comparable<SchemaUid> {
    override fun compareTo(other: SchemaUid): Int {
        val n = name.compareTo(other.name)
        return if (n != 0) n else contract.compareTo(other.contract)
    }
}

/**
 * The main type used to generate JSON Schemas.
 */
class SchemaGenerator internal constructor(
    private var settings: SchemaSettings,
) {
    private val definitions: MutableMap<String, Value> = linkedMapOf()
    private val pendingSchemaIds: MutableSet<SchemaUid> = sortedSetOfSchemaUids()
    private val schemaIdToName: MutableMap<SchemaUid, String> = sortedMapOfSchemaUids()
    private val usedSchemaNames: MutableSet<String> = linkedSetOf()

    // It's unlikely that `rootSchemaIdStack` will ever contain more than one item, but it is
    // possible, e.g. if a `jsonSchema()` implementation calls `generator.rootSchemaFor<...>()`
    private val rootSchemaIdStack: MutableList<SchemaUid> = mutableListOf()

    companion object {
        /** Creates a `SchemaGenerator` using the default [SchemaSettings]. */
        fun default(): SchemaGenerator = SchemaSettings.default().intoGenerator()
    }

    /** Borrows the [SchemaSettings] being used by this `SchemaGenerator`. */
    fun settings(): SchemaSettings = settings

    /**
     * Generates a JSON Schema for the given [JsonSchema] type, returning either the schema itself
     * or a `${'$'}ref` schema referencing the type's schema.
     */
    fun subschemaFor(type: JsonSchema): Schema {
        data class FindRef(
            val schema: Schema,
            val nameToBeInserted: String?,
        )

        fun findRef(
            uid: SchemaUid,
            inlineSchema: Boolean,
            schemaName: () -> String,
        ): FindRef? {
            val returnRef =
                !inlineSchema &&
                    (!settings.inlineSubschemas || pendingSchemaIds.contains(uid))

            if (!returnRef) return null

            if (rootSchemaIdStack.lastOrNull() == uid) {
                return FindRef(Schema.newRef("#"), null)
            }

            val existing = schemaIdToName[uid]
            val name: String =
                existing ?: run {
                    val baseName = schemaName()
                    var n = ""
                    if (usedSchemaNames.contains(baseName)) {
                        var i = 2
                        while (true) {
                            n = "$baseName$i"
                            if (!usedSchemaNames.contains(n)) break
                            i++
                        }
                    } else {
                        n = baseName
                    }
                    usedSchemaNames.add(n)
                    schemaIdToName[uid] = n
                    n
                }

            val reference = "#${definitionsPathStripped()}/${encodeRefName(name)}"
            val nameToInsert = if (!definitions.containsKey(name)) name else null
            return FindRef(Schema.newRef(reference), nameToInsert)
        }

        val uid = schemaUid(type)
        val ref = findRef(uid, type.inlineSchema(), type::schemaName) ?: return jsonSchemaInternal(type, uid)
        if (ref.nameToBeInserted != null) {
            insertNewSubschemaFor(type, ref.nameToBeInserted, uid)
        }
        return ref.schema
    }

    private fun insertNewSubschemaFor(
        type: JsonSchema,
        name: String,
        uid: SchemaUid,
    ) {
        val dummy = Value.Bool(false)
        // insert into definitions BEFORE calling jsonSchema to avoid infinite recursion
        definitions[name] = dummy

        val schema = jsonSchemaInternal(type, uid)

        definitions[name] = schema.toValue()
    }

    /**
     * Borrows the collection of all non-inlined schemas that have been generated. The keys are the
     * schema names, and the values are the schemas themselves.
     */
    internal fun definitions(): MutableMap<String, Value> = definitions

    /** Mutable variant of [definitions]; same return value. */
    internal fun definitionsMut(): MutableMap<String, Value> = definitions

    /**
     * Returns the collection of all non-inlined schemas that have been generated, leaving an empty
     * map in its place.
     *
     * To apply this generator's transforms to each of the returned schemas, set [applyTransforms]
     * to `true`.
     */
    internal fun takeDefinitions(applyTransforms: Boolean): MutableMap<String, Value> {
        val taken: MutableMap<String, Value> = linkedMapOf()
        taken.putAll(definitions)
        definitions.clear()

        if (applyTransforms) {
            val keys = taken.keys.toList()
            for (k in keys) {
                val v = taken[k] ?: continue
                Schema.tryFrom(v).getOrNull()?.let {
                    applyTransforms(it)
                    taken[k] = it.asValue()
                }
            }
        }
        return taken
    }

    /** Returns a sequence over the [transforms][SchemaSettings.transforms] used by this generator. */
    internal fun transformsMut(): MutableList<Transform> {
        val m = settings.transforms as? MutableList<Transform> ?: settings.transforms.toMutableList()
        settings.transforms = m
        return m
    }

    /**
     * Generates a JSON Schema for the given [JsonSchema] type. Includes any non-inlined dependent
     * schemas at the [definitions path][SchemaSettings.definitionsPath] (by default `"${'$'}defs"`).
     */
    fun rootSchemaFor(type: JsonSchema): Schema {
        val uid = schemaUid(type)
        rootSchemaIdStack.add(uid)

        val schema = jsonSchemaInternal(type, uid)

        val obj = schema.ensureObject()
        if (!obj.containsKey("title")) {
            obj["title"] = Value.Str(type.schemaName())
        }
        settings.metaSchema?.let { obj["\$schema"] = Value.Str(it) }

        addDefinitions(obj, definitions.toMutableMap())
        applyTransforms(schema)

        rootSchemaIdStack.removeAt(rootSchemaIdStack.size - 1)
        return schema
    }

    /** Consumes `this` and generates a root schema for [type]. */
    fun intoRootSchemaFor(type: JsonSchema): Schema {
        val uid = schemaUid(type)
        rootSchemaIdStack.add(uid)

        val schema = jsonSchemaInternal(type, uid)
        val obj = schema.ensureObject()

        if (!obj.containsKey("title")) {
            obj["title"] = Value.Str(type.schemaName())
        }
        val meta = settings.metaSchema
        if (meta != null) {
            obj["\$schema"] = Value.Str(meta)
            settings = settings.copy(metaSchema = null)
        }

        val defs = takeDefinitions(false)
        addDefinitions(obj, defs)
        applyTransforms(schema)

        return schema
    }

    /**
     * Generates a JSON Schema for the given example value.
     *
     * If the value implements [JsonSchema], prefer [rootSchemaFor] which generally produces a
     * more precise schema, particularly when the value contains any enums.
     */
    internal fun rootSchemaForValue(value: Value): Schema {
        val schema = SchemaForValue.of(value, includeTitle = true)

        val obj = schema.ensureObject()
        obj["examples"] = Value.Array(mutableListOf(value))
        settings.metaSchema?.let { obj["\$schema"] = Value.Str(it) }

        addDefinitions(obj, definitions.toMutableMap())
        applyTransforms(schema)
        return schema
    }

    /** Consumes `this` and generates a root schema for the given example value. */
    internal fun intoRootSchemaForValue(value: Value): Schema {
        val schema = SchemaForValue.of(value, includeTitle = true)
        val obj = schema.ensureObject()
        obj["examples"] = Value.Array(mutableListOf(value))
        val meta = settings.metaSchema
        if (meta != null) {
            obj["\$schema"] = Value.Str(meta)
            settings = settings.copy(metaSchema = null)
        }
        val defs = takeDefinitions(false)
        addDefinitions(obj, defs)
        applyTransforms(schema)
        return schema
    }

    /** Returns the contract for the settings on this `SchemaGenerator`. */
    fun contract(): Contract = settings.contract

    private fun jsonSchemaInternal(
        type: JsonSchema,
        uid: SchemaUid,
    ): Schema {
        val didAdd = pendingSchemaIds.add(uid)
        val schema = type.jsonSchema(this)
        if (didAdd) pendingSchemaIds.remove(uid)
        return schema
    }

    private fun addDefinitions(
        schemaObject: MutableMap<String, Value>,
        defs: MutableMap<String, Value>,
    ) {
        if (defs.isEmpty()) return

        val pointer = definitionsPathStripped()
        val target = jsonPointerMut(schemaObject, pointer, true) ?: return
        target.putAll(defs)
    }

    private fun applyTransforms(schema: Schema) {
        for (t in transformsMut()) {
            t.transform(schema)
        }
    }

    /**
     * Returns [SchemaSettings.definitionsPath] as a plain JSON pointer to the definitions object,
     * i.e. without a leading `#` or trailing `/`.
     */
    internal fun definitionsPathStripped(): String {
        var path = settings.definitionsPath
        if (path.startsWith("#")) path = path.substring(1)
        if (path.endsWith("/")) path = path.dropLast(1)
        return path
    }

    private fun schemaUid(type: JsonSchema): SchemaUid = SchemaUid(type.schemaId(), settings.contract)
}

private fun jsonPointerMut(
    objectIn: MutableMap<String, Value>,
    pointer: String,
    createIfMissing: Boolean,
): MutableMap<String, Value>? {
    if (!pointer.startsWith("/")) return null
    val rest = pointer.substring(1)
    if (rest.isEmpty()) return objectIn

    var current: MutableMap<String, Value> = objectIn
    for (rawSegment in rest.split('/')) {
        val segment =
            if (rawSegment.contains('~')) {
                rawSegment.replace("~1", "/").replace("~0", "~")
            } else {
                rawSegment
            }

        val next: Value =
            current[segment] ?: run {
                if (!createIfMissing) return null
                val newObj = Value.Object(linkedMapOf())
                current[segment] = newObj
                newObj
            }
        current = (next as? Value.Object)?.entries ?: return null
    }
    return current
}

private object SchemaForValue {
    fun of(
        value: Value,
        includeTitle: Boolean,
    ): Schema {
        val schema = describe(value)
        if (includeTitle) {
            schema.insert("title", Value.Str(titleFor(value)))
        }
        return schema
    }

    private fun describe(value: Value): Schema =
        when (value) {
            is Value.Null -> {
                val map = linkedMapOf<String, Value>("type" to Value.Str("null"))
                Schema.tryFrom(Value.Object(map)).getOrThrow()
            }
            is Value.Bool -> {
                val map = linkedMapOf<String, Value>("type" to Value.Str("boolean"))
                Schema.tryFrom(Value.Object(map)).getOrThrow()
            }
            is Value.Number -> {
                val isFloat = value.value is Double || value.value is Float
                val typeStr = if (isFloat) "number" else "integer"
                val map = linkedMapOf<String, Value>("type" to Value.Str(typeStr))
                Schema.tryFrom(Value.Object(map)).getOrThrow()
            }
            is Value.Str -> {
                val map = linkedMapOf<String, Value>("type" to Value.Str("string"))
                Schema.tryFrom(Value.Object(map)).getOrThrow()
            }
            is Value.Array -> {
                val map = linkedMapOf<String, Value>("type" to Value.Str("array"))
                Schema.tryFrom(Value.Object(map)).getOrThrow()
            }
            is Value.Object -> {
                val props = linkedMapOf<String, Value>()
                for ((k, v) in value.entries) {
                    props[k] = describe(v).asValue()
                }
                val schemaMap =
                    linkedMapOf<String, Value>(
                        "type" to Value.Str("object"),
                        "properties" to Value.Object(props),
                    )
                Schema.tryFrom(Value.Object(schemaMap)).getOrThrow()
            }
        }

    private fun titleFor(value: Value): String =
        when (value) {
            is Value.Null -> "Null"
            is Value.Bool -> "Boolean"
            is Value.Number -> "Number"
            is Value.Str -> "String"
            is Value.Array -> "Array"
            is Value.Object -> "Object"
        }
}

private fun sortedSetOfSchemaUids(): MutableSet<SchemaUid> = mutableSetOf()

private fun sortedMapOfSchemaUids(): MutableMap<SchemaUid, String> = mutableMapOf()

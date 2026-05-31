// port-lint: source transform.rs
package io.github.kotlinmania.schemars.transform

import io.github.kotlinmania.schemars.MetaSchemas
import io.github.kotlinmania.schemars.Schema
import io.github.kotlinmania.schemars.Value
import io.github.kotlinmania.schemars.jsonSchema
import io.github.kotlinmania.schemars.obj

/*
Contains the [Transform] interface, used to modify a constructed schema and optionally its
subschemas.

# Recursive Transforms

To make a transform recursive (i.e. apply it to subschemas), you have two options:
1. call the [transformSubschemas] function within the transform function
2. wrap the [Transform] in a [RecursiveTransform]
*/

/**
 * Trait used to modify a constructed schema and optionally its subschemas.
 *
 * See the [module documentation](Transform) for more details on implementing this trait.
 */
interface Transform {
    /**
     * Applies the transform to the given [Schema].
     *
     * When overriding this method, you may want to call the [transformSubschemas] function to
     * also transform any subschemas.
     */
    fun transform(schema: Schema)

    fun debugTypeName(): String = this::class.simpleName ?: "Transform"
}

/** Adapt a function into a [Transform]. */
fun transformOf(block: (Schema) -> Unit): Transform = object : Transform {
    override fun transform(schema: Schema) = block(schema)
}

/** Applies the given [Transform] to all direct subschemas of the [Schema]. */
fun transformSubschemas(t: Transform, schema: Schema) {
    val obj = schema.asObjectMut() ?: return
    // Snapshot keys to allow mutations during iteration without ConcurrentModificationException.
    val keys = obj.keys.toList()
    for (key in keys) {
        val value = obj[key] ?: continue
        // This is intentionally written to work with multiple JSON Schema versions, so that
        // users can add their own transforms on the end of e.g. SchemaSettings.draft07() and
        // they will still apply to all subschemas "as expected".
        // This is why this when statement contains both `additionalProperties` (which was
        // dropped in draft 2020-12) and `prefixItems` (which was added in draft 2020-12).
        when (key) {
            "not", "if", "then", "else", "contains",
            "additionalProperties", "propertyNames", "additionalItems" -> {
                Schema.tryFrom(value).getOrNull()?.let { t.transform(it); writeBack(obj, key, it) }
            }
            "allOf", "anyOf", "oneOf", "prefixItems" -> {
                (value as? Value.Array)?.items?.let { array ->
                    for (i in array.indices) {
                        Schema.tryFrom(array[i]).getOrNull()?.let { t.transform(it); array[i] = it.asValue() }
                    }
                }
            }
            // Support `items` array even though this is not allowed in draft 2020-12 (see above
            // comment)
            "items" -> {
                when (value) {
                    is Value.Array -> {
                        for (i in value.items.indices) {
                            Schema.tryFrom(value.items[i]).getOrNull()
                                ?.let { t.transform(it); value.items[i] = it.asValue() }
                        }
                    }
                    else -> {
                        Schema.tryFrom(value).getOrNull()?.let { t.transform(it); writeBack(obj, key, it) }
                    }
                }
            }
            "properties", "patternProperties", "\$defs", "definitions" -> {
                (value as? Value.Object)?.entries?.let { sub ->
                    val subKeys = sub.keys.toList()
                    for (k in subKeys) {
                        val v = sub[k] ?: continue
                        Schema.tryFrom(v).getOrNull()?.let { t.transform(it); sub[k] = it.asValue() }
                    }
                }
            }
        }
    }
}

private fun writeBack(obj: MutableMap<String, Value>, key: String, schema: Schema) {
    obj[key] = schema.asValue()
}

/**
 * Similar to [transformSubschemas], but only transforms subschemas that apply to the top-level
 * object, e.g. `oneOf` but not `properties`.
 */
internal fun transformImmediateSubschemas(t: Transform, schema: Schema) {
    val obj = schema.asObjectMut() ?: return
    val keys = obj.keys.toList()
    for (key in keys) {
        val value = obj[key] ?: continue
        when (key) {
            "if", "then", "else" -> {
                Schema.tryFrom(value).getOrNull()?.let { t.transform(it); obj[key] = it.asValue() }
            }
            "allOf", "anyOf", "oneOf" -> {
                (value as? Value.Array)?.items?.let { array ->
                    for (i in array.indices) {
                        Schema.tryFrom(array[i]).getOrNull()
                            ?.let { t.transform(it); array[i] = it.asValue() }
                    }
                }
            }
        }
    }
}

/**
 * A helper struct that can wrap a non-recursive [Transform] (i.e. one that does not apply to
 * subschemas) into a recursive one.
 *
 * Its implementation of [Transform] will first apply the inner transform to the "parent" schema,
 * and then its subschemas (and their subschemas, and so on).
 */
class RecursiveTransform(val inner: Transform) : Transform {
    override fun transform(schema: Schema) {
        inner.transform(schema)
        transformSubschemas(this, schema)
    }
}

/**
 * Replaces boolean JSON Schemas with equivalent object schemas.
 *
 * This also applies to subschemas.
 *
 * This is useful for dialects of JSON Schema (e.g. OpenAPI 3.0) that do not support booleans as
 * schemas.
 */
class ReplaceBoolSchemas(
    /**
     * When set to `true`, a schema's `additionalProperties` property will not be changed from a
     * boolean.
     *
     * Defaults to `false`.
     */
    var skipAdditionalProperties: Boolean = false,
) : Transform {
    override fun transform(schema: Schema) {
        val obj = schema.asObjectMut()
        if (obj != null) {
            if (skipAdditionalProperties) {
                val apValue = obj.remove("additionalProperties")
                if (apValue != null) {
                    transformSubschemas(this, schema)
                    schema.insert("additionalProperties", apValue)
                    return
                }
            }
            transformSubschemas(this, schema)
        } else {
            schema.ensureObject()
        }
    }
}

/**
 * Restructures JSON Schema objects so that the `${'$'}ref` property will never appear alongside any
 * other properties.
 *
 * This also applies to subschemas.
 *
 * This is useful for versions of JSON Schema (e.g. Draft 7) that do not support other properties
 * alongside `${'$'}ref`.
 */
class RemoveRefSiblings : Transform {
    override fun transform(schema: Schema) {
        transformSubschemas(this, schema)

        val obj = schema.asObjectMut() ?: return
        if (obj.size <= 1) return

        val refValue = obj.remove("\$ref") ?: return
        val allOf = obj.getOrPut("allOf") { Value.Array(mutableListOf()) }
        if (allOf is Value.Array) {
            allOf.items.add(Value.Object(linkedMapOf("\$ref" to refValue)))
        }
    }
}

/**
 * Removes the `examples` schema property and (if present) sets its first value as the `example`
 * property.
 *
 * This also applies to subschemas.
 *
 * This is useful for dialects of JSON Schema (e.g. OpenAPI 3.0) that do not support the `examples`
 * property.
 */
class SetSingleExample : Transform {
    override fun transform(schema: Schema) {
        transformSubschemas(this, schema)

        val examples = schema.remove("examples") as? Value.Array ?: return
        val first = examples.items.firstOrNull() ?: return
        schema.insert("example", first)
    }
}

/**
 * Replaces the `const` schema property with a single-valued `enum` property.
 *
 * This also applies to subschemas.
 *
 * This is useful for dialects of JSON Schema (e.g. OpenAPI 3.0) that do not support the `const`
 * property.
 */
class ReplaceConstValue : Transform {
    override fun transform(schema: Schema) {
        transformSubschemas(this, schema)

        val value = schema.remove("const") ?: return
        schema.insert("enum", Value.Array(mutableListOf(value)))
    }
}

/**
 * Rename the `prefixItems` schema property to `items`.
 *
 * This also applies to subschemas.
 *
 * If the schema contains both `prefixItems` and `items`, then this additionally renames `items` to
 * `additionalItems`.
 *
 * This is useful for versions of JSON Schema (e.g. Draft 7) that do not support the `prefixItems`
 * property.
 */
class ReplacePrefixItems : Transform {
    override fun transform(schema: Schema) {
        transformSubschemas(this, schema)

        val prefixItems = schema.remove("prefixItems") ?: return
        val previousItems = schema.insert("items", prefixItems)
        if (previousItems != null) {
            schema.insert("additionalItems", previousItems)
        }
    }
}

/**
 * Adds a `"nullable": true` property to schemas that allow `null` types.
 *
 * This also applies to subschemas.
 *
 * This is useful for dialects of JSON Schema (e.g. OpenAPI 3.0) that use `nullable` instead of
 * explicit null types.
 */
class AddNullable(
    /** When set to `true` (the default), `"null"` will also be removed from the schema's `type`. */
    var removeNullType: Boolean = true,
    /**
     * When set to `true` (the default), a schema that has a type only allowing `null` will also
     * have the equivalent `"const": null` inserted.
     */
    var addConstNull: Boolean = true,
) : Transform {
    override fun transform(schema: Schema) {
        if (schema.hasType("null")) {
            schema.insert("nullable", Value.Bool(true))

            // hasType returned true so we know "type" exists and is a string or array
            val ty = schema.getMut("type")!!
            when {
                ty is Value.Str || (ty is Value.Array && ty.items.all { it.asStr() == "null" }) -> {
                    if (addConstNull) {
                        schema.insert("const", Value.Null)
                        if (removeNullType) {
                            schema.remove("type")
                        }
                    } else if (removeNullType) {
                        schema.asObjectMut()?.set("type", Value.Array(mutableListOf()))
                    }
                }
                removeNullType && ty is Value.Array -> {
                    val arrItems = ty.items
                    arrItems.removeAll { it.asStr() == "null" }
                    if (arrItems.size == 1) {
                        schema.asObjectMut()?.set("type", arrItems.removeAt(0))
                    }
                }
            }
        }
        transformSubschemas(this, schema)
    }
}

/**
 * Replaces the `unevaluatedProperties` schema property with the `additionalProperties` property,
 * adding properties from a schema's subschemas to its `properties` where necessary.
 *
 * This also applies to subschemas.
 *
 * This is useful for versions of JSON Schema (e.g. Draft 7) that do not support the
 * `unevaluatedProperties` property.
 */
class ReplaceUnevaluatedProperties : Transform {
    override fun transform(schema: Schema) {
        transformSubschemas(this, schema)

        val up = schema.remove("unevaluatedProperties") ?: return
        schema.insert("additionalProperties", up)

        val gather = GatherPropertyNames()
        gather.transform(schema)
        val propertyNames = gather.names

        if (propertyNames.isEmpty()) return

        val obj = schema.ensureObject()
        val properties = obj.getOrPut("properties") { Value.Object(linkedMapOf()) }
        if (properties is Value.Object) {
            for (name in propertyNames) {
                if (name !in properties.entries) {
                    properties.entries[name] = Value.Bool(true)
                }
            }
        }
    }
}

/** Helper for getting property names for all *immediate* subschemas. */
private class GatherPropertyNames(val names: MutableSet<String> = sortedSetOf()) : Transform {
    override fun transform(schema: Schema) {
        schema.asObject()?.let { o ->
            (o["properties"] as? Value.Object)?.entries?.keys?.let { names.addAll(it) }
        }
        transformImmediateSubschemas(this, schema)
    }
}

private fun sortedSetOf(): MutableSet<String> = sortedSetWithDefaultComparator()
private fun sortedSetWithDefaultComparator(): MutableSet<String> {
    // Kotlin Multiplatform common doesn't expose TreeSet — use a LinkedHashSet so ordering is
    // (output ordering is determined by `properties` map iteration).
    return linkedSetOf()
}

/**
 * Removes any `format` values that are not defined by the JSON Schema standard or explicitly
 * allowed by a custom list.
 *
 * This also applies to subschemas.
 *
 * By default, this will infer the version of JSON Schema from the schema's `${'$'}schema` property,
 * and no additional formats will be allowed (even when the JSON schema allows nonstandard
 * formats).
 */
class RestrictFormats(
    /**
     * Whether to read the schema's `${'$'}schema` property to determine which version of JSON Schema
     * is being used, and allow only formats defined in that standard. If this is `true` but the
     * JSON Schema version can't be determined because `${'$'}schema` is missing or unknown, then no
     * `format` values will be removed.
     *
     * If this is set to `false`, then only the formats explicitly included in [allowedFormats]
     * will be allowed.
     *
     * By default, this is `true`.
     */
    var inferFromMetaSchema: Boolean = true,
    /**
     * Values of the `format` property in schemas that will always be allowed, regardless of the
     * inferred version of JSON Schema.
     */
    var allowedFormats: Set<String> = sortedSetOf(),
) : Transform {
    override fun transform(schema: Schema) {
        val impl = RestrictFormatsImpl(
            inferFromMetaSchema = inferFromMetaSchema,
            inferredFormats = null,
            allowedFormats = allowedFormats,
        )
        impl.transform(schema)
    }
}

internal val DEFINED_FORMATS: Array<String> = arrayOf(
    // `duration` and `uuid` are defined only in draft 2019-09+
    "duration",
    "uuid",
    // The rest are also defined in draft-07:
    "date-time",
    "date",
    "time",
    "email",
    "idn-email",
    "hostname",
    "idn-hostname",
    "ipv4",
    "ipv6",
    "uri",
    "uri-reference",
    "iri",
    "iri-reference",
    "uri-template",
    "json-pointer",
    "relative-json-pointer",
    "regex",
)

private class RestrictFormatsImpl(
    val inferFromMetaSchema: Boolean,
    var inferredFormats: List<String>?,
    val allowedFormats: Set<String>,
) : Transform {
    override fun transform(schema: Schema) {
        val obj = schema.asObjectMut() ?: return
        val previous = inferredFormats

        if (inferFromMetaSchema && obj.containsKey("\$schema")) {
            val schemaVal = (obj["\$schema"] as? Value.Str)?.value ?: ""
            inferredFormats = when (schemaVal) {
                MetaSchemas.DRAFT07 -> DEFINED_FORMATS.drop(2)
                MetaSchemas.DRAFT2019_09, MetaSchemas.DRAFT2020_12 -> DEFINED_FORMATS.toList()
                else -> {
                    // we can't handle an unrecognised meta-schema
                    return
                }
            }
        }

        val format = (obj["format"] as? Value.Str)?.value
        if (format != null) {
            val allowed = allowedFormats.contains(format) ||
                (inferredFormats?.contains(format) == true)
            if (!allowed) obj.remove("format")
        }

        transformSubschemas(this, schema)
        inferredFormats = previous
    }
}

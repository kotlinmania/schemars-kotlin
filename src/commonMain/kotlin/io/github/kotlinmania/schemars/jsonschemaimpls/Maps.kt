// port-lint: source json_schema_impls/maps.rs
package io.github.kotlinmania.schemars.jsonschemaimpls

import io.github.kotlinmania.schemars.JsonSchema
import io.github.kotlinmania.schemars.Schema
import io.github.kotlinmania.schemars.SchemaGenerator
import io.github.kotlinmania.schemars.Value
import io.github.kotlinmania.schemars.jsonSchema

/**
 * `impl<K: JsonSchema, V: JsonSchema> JsonSchema for BTreeMap<K, V>` (and
 * `HashMap<K, V, H>` via `forward_impl!`).
 *
 * The full upstream behaviour inspects the key schema to decide between `properties`,
 * `patternProperties`, and `additionalProperties` — string keys with `pattern` go to
 * `patternProperties`, string keys with `enum` go to `properties`, integer keys generate
 * `\d+` patterns, anything else falls back to `additionalProperties`.
 */
class MapSchema(val key: JsonSchema, val value: JsonSchema) : JsonSchema {
    override fun inlineSchema(): Boolean = true

    override fun schemaName(): String =
        if (key.schemaId() == StrSchema.schemaId()) {
            "Map_of_${value.schemaName()}"
        } else {
            "Map_from_${key.schemaName()}_to_${value.schemaName()}"
        }

    override fun schemaId(): String =
        if (key.schemaId() == StrSchema.schemaId()) {
            "Map<${value.schemaId()}>"
        } else {
            "Map<${key.schemaId()}, ${value.schemaId()}>"
        }

    override fun jsonSchema(generator: SchemaGenerator): Schema {
        val keySchema = key.jsonSchema(generator)
        val valueSchema = generator.subschemaFor(value)

        val mapSchema = jsonSchema { this["type"] = "object" }

        // Collect "options" — either a single schema or the entries of `anyOf`.
        val options: MutableList<MutableMap<String, Value>> = run {
            val anyOf = (keySchema.get("anyOf") as? Value.Array)?.items
            if (anyOf != null) {
                val collected = anyOf.mapNotNull { (it as? Value.Object)?.entries }.toMutableList()
                if (collected.size == anyOf.size) collected else mutableListOf()
            } else {
                val obj = keySchema.asObject()
                if (obj != null) mutableListOf(obj) else mutableListOf()
            }
        }
        if (options.isEmpty()) {
            return jsonSchema {
                this["additionalProperties"] = valueSchema
                this["type"] = "object"
            }
        }

        // Resolve `$ref` indirections to inline definitions, mirroring upstream.
        val prefix = "#${generator.definitionsPathStripped()}/"
        for (i in options.indices) {
            val option = options[i]
            val refStr = (option["\$ref"] as? Value.Str)?.value ?: continue
            if (!refStr.startsWith(prefix)) continue
            val name = refStr.removePrefix(prefix)
            val def = (generator.definitions()[name] as? Value.Object)?.entries
            if (def != null) options[i] = def
        }

        var additionalProperties = false
        var supportIntegers = IntegerSupport.None
        val patterns: MutableSet<String> = sortedSetOf()
        val properties: MutableSet<String> = sortedSetOf()
        for (option in options) {
            val keyPattern = (option["pattern"] as? Value.Str)?.value
            val keyEnum = (option["enum"] as? Value.Array)?.items
                ?.map { (it as? Value.Str)?.value }
                ?.takeIf { values -> values.all { it != null } }
                ?.map { it!! }
            val keyType = (option["type"] as? Value.Str)?.value
            val keyMinimum = (option["minimum"] as? Value.Number)?.asULong()

            when {
                keyPattern != null && keyType == "string" -> patterns.add(keyPattern)
                keyPattern == null && keyEnum != null && keyType == "string" -> {
                    properties.addAll(keyEnum)
                }
                keyType == "integer" && keyMinimum?.toLong() == 0L ->
                    supportIntegers = maxOf(supportIntegers, IntegerSupport.Unsigned)
                keyType == "integer" ->
                    supportIntegers = maxOf(supportIntegers, IntegerSupport.Signed)
                else -> additionalProperties = true
            }
        }

        if (additionalProperties) {
            mapSchema.insert("additionalProperties", valueSchema.toValue())
        } else {
            mapSchema.insert("additionalProperties", Value.Bool(false))
        }

        when (supportIntegers) {
            IntegerSupport.None -> {}
            IntegerSupport.Unsigned -> patterns.add("^\\d+\$")
            IntegerSupport.Signed -> patterns.add("^-?\\d+\$")
        }

        if (patterns.isNotEmpty()) {
            val patternsMap = linkedMapOf<String, Value>()
            for (p in patterns) patternsMap[p] = valueSchema.toValue()
            mapSchema.insert("patternProperties", Value.Object(patternsMap))
        }

        if (properties.isNotEmpty()) {
            val propertiesMap = linkedMapOf<String, Value>()
            for (p in properties) propertiesMap[p] = valueSchema.toValue()
            mapSchema.insert("properties", Value.Object(propertiesMap))
        }

        return mapSchema
    }
}

private enum class IntegerSupport { None, Unsigned, Signed }

private fun sortedSetOf(): MutableSet<String> = linkedSetOf()

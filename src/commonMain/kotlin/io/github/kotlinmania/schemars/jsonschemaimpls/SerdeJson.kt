// port-lint: source json_schema_impls/serdejson.rs
package io.github.kotlinmania.schemars.jsonschemaimpls

import io.github.kotlinmania.schemars.JsonSchema
import io.github.kotlinmania.schemars.Schema
import io.github.kotlinmania.schemars.SchemaGenerator
import io.github.kotlinmania.schemars.jsonSchema

/** `impl JsonSchema for serde_json::Value` — accepts any JSON value (`true` schema). */
object ValueSchema : JsonSchema {
    override fun inlineSchema(): Boolean = true
    override fun schemaName(): String = "AnyValue"
    override fun jsonSchema(generator: SchemaGenerator): Schema = jsonSchema(true)
}

/**
 * `forward_impl!(Map<String, Value> => BTreeMap<String, Value>)` — accepts any string-keyed
 * map of arbitrary values. Composes [MapSchema] with [StringSchema] and [ValueSchema].
 */
val JsonMapSchema: JsonSchema = MapSchema(StringSchema, ValueSchema)

/** `impl JsonSchema for serde_json::Number` — schema is `{ "type": "number" }`. */
object JsonNumberSchema : JsonSchema {
    override fun inlineSchema(): Boolean = true
    override fun schemaName(): String = "Number"
    override fun jsonSchema(generator: SchemaGenerator): Schema = jsonSchema {
        this["type"] = "number"
    }
}

/** `forward_impl!(serde_json::value::RawValue => Value)`. Gated on `raw_value` upstream. */
val RawValueSchema: JsonSchema = ValueSchema

// port-lint: source json_schema_impls/serdejson.rs
package io.github.kotlinmania.schemars.jsonschemaimpls

import io.github.kotlinmania.schemars.JsonSchema
import io.github.kotlinmania.schemars.Schema
import io.github.kotlinmania.schemars.generate.SchemaGenerator
import io.github.kotlinmania.schemars.jsonSchema

object ValueSchema : JsonSchema {
    override fun inlineSchema(): Boolean = true

    override fun schemaName(): String = "AnyValue"

    override fun jsonSchema(generator: SchemaGenerator): Schema = jsonSchema(true)
}

/**
 *
 * map of arbitrary values. Composes [MapSchema] with [StringSchema] and [ValueSchema].
 */
val JsonMapSchema: JsonSchema = MapSchema(StringSchema, ValueSchema)

object JsonNumberSchema : JsonSchema {
    override fun inlineSchema(): Boolean = true

    override fun schemaName(): String = "Number"

    override fun jsonSchema(generator: SchemaGenerator): Schema =
        jsonSchema {
            this["type"] = "number"
        }
}

val RawValueSchema: JsonSchema = ValueSchema

// port-lint: source json_schema_impls/either1.rs
package io.github.kotlinmania.schemars.jsonschemaimpls

import io.github.kotlinmania.schemars.JsonSchema
import io.github.kotlinmania.schemars.Schema
import io.github.kotlinmania.schemars.SchemaGenerator
import io.github.kotlinmania.schemars.jsonSchema

class EitherSchema(val left: JsonSchema, val right: JsonSchema) : JsonSchema {
    override fun inlineSchema(): Boolean = true
    override fun schemaName(): String = "Either_${left.schemaName()}_or_${right.schemaName()}"
    override fun schemaId(): String = "either::Either<${left.schemaId()}, ${right.schemaId()}>"
    override fun jsonSchema(generator: SchemaGenerator): Schema = jsonSchema {
        this["oneOf"] = listOf(
            mapOf(
                "type" to "object",
                "properties" to mapOf("Left" to generator.subschemaFor(left)),
                "additionalProperties" to false,
                "required" to listOf("Left"),
            ),
            mapOf(
                "type" to "object",
                "properties" to mapOf("Right" to generator.subschemaFor(right)),
                "additionalProperties" to false,
                "required" to listOf("Right"),
            ),
        )
    }
}

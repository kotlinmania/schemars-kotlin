// port-lint: source json_schema_impls/uuid1.rs
package io.github.kotlinmania.schemars.jsonschemaimpls

import io.github.kotlinmania.schemars.JsonSchema
import io.github.kotlinmania.schemars.Schema
import io.github.kotlinmania.schemars.SchemaGenerator
import io.github.kotlinmania.schemars.jsonSchema

/** `impl JsonSchema for uuid::Uuid` — string with `format: uuid`. */
object UuidSchema : JsonSchema {
    override fun inlineSchema(): Boolean = true
    override fun schemaName(): String = "Uuid"
    override fun schemaId(): String = "uuid::Uuid"
    override fun jsonSchema(generator: SchemaGenerator): Schema = jsonSchema {
        this["type"] = "string"
        this["format"] = "uuid"
    }
}

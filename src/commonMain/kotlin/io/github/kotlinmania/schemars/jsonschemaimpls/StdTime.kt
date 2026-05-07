// port-lint: source json_schema_impls/std_time.rs
package io.github.kotlinmania.schemars.jsonschemaimpls

import io.github.kotlinmania.schemars.JsonSchema
import io.github.kotlinmania.schemars.Schema
import io.github.kotlinmania.schemars.SchemaGenerator
import io.github.kotlinmania.schemars.jsonSchema

object DurationSchema : JsonSchema {
    override fun schemaName(): String = "Duration"
    override fun schemaId(): String = "std::time::Duration"
    override fun jsonSchema(generator: SchemaGenerator): Schema = jsonSchema {
        this["type"] = "object"
        this["required"] = listOf("secs", "nanos")
        this["properties"] = mapOf(
            "secs" to U64Schema.jsonSchema(generator),
            "nanos" to U32Schema.jsonSchema(generator),
        )
    }
}

object SystemTimeSchema : JsonSchema {
    override fun schemaName(): String = "SystemTime"
    override fun schemaId(): String = "std::time::SystemTime"
    override fun jsonSchema(generator: SchemaGenerator): Schema = jsonSchema {
        this["type"] = "object"
        this["required"] = listOf("secs_since_epoch", "nanos_since_epoch")
        this["properties"] = mapOf(
            "secs_since_epoch" to U64Schema.jsonSchema(generator),
            "nanos_since_epoch" to U32Schema.jsonSchema(generator),
        )
    }
}

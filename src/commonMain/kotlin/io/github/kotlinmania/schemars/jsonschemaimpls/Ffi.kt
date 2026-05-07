// port-lint: source json_schema_impls/ffi.rs
package io.github.kotlinmania.schemars.jsonschemaimpls

import io.github.kotlinmania.schemars.JsonSchema
import io.github.kotlinmania.schemars.Schema
import io.github.kotlinmania.schemars.SchemaGenerator
import io.github.kotlinmania.schemars.Value
import io.github.kotlinmania.schemars.jsonSchema

object OsStringSchema : JsonSchema {
    override fun schemaName(): String = "OsString"
    override fun schemaId(): String = "std::ffi::OsString"
    override fun jsonSchema(generator: SchemaGenerator): Schema = jsonSchema {
        this["oneOf"] = listOf(
            mapOf(
                "type" to "object",
                "properties" to mapOf(
                    "Unix" to SeqSchema(U8Schema).jsonSchema(generator),
                ),
                "required" to listOf("Unix"),
            ),
            mapOf(
                "type" to "object",
                "properties" to mapOf(
                    "Windows" to SeqSchema(U16Schema).jsonSchema(generator),
                ),
                "required" to listOf("Windows"),
            ),
        )
    }
}

val OsStrSchema: JsonSchema = OsStringSchema

object CStringSchema : JsonSchema {
    override fun schemaName(): String = "CString"
    override fun schemaId(): String = "std::ffi::CString"
    override fun jsonSchema(generator: SchemaGenerator): Schema {
        val ty: Any = if (generator.contract().isDeserialize()) {
            listOf("array", "string")
        } else {
            "array"
        }
        return jsonSchema {
            this["type"] = ty
            this["items"] = mapOf<String, Any>(
                "type" to "integer",
                "minimum" to 1,
                "maximum" to 255,
            )
        }
    }
}

val CStrSchema: JsonSchema = CStringSchema

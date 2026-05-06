// port-lint: source json_schema_impls/bytes1.rs
package io.github.kotlinmania.schemars.jsonschemaimpls

import io.github.kotlinmania.schemars.JsonSchema
import io.github.kotlinmania.schemars.Schema
import io.github.kotlinmania.schemars.SchemaGenerator
import io.github.kotlinmania.schemars.jsonSchema

/** `impl JsonSchema for bytes1::Bytes` — array of u8 (or string when deserialising). */
object BytesSchema : JsonSchema {
    override fun schemaName(): String = "Bytes"
    override fun schemaId(): String = "bytes::Bytes"
    override fun jsonSchema(generator: SchemaGenerator): Schema {
        val ty: Any = when (generator.contract()) {
            io.github.kotlinmania.schemars.generate.Contract.Deserialize -> listOf("array", "string")
            io.github.kotlinmania.schemars.generate.Contract.Serialize -> "array"
        }
        return jsonSchema {
            this["type"] = ty
            this["items"] = generator.subschemaFor(U8Schema)
        }
    }
}

/** `forward_impl!(bytes1::BytesMut => bytes1::Bytes);`. */
val BytesMutSchema: JsonSchema = BytesSchema

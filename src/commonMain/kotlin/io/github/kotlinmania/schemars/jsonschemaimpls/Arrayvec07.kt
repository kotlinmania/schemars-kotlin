// port-lint: source json_schema_impls/arrayvec07.rs
package io.github.kotlinmania.schemars.jsonschemaimpls

import io.github.kotlinmania.schemars.JsonSchema
import io.github.kotlinmania.schemars.Schema
import io.github.kotlinmania.schemars.SchemaGenerator
import io.github.kotlinmania.schemars.jsonSchema

/*
 * `forward_impl!((<const CAP: usize> JsonSchema for ArrayString<CAP>) => String);` — the
 * `arrayvec` crate's bounded string is described by the same schema as `String` (no maxLength
 * since the upstream limit is in bytes, not characters).
 */
val ArrayStringSchema: JsonSchema = StringSchema

/** `impl<T, const CAP: usize> JsonSchema for ArrayVec<T, CAP>` — array of T with maxItems. */
class ArrayVecSchema(val cap: Int, val element: JsonSchema) : JsonSchema {
    init { require(cap >= 0) { "ArrayVecSchema cap must be >= 0 (was $cap)" } }
    override fun inlineSchema(): Boolean = true
    override fun schemaName(): String = "Array_up_to_size_${cap}_of_${element.schemaName()}"
    override fun jsonSchema(generator: SchemaGenerator): Schema = jsonSchema {
        this["type"] = "array"
        this["items"] = generator.subschemaFor(element)
        this["maxItems"] = cap
    }
}

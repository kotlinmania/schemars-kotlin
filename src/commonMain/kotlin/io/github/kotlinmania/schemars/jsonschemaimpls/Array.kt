// port-lint: source json_schema_impls/array.rs
package io.github.kotlinmania.schemars.jsonschemaimpls

import io.github.kotlinmania.schemars.JsonSchema
import io.github.kotlinmania.schemars.Schema
import io.github.kotlinmania.schemars.generate.SchemaGenerator
import io.github.kotlinmania.schemars.jsonSchema

/**
 *
 *
 */
object EmptyArraySchema : JsonSchema {
    override fun inlineSchema(): Boolean = true
    override fun schemaName(): String = "EmptyArray"
    override fun schemaId(): String = "[]"
    override fun jsonSchema(generator: SchemaGenerator): Schema = jsonSchema {
        this["type"] = "array"
        this["maxItems"] = 0
    }
}

/**
 *
 *
 * Kotlin's `Array<T>` carries no compile-time length, so the array length is passed as a
 * runtime parameter rather than being a generic-const argument.
 */
class FixedArraySchema(val length: Int, val element: JsonSchema) : JsonSchema {
    init {
        require(length in 1..32) { "FixedArraySchema length must be 1..32 (was $length)" }
    }

    override fun inlineSchema(): Boolean = true
    override fun schemaName(): String = "Array_size_${length}_of_${element.schemaName()}"
    override fun schemaId(): String = "[$length; ${element.schemaId()}]"
    override fun jsonSchema(generator: SchemaGenerator): Schema = jsonSchema {
        this["type"] = "array"
        this["items"] = generator.subschemaFor(element)
        this["minItems"] = length
        this["maxItems"] = length
    }
}

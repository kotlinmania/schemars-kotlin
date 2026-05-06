// port-lint: source json_schema_impls/tuple.rs
package io.github.kotlinmania.schemars.jsonschemaimpls

import io.github.kotlinmania.schemars.JsonSchema
import io.github.kotlinmania.schemars.Schema
import io.github.kotlinmania.schemars.SchemaGenerator
import io.github.kotlinmania.schemars.jsonSchema

/**
 * `tuple_impls!` covers tuples of arity 1..16.
 *
 * In Kotlin tuples are not first-class for arities > 3 (`Pair`, `Triple`); the upstream
 * macro's pattern collapses to a single class parameterised by the list of element schemas.
 */
class TupleSchema(val elements: List<JsonSchema>) : JsonSchema {
    init {
        require(elements.isNotEmpty()) { "TupleSchema requires at least one element type" }
    }

    override fun inlineSchema(): Boolean = true

    override fun schemaName(): String =
        "Tuple_of_" + elements.joinToString("_and_") { it.schemaName() }

    override fun schemaId(): String =
        "(" + elements.joinToString(",") { it.schemaId() } + ")"

    override fun jsonSchema(generator: SchemaGenerator): Schema = jsonSchema {
        this["type"] = "array"
        this["prefixItems"] = elements.map { generator.subschemaFor(it) }
        this["minItems"] = elements.size
        this["maxItems"] = elements.size
    }
}

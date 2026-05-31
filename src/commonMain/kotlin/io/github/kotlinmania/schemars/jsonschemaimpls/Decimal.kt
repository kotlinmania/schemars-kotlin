// port-lint: source json_schema_impls/decimal.rs
package io.github.kotlinmania.schemars.jsonschemaimpls

import io.github.kotlinmania.schemars.JsonSchema
import io.github.kotlinmania.schemars.Schema
import io.github.kotlinmania.schemars.generate.SchemaGenerator
import io.github.kotlinmania.schemars.jsonSchema

/*
 *
 * `bigdecimal::BigDecimal`. The contract dictates whether the wire format accepts string or
 * number on deserialise, but always emits string on serialise.
 */

private fun decimalImpl(): JsonSchema =
    object : JsonSchema {
        override fun inlineSchema(): Boolean = true

        override fun schemaName(): String = "Decimal"

        override fun jsonSchema(generator: SchemaGenerator): Schema {
            val (ty, pattern) =
                when (generator.contract()) {
                    io.github.kotlinmania.schemars.generate.Contract.Deserialize ->
                        Pair(listOf("string", "number"), "^-?\\d+(\\.\\d+)?([eE]\\d+)?\$")
                    io.github.kotlinmania.schemars.generate.Contract.Serialize ->
                        Pair("string", "^-?\\d+(\\.\\d+)?\$")
                }
            return jsonSchema {
                this["type"] = ty
                this["pattern"] = pattern
            }
        }
    }

val RustDecimalSchema: JsonSchema = decimalImpl()

val BigDecimalSchema: JsonSchema = decimalImpl()

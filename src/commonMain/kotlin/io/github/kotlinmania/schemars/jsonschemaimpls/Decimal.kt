// port-lint: source json_schema_impls/decimal.rs
package io.github.kotlinmania.schemars.jsonschemaimpls

import io.github.kotlinmania.schemars.JsonSchema
import io.github.kotlinmania.schemars.Schema
import io.github.kotlinmania.schemars.SchemaGenerator
import io.github.kotlinmania.schemars.Value
import io.github.kotlinmania.schemars.jsonSchema

/*
 * `decimal_impl!` produces a `Decimal` schema for both `rust_decimal::Decimal` and
 * `bigdecimal::BigDecimal`. The contract dictates whether the wire format accepts string or
 * number on deserialise, but always emits string on serialise.
 */

private fun decimalImpl(): JsonSchema = object : JsonSchema {
    override fun inlineSchema(): Boolean = true
    override fun schemaName(): String = "Decimal"
    override fun jsonSchema(generator: SchemaGenerator): Schema {
        val (ty, pattern) = when (generator.contract()) {
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

/** `decimal_impl!(rust_decimal1::Decimal);`. */
val RustDecimalSchema: JsonSchema = decimalImpl()

/** `decimal_impl!(bigdecimal04::BigDecimal);`. */
val BigDecimalSchema: JsonSchema = decimalImpl()

@Suppress("unused") private fun _vUnused(v: Value): Value = v

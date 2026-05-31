// port-lint: source json_schema_impls/nonzero.rs
package io.github.kotlinmania.schemars.jsonschemaimpls

import io.github.kotlinmania.schemars.JsonSchema
import io.github.kotlinmania.schemars.Schema
import io.github.kotlinmania.schemars.Value
import io.github.kotlinmania.schemars.generate.SchemaGenerator
import io.github.kotlinmania.schemars.jsonSchema

/*
 *
 *
 * numeric type, but the schema constraint is well-defined: signed nonzero adds `not: { const: 0 }`,
 * unsigned nonzero adds `minimum: 1`.
 */

private fun nonzeroSigned(
    name: String,
    primitive: JsonSchema,
): JsonSchema =
    object : JsonSchema {
        override fun inlineSchema(): Boolean = true

        override fun schemaName(): String = name

        override fun schemaId(): String = "std::num::$name"

        override fun jsonSchema(generator: SchemaGenerator): Schema {
            val schema = primitive.jsonSchema(generator)
            schema.insert("not", Value.Object(linkedMapOf("const" to Value.Number(0))))
            return schema
        }
    }

private fun nonzeroUnsigned(
    name: String,
    primitive: JsonSchema,
): JsonSchema =
    object : JsonSchema {
        override fun inlineSchema(): Boolean = true

        override fun schemaName(): String = name

        override fun schemaId(): String = "std::num::$name"

        override fun jsonSchema(generator: SchemaGenerator): Schema {
            val schema = primitive.jsonSchema(generator)
            schema.insert("minimum", Value.Number(1))
            return schema
        }
    }

val NonZeroI8Schema: JsonSchema = nonzeroSigned("NonZeroI8", I8Schema)
val NonZeroI16Schema: JsonSchema = nonzeroSigned("NonZeroI16", I16Schema)
val NonZeroI32Schema: JsonSchema = nonzeroSigned("NonZeroI32", I32Schema)
val NonZeroI64Schema: JsonSchema = nonzeroSigned("NonZeroI64", I64Schema)
val NonZeroI128Schema: JsonSchema = nonzeroSigned("NonZeroI128", I128Schema)
val NonZeroIsizeSchema: JsonSchema = nonzeroSigned("NonZeroIsize", IsizeSchema)

val NonZeroU8Schema: JsonSchema = nonzeroUnsigned("NonZeroU8", U8Schema)
val NonZeroU16Schema: JsonSchema = nonzeroUnsigned("NonZeroU16", U16Schema)
val NonZeroU32Schema: JsonSchema = nonzeroUnsigned("NonZeroU32", U32Schema)
val NonZeroU64Schema: JsonSchema = nonzeroUnsigned("NonZeroU64", U64Schema)
val NonZeroU128Schema: JsonSchema = nonzeroUnsigned("NonZeroU128", U128Schema)
val NonZeroUsizeSchema: JsonSchema = nonzeroUnsigned("NonZeroUsize", UsizeSchema)

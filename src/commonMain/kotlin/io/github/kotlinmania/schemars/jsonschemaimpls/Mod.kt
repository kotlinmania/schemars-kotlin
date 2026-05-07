// port-lint: source json_schema_impls/mod.rs
package io.github.kotlinmania.schemars.jsonschemaimpls

import io.github.kotlinmania.schemars.JsonSchema

fun smallVecSchema(element: JsonSchema): JsonSchema = SeqSchema(element)

val SmolStr02Schema: JsonSchema = StringSchema

val SmolStr03Schema: JsonSchema = StringSchema

// port-lint: source json_schema_impls/jiff02.rs
package io.github.kotlinmania.schemars.jsonschemaimpls

import io.github.kotlinmania.schemars.JsonSchema
import io.github.kotlinmania.schemars.Schema
import io.github.kotlinmania.schemars.generate.SchemaGenerator
import io.github.kotlinmania.schemars.jsonSchema

/*
 *
 * jiff's date/time types. Identical pattern to `chrono04::formattedString`.
 */

private fun formattedString(name: String, format: String): JsonSchema = object : JsonSchema {
    override fun inlineSchema(): Boolean = true
    override fun schemaName(): String = name
    override fun schemaId(): String = "jiff::$name"
    override fun jsonSchema(generator: SchemaGenerator): Schema = jsonSchema {
        this["type"] = "string"
        this["format"] = format
    }
}

val SignedDurationSchema: JsonSchema = formattedString("SignedDuration", "duration")
val SpanSchema: JsonSchema = formattedString("Span", "duration")
val TimestampSchema: JsonSchema = formattedString("Timestamp", "date-time")
val ZonedSchema: JsonSchema = formattedString("Zoned", "zoned-date-time")
val DateSchema: JsonSchema = formattedString("Date", "date")
val TimeSchema: JsonSchema = formattedString("Time", "partial-time")
val DateTimeSchema: JsonSchema = formattedString("DateTime", "partial-date-time")

// port-lint: source json_schema_impls/chrono04.rs
package io.github.kotlinmania.schemars.jsonschemaimpls

import io.github.kotlinmania.schemars.JsonSchema
import io.github.kotlinmania.schemars.Schema
import io.github.kotlinmania.schemars.SchemaGenerator
import io.github.kotlinmania.schemars.jsonSchema

/** `impl JsonSchema for chrono::Weekday` — string enum of Mon..Sun. */
object WeekdaySchema : JsonSchema {
    override fun inlineSchema(): Boolean = true
    override fun schemaName(): String = "Weekday"
    override fun schemaId(): String = "chrono::Weekday"
    override fun jsonSchema(generator: SchemaGenerator): Schema = jsonSchema {
        this["type"] = "string"
        this["enum"] = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    }
}

/** `impl JsonSchema for chrono::TimeDelta` — `[seconds: i64, nanoseconds: 0..1e9]`. */
object TimeDeltaSchema : JsonSchema {
    override fun inlineSchema(): Boolean = true
    override fun schemaName(): String = "TimeDelta"
    override fun schemaId(): String = "chrono::TimeDelta"
    override fun jsonSchema(generator: SchemaGenerator): Schema = jsonSchema {
        this["type"] = "array"
        this["prefixItems"] = listOf(
            mapOf("type" to "integer", "format" to "int64"),
            mapOf("type" to "integer", "minimum" to 0, "exclusiveMaximum" to 1_000_000_000L),
        )
        this["minItems"] = 2
        this["maxItems"] = 2
    }
}

private fun formattedString(name: String, format: String): JsonSchema = object : JsonSchema {
    override fun inlineSchema(): Boolean = true
    override fun schemaName(): String = name
    override fun schemaId(): String = "chrono::$name"
    override fun jsonSchema(generator: SchemaGenerator): Schema = jsonSchema {
        this["type"] = "string"
        this["format"] = format
    }
}

val NaiveDateSchema: JsonSchema = formattedString("NaiveDate", "date")
val NaiveDateTimeSchema: JsonSchema = formattedString("NaiveDateTime", "partial-date-time")
val NaiveTimeSchema: JsonSchema = formattedString("NaiveTime", "partial-time")

/** `impl<Tz: TimeZone> JsonSchema for chrono::DateTime<Tz>` — string with `format: date-time`. */
val ChronoDateTimeSchema: JsonSchema = formattedString("DateTime", "date-time")

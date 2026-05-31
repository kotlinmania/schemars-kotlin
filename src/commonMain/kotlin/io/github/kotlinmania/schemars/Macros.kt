// port-lint: source macros.rs
@file:OptIn(kotlin.experimental.ExperimentalObjCRefinement::class)

package io.github.kotlinmania.schemars

import io.github.kotlinmania.schemars.generate.SchemaGenerator
import kotlin.native.HiddenFromObjC

/**
 * Generates a [Schema] for the given type using default settings.
 *
 * The default settings currently conform to JSON Schema 2020-12, but this is liable to change
 * in a future version of Schemars if support for other JSON Schema versions is added.
 *
 * The type must implement [JsonSchema].
 */
fun schemaFor(type: JsonSchema): Schema = SchemaGenerator.default().intoRootSchemaFor(type)

/**
 * Generates a [Schema] for the given example value using default settings.
 */
fun schemaForValue(value: Any?): Schema = SchemaGenerator.default().intoRootSchemaForValue(Value.of(value))

/**
 * Construct a [Schema] from a JSON boolean literal (`true` or `false`).
 */
fun jsonSchema(value: Boolean): Schema = Schema.from(value)

/**
 * Construct a [Schema] from a JSON object literal expressed as a builder block.
 */
@HiddenFromObjC
fun jsonSchema(block: JsonObjectBuilder.() -> Unit): Schema {
    val builder = JsonObjectBuilder()
    builder.block()
    return Schema.from(builder.entries)
}

/** Builder for object-shaped JSON literals. */
class JsonObjectBuilder internal constructor() {
    internal val entries: MutableMap<String, Value> = linkedMapOf()

    /** Add an entry. Right-hand side is coerced via [Value.of]. */
    operator fun set(
        key: String,
        value: Any?,
    ) {
        entries[key] = Value.of(value)
    }
}

/** Inline-object helper for nesting inside a [jsonSchema] block. */
internal fun obj(block: JsonObjectBuilder.() -> Unit): Value {
    val builder = JsonObjectBuilder()
    builder.block()
    return Value.Object(builder.entries)
}

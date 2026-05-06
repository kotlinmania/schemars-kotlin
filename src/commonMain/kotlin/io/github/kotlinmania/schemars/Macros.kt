// port-lint: source macros.rs
package io.github.kotlinmania.schemars

/*
 * Upstream uses `macro_rules!` to define `schema_for!`, `schema_for_value!`, and `json_schema!`.
 * Kotlin doesn't have hygienic macros, so each lowers to a function (or builder) that lets
 * callers reach the same shape:
 *
 *   schema_for!(MyType)      -> schemaFor(MyType)
 *   schema_for_value!(value) -> schemaForValue(value)
 *   json_schema!({ ... })    -> jsonSchema { "type" to "object" }
 *   json_schema!(true)       -> jsonSchema(true)
 *
 * The `json_schema!({...})` macro accepts inline object literals with nested
 * objects/arrays/primitives. The Kotlin builder mirrors that with a DSL: keys are added via
 * `String.to`, and nested objects are written with the `obj { }` helper or `mapOf(...)`.
 */

/**
 * Generates a [Schema] for the given [JsonSchema] type using default settings.
 *
 * The default settings currently conform to JSON Schema 2020-12, matching the upstream
 * crate behaviour.
 */
fun schemaFor(type: JsonSchema): Schema =
    SchemaGenerator.default().intoRootSchemaFor(type)

/**
 * Generates a [Schema] for the given example value using default settings.
 *
 * The value must be representable as a [Value] (one of `Boolean`, `Number`, `String`,
 * `List`, `Map`, or [Value]).
 */
fun schemaForValue(value: Any?): Schema =
    SchemaGenerator.default().intoRootSchemaForValue(Value.of(value))

/** Construct a [Schema] from a bool literal (mirrors `json_schema!(true)`/`json_schema!(false)`). */
fun jsonSchema(value: Boolean): Schema = Schema.from(value)

/** Construct a [Schema] from a builder block (mirrors `json_schema!({ ... })`). */
fun jsonSchema(block: JsonObjectBuilder.() -> Unit): Schema {
    val builder = JsonObjectBuilder()
    builder.block()
    return Schema.from(builder.entries)
}

/** Builder DSL for object-shaped JSON literals. */
class JsonObjectBuilder internal constructor() {
    internal val entries: MutableMap<String, Value> = linkedMapOf()

    /**
     * Add an entry. Right-hand side is coerced via [Value.of].
     *
     * Indexer form is used (rather than `infix to`) so that nested calls like
     * `mapOf("Ok" to subschema)` resolve `to` to `kotlin.Pair` instead of being captured by
     * the builder's receiver.
     */
    operator fun set(key: String, value: Any?) {
        entries[key] = Value.of(value)
    }
}

/** Inline-object helper for nesting inside a [jsonSchema] block. */
fun obj(block: JsonObjectBuilder.() -> Unit): Value {
    val builder = JsonObjectBuilder()
    builder.block()
    return Value.Object(builder.entries)
}

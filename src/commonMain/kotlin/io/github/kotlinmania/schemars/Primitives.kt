// port-lint: source src/json_schema_impls/primitives.rs
package io.github.kotlinmania.schemars

/**
 * The [JsonSchema] implementation for Rust's `String` primitive.
 *
 * Wrapping the impl in an `object` is the Kotlin transliteration of
 * the schemars associated-function call `<String>::json_schema(gen)`.
 * Call sites that mirror that Rust expression invoke
 * `StringJsonSchema.jsonSchema(generator)`.
 */
object StringJsonSchema : JsonSchema {
    override fun schemaName(): String = "String"

    override fun jsonSchema(generator: SchemaGenerator): Schema =
        Schema.Object(SchemaObject(instanceType = InstanceType.String))
}

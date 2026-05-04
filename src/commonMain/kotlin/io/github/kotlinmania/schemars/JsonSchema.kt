// port-lint: source src/lib.rs
package io.github.kotlinmania.schemars

/**
 * A type which can be described as a JSON Schema document.
 *
 * This is usually derived automatically using `#[derive(JsonSchema)]`.
 */
interface JsonSchema {
    /**
     * The name of the generated JSON Schema.
     *
     * This is used as the title for root schemas, and the key within the
     * root's `definitions` property for sub-schemas.
     */
    fun schemaName(): String

    /**
     * Generates a JSON Schema for this type.
     *
     * If the returned schema depends on any [Schema]s for other types, then
     * those should be added to the [SchemaGenerator]'s schema definitions.
     */
    fun jsonSchema(generator: SchemaGenerator): Schema
}

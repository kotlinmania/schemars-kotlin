// port-lint: source src/schema.rs
package io.github.kotlinmania.schemars

/**
 * A JSON Schema.
 *
 * A schema is either an opaque boolean (allowing or denying every
 * value) or an object describing the constraints on values that
 * conform to it.
 */
sealed interface Schema {
    /**
     * A schema that always validates (`true`) or always fails (`false`).
     */
    data class Bool(val value: Boolean) : Schema

    /**
     * A schema described by the contained [SchemaObject].
     */
    data class Object(val obj: SchemaObject) : Schema
}

/**
 * The full set of keywords allowed at the root of a schema object.
 *
 * Upstream `schemars::schema::SchemaObject` carries every JSON Schema
 * keyword on a single struct (instance type, format, validations,
 * subschemas, metadata, etc.). Only the fields read by current
 * consumers are preserved here; add fields as call sites need them.
 */
data class SchemaObject(
    val instanceType: InstanceType? = null,
)

/**
 * The set of types that a schema's `type` keyword may name.
 */
enum class InstanceType {
    Null,
    Boolean,
    Object,
    Array,
    Number,
    String,
    Integer,
}

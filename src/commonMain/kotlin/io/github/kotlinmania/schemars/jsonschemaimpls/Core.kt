// port-lint: source json_schema_impls/core.rs
package io.github.kotlinmania.schemars.jsonschemaimpls

import io.github.kotlinmania.schemars.JsonSchema
import io.github.kotlinmania.schemars.Schema
import io.github.kotlinmania.schemars.SchemaGenerator
import io.github.kotlinmania.schemars.Value
import io.github.kotlinmania.schemars.jsonSchema
import io.github.kotlinmania.schemars.private.allowNull

/**
 * `impl<T: JsonSchema> JsonSchema for Option<T>`.
 *
 * Wraps an inner [JsonSchema] and adds `null` as a permitted value.
 */
class OptionSchema(val inner: JsonSchema) : JsonSchema {
    override fun inlineSchema(): Boolean = true
    override fun schemaName(): String = "Nullable_${inner.schemaName()}"
    override fun schemaId(): String = "Option<${inner.schemaId()}>"
    override fun jsonSchema(generator: SchemaGenerator): Schema {
        val schema = generator.subschemaFor(inner)
        allowNull(generator, schema)
        return schema
    }

    override fun schemarsPrivateNonOptionalJsonSchema(generator: SchemaGenerator): Schema =
        inner.schemarsPrivateNonOptionalJsonSchema(generator)

    override fun schemarsPrivateIsOption(): Boolean = true
}

/** `impl<T: JsonSchema, E: JsonSchema> JsonSchema for Result<T, E>`. */
class ResultSchema(val ok: JsonSchema, val err: JsonSchema) : JsonSchema {
    override fun schemaName(): String =
        "Result_of_${ok.schemaName()}_or_${err.schemaName()}"
    override fun schemaId(): String =
        "Result<${ok.schemaId()}, ${err.schemaId()}>"
    override fun jsonSchema(generator: SchemaGenerator): Schema = jsonSchema {
        this["oneOf"] = listOf(
            mapOf(
                "type" to "object",
                "properties" to mapOf("Ok" to generator.subschemaFor(ok)),
                "required" to listOf("Ok"),
            ),
            mapOf(
                "type" to "object",
                "properties" to mapOf("Err" to generator.subschemaFor(err)),
                "required" to listOf("Err"),
            ),
        )
    }
}

/** `impl<T: JsonSchema> JsonSchema for Bound<T>`. */
class BoundSchema(val inner: JsonSchema) : JsonSchema {
    override fun schemaName(): String = "Bound_of_${inner.schemaName()}"
    override fun schemaId(): String = "Bound<${inner.schemaId()}>"
    override fun jsonSchema(generator: SchemaGenerator): Schema = jsonSchema {
        this["oneOf"] = listOf(
            mapOf(
                "type" to "object",
                "properties" to mapOf("Included" to generator.subschemaFor(inner)),
                "required" to listOf("Included"),
            ),
            mapOf(
                "type" to "object",
                "properties" to mapOf("Excluded" to generator.subschemaFor(inner)),
                "required" to listOf("Excluded"),
            ),
            mapOf(
                "type" to "string",
                "const" to "Unbounded",
            ),
        )
    }
}

/** `impl<T: JsonSchema> JsonSchema for Range<T>`. */
class RangeSchema(val inner: JsonSchema) : JsonSchema {
    override fun schemaName(): String = "Range_of_${inner.schemaName()}"
    override fun schemaId(): String = "Range<${inner.schemaId()}>"
    override fun jsonSchema(generator: SchemaGenerator): Schema {
        val sub = generator.subschemaFor(inner)
        return jsonSchema {
            this["type"] = "object"
            this["properties"] = mapOf("start" to sub, "end" to sub)
            this["required"] = listOf("start", "end")
        }
    }
}

/** `forward_impl!((<T: JsonSchema> JsonSchema for RangeInclusive<T>) => Range<T>);`. */
class RangeInclusiveSchema(inner: JsonSchema) : JsonSchema by RangeSchema(inner)

/** `forward_impl!((<T: ?Sized> JsonSchema for core::marker::PhantomData<T>) => ());`. */
val PhantomDataSchema: JsonSchema = UnitSchema

/** `forward_impl!((<'a> JsonSchema for core::fmt::Arguments<'a>) => String);`. */
val FmtArgumentsSchema: JsonSchema = StringSchema

/** Helper used by [OptionSchema.jsonSchema] — re-exports the `_private/mod.rs` allow_null. */
private fun allowNullDelegate(): Nothing = error("delegated to private.allowNull")

@Suppress("unused") private fun _unused(s: Schema): Schema = s

@Suppress("unused") private fun _unused(v: Value): Value = v

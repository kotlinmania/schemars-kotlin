// port-lint: source json_schema_impls/core.rs
package io.github.kotlinmania.schemars.jsonschemaimpls

import io.github.kotlinmania.schemars.JsonSchema
import io.github.kotlinmania.schemars.Schema
import io.github.kotlinmania.schemars.SchemaGenerator
import io.github.kotlinmania.schemars.Value
import io.github.kotlinmania.schemars.jsonSchema
import io.github.kotlinmania.schemars.private.allowNull

/** Schema for an optional value. Wraps the inner schema and adds `null` as permitted. */
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

/** Schema for an `Ok|Err` result with two parametric branches. */
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

/** Schema for a bound endpoint: included, excluded, or unbounded. */
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

/** Schema for a half-open range with `start` and `end` fields. */
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

/** Schema for an inclusive range — same shape as [RangeSchema]. */
class RangeInclusiveSchema(inner: JsonSchema) : JsonSchema by RangeSchema(inner)

/** Schema for a phantom marker type — equivalent to the unit schema. */
val PhantomDataSchema: JsonSchema = UnitSchema

/** Schema for formatted-arguments values — equivalent to the string schema. */
val FmtArgumentsSchema: JsonSchema = StringSchema


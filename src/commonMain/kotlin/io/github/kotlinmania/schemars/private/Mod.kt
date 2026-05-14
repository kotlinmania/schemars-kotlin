// port-lint: source _private/mod.rs
package io.github.kotlinmania.schemars.private

import io.github.kotlinmania.schemars.JsonSchema
import io.github.kotlinmania.schemars.Schema
import io.github.kotlinmania.schemars.generate.SchemaGenerator
import io.github.kotlinmania.schemars.Value
import io.github.kotlinmania.schemars.jsonSchema
import io.github.kotlinmania.schemars.jsonschemaimpls.UnitSchema
import io.github.kotlinmania.schemars.transform.Transform
import io.github.kotlinmania.schemars.transform.transformImmediateSubschemas

// Tracking file for upstream `_private/mod.rs`. The upstream module mixes pub-fn definitions
// (translated below as top-level functions) with one `pub use` re-export. Per the workspace
// rule on `mod.rs` re-exports (CLAUDE.md `## Re-exports from upstream `mod.rs` files`), no
// Kotlin `typealias` is introduced for the re-exported name. Callers reach the original
// symbol directly, either by package-local resolution or via explicit
// `import <upstream-fully-qualified-name> as <Name>`.

// Upstream `pub use` lines:
//   `pub use rustdoc::get_title_and_description;` — getTitleAndDescription lives in
//                                                    Rustdoc.kt in this same package. Already
//                                                    accessible by name from any caller in
//                                                    `io.github.kotlinmania.schemars.private`
//                                                    and by qualified import elsewhere; no
//                                                    synthetic alias.

// Callers migrated:
//   (none — workspace audit confirmed zero Kotlin callers held a direct or wildcard import of
//    `io.github.kotlinmania.schemars.private.getTitleAndDescription` at the time of the audit.
//    No same-package consumers existed; future imports should target
//    `io.github.kotlinmania.schemars.private.getTitleAndDescription` directly.)

// Projected callers (Rust):
//   workspace dependency search shows two `*-kotlin` repos with `tmp/` Rust trees that depend
//   on the schemars crate in their Cargo.toml: `rmcp-kotlin` and `serde-with-kotlin`. Neither
//   tmp/ tree references `schemars::_private::get_title_and_description` from upstream Rust;
//   future Kotlin ports of either crate should import the symbol from
//   `io.github.kotlinmania.schemars.private.getTitleAndDescription` directly when needed.

fun jsonSchemaForInternallyTaggedEnumNewtypeVariant(
    type: JsonSchema,
    generator: SchemaGenerator,
): Schema {
    val schema = type.jsonSchema(generator)

    // Inline the newtype's inner schema if any of:
    // - The type specifies that its schema should always be inlined
    // - The generator settings specify that all schemas should be inlined
    // - The inner type is a unit struct, which would cause an unsatisfiable schema due to
    //   mismatched `type`. In this case, we replace its type with "object" in
    //   `applyInternalEnumVariantTag`.
    // - The inner schema specified `"additionalProperties": false` or
    //   `"unevaluatedProperties": false`, since that would disallow the variant tag. If
    //   additional/unevaluatedProperties is in the top-level schema, then we can leave it
    //   there, because it will "see" the variant tag property. But if it is nested e.g. in an
    //   `allOf`, then it must be removed, which is why we run `AllowUnknownProperties` but
    //   only on immediate subschemas.

    val transform = AllowUnknownProperties()
    transformImmediateSubschemas(transform, schema)

    val inline = type.inlineSchema() ||
        generator.settings().inlineSubschemas ||
        schema.get("type")?.asStr() == "null" ||
        schema.get("additionalProperties")?.asBool() == false ||
        schema.get("unevaluatedProperties")?.asBool() == false ||
        transform.didModify

    if (inline) {
        return schema
    }

    // ...otherwise, we can freely refer to the schema via a `$ref`
    return generator.subschemaFor(type)
}

// Helper for generating schemas for flattened enums and `Option` fields.
fun jsonSchemaForFlatten(
    type: JsonSchema,
    generator: SchemaGenerator,
    required: Boolean,
): Schema {
    fun removeUnitVariants(schema: Schema): MutableList<Value>? {
        // For enums that only have unit variants, all variants are in `enum`
        if (schema.get("type")?.asStr() == "string") {
            // Remove both `"enum": [...]`...
            val a = schema.remove("enum") as? Value.Array
            if (a != null) {
                // ...and `"type": "string"`, since the variants are not serialized as strings
                schema.remove("type")
                return a.items.toMutableList()
            }
        }

        // For enums that have unit and other variants, unit variants are in the first `oneOf` item
        val oneOf = (schema.getMut("oneOf") as? Value.Array)?.items ?: return null
        val first = oneOf.firstOrNull() ?: return null
        val firstSchema = Schema.tryFrom(first).getOrNull() ?: return null
        if (firstSchema.get("type")?.asStr() == "string") {
            val a = firstSchema.remove("enum") as? Value.Array
            if (a != null) {
                oneOf.removeAt(0)
                return a.items.toMutableList()
            }
        }
        return null
    }

    /** Non-generic inner function to reduce monomorphization overhead. */
    fun inner(schema: Schema, isOptional: Boolean): Schema {
        // Special handling for externally-tagged enums with unit variants.
        // Unit variants are normally serialized as strings, but when flattened, are serialized
        // as objects like `{ "VariantName": null }`
        val unitVariants = removeUnitVariants(schema)
        if (unitVariants != null) {
            val obj = schema.ensureObject()
            val oneOf = obj.getOrPut("oneOf") { Value.Array(mutableListOf()) }
            if (oneOf is Value.Array) {
                for (variant in unitVariants) {
                    val name = variant.asStr() ?: continue
                    oneOf.items.add(
                        Value.Object(
                            linkedMapOf(
                                "type" to Value.Str("object"),
                                "properties" to Value.Object(
                                    linkedMapOf(
                                        name to Value.Object(
                                            linkedMapOf("type" to Value.Str("null")),
                                        ),
                                    ),
                                ),
                                "required" to Value.Array(mutableListOf(Value.Str(name))),
                            ),
                        ),
                    )
                }
            }
        }

        if (isOptional) {
            schema.remove("required")

            // Handle `Option<>` of externally/internally/adjacently-tagged enums
            val oneOf = schema.remove("oneOf")
            if (oneOf != null) {
                // We can't just add `{}` to the existing `oneOf`, because its items must be
                // mutually-exclusive, and `{}` matches everything.
                flatten(
                    schema,
                    jsonSchema {
                        "anyOf" to listOf(
                            mapOf("oneOf" to oneOf),
                            mapOf<String, Any?>(),
                        )
                    },
                )
            }

            // Handle `Option<>` of untagged enums
            val anyOf = schema.getMut("anyOf")
            if (anyOf is Value.Array) {
                val emptyObject = Value.Object(linkedMapOf())
                if (!anyOf.items.contains(emptyObject)) {
                    anyOf.items.add(emptyObject)
                }
            }
        }

        // Always allow additional/unevaluated properties, because the outer struct determines
        // whether it denies unknown fields.
        AllowUnknownProperties().transform(schema)

        return schema
    }

    return inner(
        type.schemarsPrivateNonOptionalJsonSchema(generator),
        type.schemarsPrivateIsOption() && !required,
    )
}

internal class AllowUnknownProperties(var didModify: Boolean = false) : Transform {
    override fun transform(schema: Schema) {
        if (schema.get("additionalProperties")?.asBool() == false) {
            schema.remove("additionalProperties")
            didModify = true
        }
        if (schema.get("unevaluatedProperties")?.asBool() == false) {
            schema.remove("unevaluatedProperties")
            didModify = true
        }
        transformImmediateSubschemas(this, schema)
    }
}

class MaybeSerializeWrapper(val value: Value?) {
    fun maybeToValue(): Value? = value
}

/** Create a schema for a unit enum variant. */
fun newUnitEnumVariant(variant: String): Schema = jsonSchema {
    this["type"] = "string"
    this["const"] = variant
}

class MaybeJsonSchemaWrapper(val schemaId: String?) {
    fun maybeSchemaId(): String = schemaId.orEmpty()
}

/** Create a schema for an externally tagged enum variant. */
fun newExternallyTaggedEnumVariant(variant: String, subSchema: Schema): Schema = jsonSchema {
    this["type"] = "object"
    this["properties"] = mapOf(variant to subSchema)
    this["required"] = listOf(variant)
    this["additionalProperties"] = false
}

/** Update a schema for an internally tagged enum variant. */
fun applyInternalEnumVariantTag(
    schema: Schema,
    tagName: String,
    variant: String,
    denyUnknownFields: Boolean,
) {
    val obj = schema.ensureObject()
    val isUnit = obj["type"]?.asStr() == "null"

    obj["type"] = Value.Str("object")

    val properties = obj.getOrPut("properties") { Value.Object(linkedMapOf()) }
    if (properties is Value.Object) {
        properties.entries[tagName] = Value.Object(
            linkedMapOf(
                "type" to Value.Str("string"),
                "const" to Value.Str(variant),
            ),
        )
    }

    val required = obj.getOrPut("required") { Value.Array(mutableListOf()) }
    if (required is Value.Array) {
        required.items.add(0, Value.Str(tagName))
    }

    if (denyUnknownFields && isUnit) {
        if (!obj.containsKey("additionalProperties")) {
            obj["additionalProperties"] = Value.Bool(false)
        }
    }
}

fun insertObjectProperty(
    schema: Schema,
    key: String,
    isOptional: Boolean,
    subSchema: Schema,
) {
    val obj = schema.ensureObject()
    val properties = obj.getOrPut("properties") { Value.Object(linkedMapOf()) }
    if (properties is Value.Object) {
        properties.entries[key] = subSchema.asValue()
    }

    if (!isOptional) {
        val req = obj.getOrPut("required") { Value.Array(mutableListOf()) }
        if (req is Value.Array) {
            req.items.add(Value.Str(key))
        }
    }
}

fun insertMetadataPropertyIfNonempty(schema: Schema, key: String, value: String) {
    if (value.isNotEmpty()) {
        schema.insert(key, Value.Str(value))
    }
}

fun insertValidationProperty(
    schema: Schema,
    requiredType: String,
    key: String,
    value: Value,
) {
    if (schemaHasType(schema, requiredType) ||
        (requiredType == "number" && schemaHasType(schema, "integer"))
    ) {
        schema.insert(key, value)
    }
}

private fun schemaHasType(schema: Schema, ty: String): Boolean {
    return when (val t = schema.get("type")) {
        is Value.Array -> t.items.any { it.asStr() == ty }
        is Value.Str -> t.value == ty
        else -> false
    }
}

fun mustContain(schema: Schema, substring: String) {
    val escaped = io.github.kotlinmania.schemars.private.escape(substring)
    insertValidationProperty(schema, "string", "pattern", Value.Str(escaped))
}

fun applyInnerValidation(schema: Schema, f: (Schema) -> Unit) {
    val inner = schema.getMut("items") ?: return
    val sub = Schema.tryFrom(inner).getOrNull() ?: return
    f(sub)
    schema.asObjectMut()?.set("items", sub.asValue())
}

fun flatten(schema: Schema, other: Schema) {
    fun flattenProperty(obj1: MutableMap<String, Value>, key: String, value2: Value) {
        if (key !in obj1) {
            obj1[key] = value2
            return
        }
        val current = obj1[key]
        when (key) {
            "required", "allOf" -> {
                if (current is Value.Array && value2 is Value.Array) {
                    current.items.addAll(value2.items)
                }
            }
            "properties", "patternProperties" -> {
                if (current is Value.Object && value2 is Value.Object) {
                    current.entries.putAll(value2.entries)
                }
            }
            "oneOf", "anyOf" -> {
                obj1.remove(key)
                flattenProperty(
                    obj1,
                    "allOf",
                    Value.Array(
                        mutableListOf(
                            Value.Object(linkedMapOf(key to (current ?: Value.Null))),
                            Value.Object(linkedMapOf(key to value2)),
                        ),
                    ),
                )
            }
            // leave the original value as it is (don't modify `schema`)
            else -> {}
        }
    }

    when (val r = other.tryToObject()) {
        is io.github.kotlinmania.schemars.TryToObjectResult.Err -> {
            if (r.bool) {
                val obj = schema.asObjectMut()
                if (obj != null &&
                    !obj.containsKey("additionalProperties") &&
                    !obj.containsKey("unevaluatedProperties")
                ) {
                    val key = if (containsImmediateSubschema(obj)) {
                        "unevaluatedProperties"
                    } else {
                        "additionalProperties"
                    }
                    obj[key] = Value.Bool(true)
                }
            }
        }
        is io.github.kotlinmania.schemars.TryToObjectResult.Ok -> {
            val obj1 = schema.ensureObject()
            val obj2 = r.entries

            // For complex merges, replace `additionalProperties` with `unevaluatedProperties`
            // which usually "works out better".
            normaliseAdditionalUnevaluatedProperties(obj1, obj2)
            normaliseAdditionalUnevaluatedProperties(obj2, obj1)

            for ((key, value2) in obj2.toMap()) {
                flattenProperty(obj1, key, value2)
            }
        }
    }
}

private fun normaliseAdditionalUnevaluatedProperties(
    schemaObj1: MutableMap<String, Value>,
    schemaObj2: MutableMap<String, Value>,
) {
    if (schemaObj1.containsKey("additionalProperties") &&
        (schemaObj2.containsKey("unevaluatedProperties") || containsImmediateSubschema(schemaObj2))
    ) {
        val ap = schemaObj1.remove("additionalProperties") ?: Value.Null
        schemaObj1["unevaluatedProperties"] = ap
    }
}

private fun containsImmediateSubschema(schemaObj: Map<String, Value>): Boolean =
    listOf("if", "allOf", "anyOf", "oneOf", "\$ref").any { schemaObj.containsKey(it) }

internal fun allowNull(generator: SchemaGenerator, schema: Schema) {
    fun isNullSchema(value: Value): Boolean {
        val s = Schema.tryFrom(value).getOrNull() ?: return false
        return when (val t = s.get("type")) {
            is Value.Str -> t.value == "null"
            is Value.Array -> t.items.any { it.asStr() == "null" }
            else -> false
        }
    }

    when (val r = schema.tryAsObjectMut()) {
        is io.github.kotlinmania.schemars.TryToObjectResult.Ok -> {
            val obj = r.entries
            if (obj.size == 1) {
                val anyOf = obj["anyOf"] as? Value.Array
                if (anyOf != null && anyOf.items.any { isNullSchema(it) }) {
                    return
                }
            }

            if (containsImmediateSubschema(obj)) {
                val current = Value.Object(linkedMapOf<String, Value>().apply { putAll(obj) })
                val unitSchema = UnitSchema.jsonSchema(generator)
                val newSchema = jsonSchema {
                    "anyOf" to listOf(current, unitSchema)
                }
                obj.clear()
                newSchema.asObject()?.let { obj.putAll(it) }
                // No need to check `type`/`const`/`enum` because they're trivially not present
                return
            }

            val instanceType = obj["type"]
            when (instanceType) {
                is Value.Array -> {
                    val nullVal = Value.Str("null")
                    if (!instanceType.items.contains(nullVal)) {
                        instanceType.items.add(nullVal)
                    }
                }
                is Value.Str -> {
                    if (instanceType.value != "null") {
                        obj["type"] = Value.Array(
                            mutableListOf(Value.Str(instanceType.value), Value.Str("null")),
                        )
                    }
                }
                else -> {}
            }

            val c = obj.remove("const")
            if (c != null) {
                if (c !is Value.Null) {
                    obj["enum"] = Value.Array(mutableListOf(c, Value.Null))
                }
            } else {
                val e = obj["enum"] as? Value.Array
                if (e != null && !e.items.contains(Value.Null)) {
                    e.items.add(Value.Null)
                }
            }
        }
        is io.github.kotlinmania.schemars.TryToObjectResult.Err -> {
            if (!r.bool) {
                val unit = UnitSchema.jsonSchema(generator)
                val src = unit.asObject() ?: linkedMapOf()
                val obj = schema.ensureObject()
                obj.clear()
                obj.putAll(src)
            }
        }
    }
}

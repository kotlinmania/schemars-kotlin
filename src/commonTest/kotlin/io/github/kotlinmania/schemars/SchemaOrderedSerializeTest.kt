// port-lint: source schema.rs
package io.github.kotlinmania.schemars

import io.github.kotlinmania.schemars.generate.SchemaGenerator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SchemaOrderedSerializeTest {
    @Test
    fun orderedKeywordsLeadAndTrail() {
        val schema =
            jsonSchema {
                this["\$defs"] = mapOf("Foo" to true)
                this["additionalProperties"] = false
                this["title"] = "A title"
                this["\$schema"] = "https://json-schema.org/draft/2020-12/schema"
                this["description"] = "An object"
                this["type"] = "object"
                this["\$id"] = "https://example.test/foo"
            }

        val obj = (schema.serialize() as Value.Object).entries
        assertEquals(
            listOf("\$id", "\$schema", "title", "description", "type", "additionalProperties", "\$defs"),
            obj.keys.toList(),
        )
    }

    @Test
    fun orderedKeywordsPreservesInsertionForCustomKeys() {
        val schema =
            jsonSchema {
                this["type"] = "object"
                this["zebra"] = "z"
                this["alpha"] = "a"
                this["middle"] = "m"
            }

        val obj = (schema.serialize() as Value.Object).entries
        assertEquals(listOf("type", "zebra", "alpha", "middle"), obj.keys.toList())
    }

    @Test
    fun examplesAndDefaultArePassedThroughUnchanged() {
        val schema =
            jsonSchema {
                this["type"] = "object"
                this["examples"] = listOf(linkedMapOf("description" to "a", "title" to "b"))
                this["default"] = linkedMapOf("title" to "c", "type" to "object")
            }

        val out = schema.serialize() as Value.Object
        val examples = (out.entries["examples"] as Value.Array).items.first() as Value.Object
        val default = out.entries["default"] as Value.Object

        // `examples` and `default` carry plain values, not subschemas — their inner keys must
        // keep their insertion order (description→title in examples; title→type in default).
        assertEquals(listOf("description", "title"), examples.entries.keys.toList())
        assertEquals(listOf("title", "type"), default.entries.keys.toList())
    }

    @Test
    fun customXPrefixedKeysAreNotReordered() {
        val schema =
            jsonSchema {
                this["type"] = "object"
                this["x-custom"] = linkedMapOf("description" to "a", "title" to "b")
            }

        val out = schema.serialize() as Value.Object
        val custom = out.entries["x-custom"] as Value.Object
        assertEquals(listOf("description", "title"), custom.entries.keys.toList())
    }

    @Test
    fun propertiesObjectKeepsInsertionOrderButReordersSubschemas() {
        val schema =
            jsonSchema {
                this["type"] = "object"
                this["properties"] =
                    linkedMapOf(
                        "zebra" to
                            linkedMapOf(
                                "description" to "z",
                                "type" to "string",
                            ),
                        "alpha" to
                            linkedMapOf(
                                "description" to "a",
                                "type" to "number",
                            ),
                    )
            }

        val out = schema.serialize() as Value.Object
        val props = out.entries["properties"] as Value.Object
        // Direct keys keep insertion order: zebra first, alpha second.
        assertEquals(listOf("zebra", "alpha"), props.entries.keys.toList())
        // But each property's *value* is a schema, so it gets re-ordered:
        // ORDERED_KEYWORDS_START puts `description` before `type`.
        val zebra = props.entries["zebra"] as Value.Object
        assertEquals(listOf("description", "type"), zebra.entries.keys.toList())
    }

    @Test
    fun defsObjectKeepsInsertionOrderButReordersSubschemas() {
        val schema =
            jsonSchema {
                this["\$defs"] =
                    linkedMapOf(
                        "Foo" to linkedMapOf("description" to "f", "type" to "object"),
                        "Bar" to linkedMapOf("description" to "b", "type" to "string"),
                    )
                this["type"] = "object"
            }

        val out = schema.serialize() as Value.Object
        // `$defs` is in ORDERED_KEYWORDS_END so it comes after `type`.
        assertEquals(listOf("type", "\$defs"), out.entries.keys.toList())
        val defs = out.entries["\$defs"] as Value.Object
        assertEquals(listOf("Foo", "Bar"), defs.entries.keys.toList())
        val foo = defs.entries["Foo"] as Value.Object
        assertEquals(listOf("description", "type"), foo.entries.keys.toList())
    }

    @Test
    fun nestedSubschemasInArrayAreReordered() {
        val schema =
            jsonSchema {
                this["type"] = "object"
                this["anyOf"] =
                    listOf(
                        linkedMapOf("description" to "first", "type" to "string"),
                        linkedMapOf("description" to "second", "type" to "number"),
                    )
            }

        val out = schema.serialize() as Value.Object
        val anyOf = (out.entries["anyOf"] as Value.Array).items
        val first = anyOf[0] as Value.Object
        val second = anyOf[1] as Value.Object
        assertEquals(listOf("description", "type"), first.entries.keys.toList())
        assertEquals(listOf("description", "type"), second.entries.keys.toList())
    }

    @Test
    fun primitiveValuesPassThroughUnchanged() {
        val boolSchema = Schema.from(true)
        assertEquals(Value.Bool(true), boolSchema.serialize())
    }

    @Test
    fun schemaImplementsJsonSchemaWithUpstreamNames() {
        val s = Schema.default()
        assertEquals("Schema", s.schemaName())
        assertEquals("schemars::Schema", s.schemaId())
        // The schema-of-schema describes a value that is an object or a boolean.
        val described = s.jsonSchema(SchemaGenerator.default()).serialize() as Value.Object
        val typeField = described.entries["type"] as Value.Array
        val expected = listOf("object", "boolean")
        assertEquals(expected, typeField.items.map { (it as Value.Str).value })
    }

    @Test
    fun deserializeWrapsObjectsAndBoolsAndRejectsOthers() {
        val obj = Schema.deserialize(Value.Object(linkedMapOf("type" to Value.Str("object"))))
        assertTrue(obj.isSuccess())

        val b = Schema.deserialize(Value.Bool(true))
        assertTrue(b.isSuccess())

        val n = Schema.deserialize(Value.Null)
        assertTrue(n.isFailure())

        val s = Schema.deserialize(Value.Str("not a schema"))
        assertTrue(s.isFailure())
    }
}

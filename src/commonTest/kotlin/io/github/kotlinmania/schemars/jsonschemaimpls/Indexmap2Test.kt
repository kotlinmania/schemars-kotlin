// port-lint: tests tests/integration/indexmap.rs
package io.github.kotlinmania.schemars.jsonschemaimpls

import io.github.kotlinmania.indexmap.IndexMap
import io.github.kotlinmania.indexmap.IndexSet
import io.github.kotlinmania.schemars.Value
import io.github.kotlinmania.schemars.generate.SchemaGenerator
import kotlin.test.Test
import kotlin.test.assertEquals

class Indexmap2Test {
    @Test
    fun indexMapSchemaMatchesMapSchema() {
        val map = IndexMap.new<String, Boolean>()
        map.insert("key", true)

        val schema = map.indexMapJsonSchema(StrSchema, BoolSchema)
            .jsonSchema(SchemaGenerator.default())
            .serialize() as Value.Object

        assertEquals(Value.Str("object"), schema.entries["type"])
        val additionalProperties = schema.entries["additionalProperties"] as Value.Object
        assertEquals(Value.Str("boolean"), additionalProperties.entries["type"])
    }

    @Test
    fun indexSetSchemaMatchesSetSchema() {
        val set = IndexSet.new<String>()
        set.insert("test")

        val schema = set.indexSetJsonSchema(StrSchema)
            .jsonSchema(SchemaGenerator.default())
            .serialize() as Value.Object

        assertEquals(Value.Str("array"), schema.entries["type"])
        assertEquals(Value.Bool(true), schema.entries["uniqueItems"])
    }
}

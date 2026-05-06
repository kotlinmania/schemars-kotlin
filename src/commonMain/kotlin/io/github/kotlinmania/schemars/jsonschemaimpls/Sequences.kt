// port-lint: source json_schema_impls/sequences.rs
package io.github.kotlinmania.schemars.jsonschemaimpls

import io.github.kotlinmania.schemars.JsonSchema
import io.github.kotlinmania.schemars.Schema
import io.github.kotlinmania.schemars.SchemaGenerator
import io.github.kotlinmania.schemars.jsonSchema

/**
 * `seq_impl!` covers `BinaryHeap<T>`, `LinkedList<T>`, `[T]`, `Vec<T>`, `VecDeque<T>`.
 *
 * In Kotlin all sequence-like containers (`MutableList`, `LinkedList`-equivalent ports, etc.)
 * map to a single parametric schema describing an `array` of [inner].
 */
class SeqSchema(val inner: JsonSchema) : JsonSchema {
    override fun inlineSchema(): Boolean = true
    override fun schemaName(): String = "Array_of_${inner.schemaName()}"
    override fun schemaId(): String = "[${inner.schemaId()}]"
    override fun jsonSchema(generator: SchemaGenerator): Schema = jsonSchema {
        this["type"] = "array"
        this["items"] = generator.subschemaFor(inner)
    }
}

/** `set_impl!` covers `BTreeSet<T>`, `HashSet<T>`. Adds `uniqueItems: true`. */
class SetSchema(val inner: JsonSchema) : JsonSchema {
    override fun inlineSchema(): Boolean = true
    override fun schemaName(): String = "Set_of_${inner.schemaName()}"
    override fun schemaId(): String = "Set<${inner.schemaId()}>"
    override fun jsonSchema(generator: SchemaGenerator): Schema = jsonSchema {
        this["type"] = "array"
        this["uniqueItems"] = true
        this["items"] = generator.subschemaFor(inner)
    }
}

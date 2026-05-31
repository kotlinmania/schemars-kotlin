// port-lint: source json_schema_impls/indexmap2.rs
package io.github.kotlinmania.schemars.jsonschemaimpls

import io.github.kotlinmania.indexmap.IndexMap
import io.github.kotlinmania.indexmap.IndexSet
import io.github.kotlinmania.schemars.JsonSchema

/*
 *
 * schema is identical to BTreeMap's. Same for IndexSet -> BTreeSet.
 */

/** `IndexMap<K, V>` schema, identical to the ordered-map schema. */
class IndexMapSchema(
    key: JsonSchema,
    value: JsonSchema,
) : JsonSchema by MapSchema(key, value)

/** `IndexSet<T>` schema, identical to the ordered-set schema. */
class IndexSetSchema(
    inner: JsonSchema,
) : JsonSchema by SetSchema(inner)

/** Return the schema implementation for an [IndexMap] value. */
fun <K, V> IndexMap<K, V>.indexMapJsonSchema(
    key: JsonSchema,
    value: JsonSchema,
): JsonSchema = IndexMapSchema(key, value)

/** Return the schema implementation for an [IndexSet] value. */
fun <T> IndexSet<T>.indexSetJsonSchema(inner: JsonSchema): JsonSchema = IndexSetSchema(inner)

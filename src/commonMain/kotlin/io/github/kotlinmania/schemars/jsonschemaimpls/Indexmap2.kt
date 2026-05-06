// port-lint: source json_schema_impls/indexmap2.rs
package io.github.kotlinmania.schemars.jsonschemaimpls

import io.github.kotlinmania.schemars.JsonSchema

/*
 * `forward_impl!((<K, V, H> JsonSchema for IndexMap<K, V, H>) => BTreeMap<K, V>)` — IndexMap's
 * schema is identical to BTreeMap's. Same for IndexSet → BTreeSet.
 */

/** `IndexMap<K, V>` schema (alias of [MapSchema]). */
class IndexMapSchema(key: JsonSchema, value: JsonSchema) : JsonSchema by MapSchema(key, value)

/** `IndexSet<T>` schema (alias of [SetSchema]). */
class IndexSetSchema(inner: JsonSchema) : JsonSchema by SetSchema(inner)

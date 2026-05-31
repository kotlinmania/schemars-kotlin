// port-lint: source json_schema_impls/indexmap2.rs
@file:OptIn(kotlin.experimental.ExperimentalObjCRefinement::class)

package io.github.kotlinmania.schemars.jsonschemaimpls

import io.github.kotlinmania.indexmap.IndexMap
import io.github.kotlinmania.indexmap.IndexSet
import io.github.kotlinmania.schemars.JsonSchema
import kotlin.native.HiddenFromObjC

/*
 *
 * schema is identical to BTreeMap's. Same for IndexSet -> BTreeSet.
 */

/** `IndexMap<K, V>` schema, identical to the ordered-map schema. */
@HiddenFromObjC
class IndexMapSchema<K, V>(
    key: JsonSchema,
    value: JsonSchema,
) : JsonSchema by MapSchema(key, value)

/** `IndexSet<T>` schema, identical to the ordered-set schema. */
@HiddenFromObjC
class IndexSetSchema<T>(
    inner: JsonSchema,
) : JsonSchema by SetSchema(inner)

/** Return the schema implementation for an [IndexMap] value. */
fun <K, V> IndexMap<K, V>.indexMapJsonSchema(
    key: JsonSchema,
    value: JsonSchema,
): JsonSchema = IndexMapSchema<K, V>(key, value)

/** Return the schema implementation for an [IndexSet] value. */
fun <T> IndexSet<T>.indexSetJsonSchema(inner: JsonSchema): JsonSchema = IndexSetSchema<T>(inner)

// port-lint: source json_schema_impls/atomic.rs
package io.github.kotlinmania.schemars.jsonschemaimpls

import io.github.kotlinmania.schemars.JsonSchema

/*
 * Each upstream `forward_impl!(AtomicX => X)` says "the AtomicX schema is the same as X's
 * schema". Kotlin's `kotlin.concurrent.atomics.AtomicInt`/`AtomicLong`/`AtomicReference`
 * follow the same idea — the wire format is the inner type's. So the schemas below are simple
 * aliases of the corresponding primitive schemas.
 */

val AtomicBoolSchema: JsonSchema = BoolSchema
val AtomicI8Schema: JsonSchema = I8Schema
val AtomicI16Schema: JsonSchema = I16Schema
val AtomicI32Schema: JsonSchema = I32Schema
val AtomicI64Schema: JsonSchema = I64Schema
val AtomicIsizeSchema: JsonSchema = IsizeSchema
val AtomicU8Schema: JsonSchema = U8Schema
val AtomicU16Schema: JsonSchema = U16Schema
val AtomicU32Schema: JsonSchema = U32Schema
val AtomicU64Schema: JsonSchema = U64Schema
val AtomicUsizeSchema: JsonSchema = UsizeSchema

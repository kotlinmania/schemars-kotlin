// port-lint: source json_schema_impls/mod.rs
package io.github.kotlinmania.schemars.jsonschemaimpls

import io.github.kotlinmania.schemars.JsonSchema

// Tracking file for upstream `json_schema_impls/mod.rs`. The upstream module is composed of
// submodule declarations (each translated as its own Kotlin file in this package) and three
// inline `forward_impl!` invocations for `SmallVec`, `SmolStr` (v0.2), and `SmolStr` (v0.3).
// Per the workspace rule on `mod.rs` re-exports (CLAUDE.md `## Re-exports from upstream `mod.rs`
// files`), the inline `forward_impl!` translations live below as ordinary Kotlin definitions
// rather than as `typealias` aliases. The upstream module declares **no** `pub use`
// re-exports; there is no migration ledger to maintain for this file.

// Inline `forward_impl!` translations:
fun smallVecSchema(element: JsonSchema): JsonSchema = SeqSchema(element)

val SmolStr02Schema: JsonSchema = StringSchema

val SmolStr03Schema: JsonSchema = StringSchema

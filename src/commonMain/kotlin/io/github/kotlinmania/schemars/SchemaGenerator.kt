// port-lint: ignore
package io.github.kotlinmania.schemars

// Tracking file for upstream `src/lib.rs`:
//   `pub use generate::SchemaGenerator;`
//
// The full type lives at `io.github.kotlinmania.schemars.generate.SchemaGenerator`.
// Existing same-repo callers still rely on this compatibility alias while the
// re-export migration proceeds. New or migrated callers should import the
// original symbol directly.
//
// Callers migrated:
//   /Volumes/stuff/Projects/kotlinmania/codex-kotlin/src/commonMain/kotlin/io/github/solaceharmony/codex/protocol/ConversationId.kt
//   /Volumes/stuff/Projects/kotlinmania/schemars-kotlin/src/commonMain/kotlin/io/github/kotlinmania/schemars/jsonschemaimpls/SerdeJson.kt

typealias SchemaGenerator = io.github.kotlinmania.schemars.generate.SchemaGenerator

// port-lint: ignore
package io.github.kotlinmania.schemars

// Tracking file for upstream `src/lib.rs`:
//   `pub use generate::SchemaGenerator;`
//
// The full type lives at `io.github.kotlinmania.schemars.generate.SchemaGenerator`.
// Callers import that defining symbol directly.
//
// Callers migrated:
//   /Volumes/stuff/Projects/kotlinmania/codex-kotlin/src/commonMain/kotlin/io/github/solaceharmony/codex/protocol/ConversationId.kt
//   /Volumes/stuff/Projects/kotlinmania/schemars-kotlin/src/commonMain/kotlin/io/github/kotlinmania/schemars/JsonSchema.kt
//   /Volumes/stuff/Projects/kotlinmania/schemars-kotlin/src/commonMain/kotlin/io/github/kotlinmania/schemars/Macros.kt
//   /Volumes/stuff/Projects/kotlinmania/schemars-kotlin/src/commonMain/kotlin/io/github/kotlinmania/schemars/Ser.kt
//   /Volumes/stuff/Projects/kotlinmania/schemars-kotlin/src/commonMain/kotlin/io/github/kotlinmania/schemars/jsonschemaimpls/Array.kt
//   /Volumes/stuff/Projects/kotlinmania/schemars-kotlin/src/commonMain/kotlin/io/github/kotlinmania/schemars/jsonschemaimpls/Arrayvec07.kt
//   /Volumes/stuff/Projects/kotlinmania/schemars-kotlin/src/commonMain/kotlin/io/github/kotlinmania/schemars/jsonschemaimpls/Bytes1.kt
//   /Volumes/stuff/Projects/kotlinmania/schemars-kotlin/src/commonMain/kotlin/io/github/kotlinmania/schemars/jsonschemaimpls/Chrono04.kt
//   /Volumes/stuff/Projects/kotlinmania/schemars-kotlin/src/commonMain/kotlin/io/github/kotlinmania/schemars/jsonschemaimpls/Core.kt
//   /Volumes/stuff/Projects/kotlinmania/schemars-kotlin/src/commonMain/kotlin/io/github/kotlinmania/schemars/jsonschemaimpls/Decimal.kt
//   /Volumes/stuff/Projects/kotlinmania/schemars-kotlin/src/commonMain/kotlin/io/github/kotlinmania/schemars/jsonschemaimpls/Either1.kt
//   /Volumes/stuff/Projects/kotlinmania/schemars-kotlin/src/commonMain/kotlin/io/github/kotlinmania/schemars/jsonschemaimpls/Ffi.kt
//   /Volumes/stuff/Projects/kotlinmania/schemars-kotlin/src/commonMain/kotlin/io/github/kotlinmania/schemars/jsonschemaimpls/Jiff02.kt
//   /Volumes/stuff/Projects/kotlinmania/schemars-kotlin/src/commonMain/kotlin/io/github/kotlinmania/schemars/jsonschemaimpls/Maps.kt
//   /Volumes/stuff/Projects/kotlinmania/schemars-kotlin/src/commonMain/kotlin/io/github/kotlinmania/schemars/jsonschemaimpls/Nonzero.kt
//   /Volumes/stuff/Projects/kotlinmania/schemars-kotlin/src/commonMain/kotlin/io/github/kotlinmania/schemars/jsonschemaimpls/Primitives.kt
//   /Volumes/stuff/Projects/kotlinmania/schemars-kotlin/src/commonMain/kotlin/io/github/kotlinmania/schemars/jsonschemaimpls/SerdeJson.kt
//   /Volumes/stuff/Projects/kotlinmania/schemars-kotlin/src/commonMain/kotlin/io/github/kotlinmania/schemars/jsonschemaimpls/Semver1.kt
//   /Volumes/stuff/Projects/kotlinmania/schemars-kotlin/src/commonMain/kotlin/io/github/kotlinmania/schemars/jsonschemaimpls/Sequences.kt
//   /Volumes/stuff/Projects/kotlinmania/schemars-kotlin/src/commonMain/kotlin/io/github/kotlinmania/schemars/jsonschemaimpls/StdTime.kt
//   /Volumes/stuff/Projects/kotlinmania/schemars-kotlin/src/commonMain/kotlin/io/github/kotlinmania/schemars/jsonschemaimpls/Tuple.kt
//   /Volumes/stuff/Projects/kotlinmania/schemars-kotlin/src/commonMain/kotlin/io/github/kotlinmania/schemars/jsonschemaimpls/Url2.kt
//   /Volumes/stuff/Projects/kotlinmania/schemars-kotlin/src/commonMain/kotlin/io/github/kotlinmania/schemars/jsonschemaimpls/Uuid1.kt
//   /Volumes/stuff/Projects/kotlinmania/schemars-kotlin/src/commonMain/kotlin/io/github/kotlinmania/schemars/private/Mod.kt

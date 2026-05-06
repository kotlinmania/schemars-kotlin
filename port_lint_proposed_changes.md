# port-lint Proposed Changes

**Generated:** 2026-05-06
**Source:** tmp/schemars/src
**Target:** src/commonMain/kotlin/io/github/kotlinmania/schemars

These are review proposals only. They are emitted when a Rust -> Kotlin pair matches only after fallback normalization, so the existing `port-lint` header is not an exact provenance match.

| Target file | Current header | Proposed header | Source path | Reason |
|-------------|----------------|-----------------|-------------|--------|
| `src/commonMain/kotlin/io/github/kotlinmania/schemars/Schema.kt` | `// port-lint: source src/schema.rs` | `// port-lint: source schema.rs` | `schema.rs` | `port-lint provenance header matched only after fallback normalization: 'src/schema.rs' vs expected 'schema.rs'` |
| `src/commonMain/kotlin/io/github/kotlinmania/schemars/SchemaGenerator.kt` | `// port-lint: source src/generate.rs` | `// port-lint: source generate.rs` | `generate.rs` | `port-lint provenance header matched only after fallback normalization: 'src/generate.rs' vs expected 'generate.rs'` |
| `src/commonMain/kotlin/io/github/kotlinmania/schemars/JsonSchema.kt` | `// port-lint: source src/lib.rs` | `// port-lint: source lib.rs` | `lib.rs` | `port-lint provenance header matched only after fallback normalization: 'src/lib.rs' vs expected 'lib.rs'` |
| `src/commonMain/kotlin/io/github/kotlinmania/schemars/Primitives.kt` | `// port-lint: source src/json_schema_impls/primitives.rs` | `// port-lint: source json_schema_impls/primitives.rs` | `json_schema_impls/primitives.rs` | `port-lint provenance header matched only after fallback normalization: 'src/json_schema_impls/primitives.rs' vs expected 'json_schema_impls/primitives.rs'` |

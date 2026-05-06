# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 4/34 (11.8%)
- **Function parity:** 2/201 matched (target 2) — 1.0%
- **Class/type parity:** 3/34 matched (target 8) — 8.8%
- **Combined symbol parity:** 5/235 matched (target 10) — 2.1%
- **Average inline-code cosine:** 0.00 (function body across 3 matched files)
- **Average documentation cosine:** 0.35 (doc text across 3 matched files)
- **Cheat-zeroed Files:** 4
- **Critical Issues:** 4 files with <0.60 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

No incomplete high-dependency files detected.

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. schema

- **Target:** `schemars.Schema [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 2
- **Priority Score:** 2293010.0
- **Functions:** 0/27 matched (target 0)
- **Missing functions:** `deserialize`, `serialize`, `eq`, `new_ref`, `as_value`, `as_bool`, `as_object`, `as_object_mut`, `try_to_object`, `try_as_object_mut`, `to_value`, `ensure_object`, `insert`, `get`, `get_mut`, `pointer`, `pointer_mut`, `remove`, `has_type`, `validate`, `from`, `try_from`, `default`, `schema_name`, `schema_id`, `json_schema`, `serialize_schema_property`
- **Types:** 1/3 matched (target 5)
- **Missing types:** `Error`, `OrderedKeywordWrapper`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/schema.rs` vs expected `schema.rs`
- **Proposed provenance header:** `// port-lint: source schema.rs` (current: `// port-lint: source src/schema.rs`)
- **Lint issues:** 1

### 2. generate

- **Target:** `schemars.SchemaGenerator [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 505110.0
- **Functions:** 0/44 matched (target 0)
- **Missing functions:** `default`, `draft07`, `draft2019_09`, `draft2020_12`, `openapi3`, `with`, `with_transform`, `into_generator`, `for_deserialize`, `for_serialize`, `is_deserialize`, `is_serialize`, `clone`, `from`, `new`, `settings`, `subschema_for`, `find_ref`, `insert_new_subschema_for`, `definitions`, `definitions_mut`, `take_definitions`, `transforms_mut`, `root_schema_for`, `into_root_schema_for`, `root_schema_for_value`, `into_root_schema_for_value`, `contract`, `json_schema_internal`, `add_definitions`, `apply_transforms`, `definitions_path_stripped`, `schema_uid`, `json_pointer_mut`, `is`, `downcast_ref`, `downcast_mut`, `downcast`, `_as_any`, `_as_any_mut`, `_into_any`, `fmt`, `_assert_send`, `assert`
- **Types:** 1/7 matched (target 1)
- **Missing types:** `CowStr`, `SchemaSettings`, `Contract`, `SchemaUid`, `FindRef`, `GenTransform`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/generate.rs` vs expected `generate.rs`
- **Proposed provenance header:** `// port-lint: source generate.rs` (current: `// port-lint: source src/generate.rs`)
- **Lint issues:** 1

### 3. lib

- **Target:** `schemars.JsonSchema [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 40510.0
- **Functions:** 0/4 matched (target 0)
- **Missing functions:** `inline_schema`, `schema_id`, `_schemars_private_non_optional_json_schema`, `_schemars_private_is_option`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/lib.rs` vs expected `lib.rs`
- **Proposed provenance header:** `// port-lint: source lib.rs` (current: `// port-lint: source src/lib.rs`)
- **Lint issues:** 1

### 4. json_schema_impls.primitives

- **Target:** `schemars.Primitives [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10310.0
- **Functions:** 2/3 matched (target 2)
- **Missing functions:** `schema_id`
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/json_schema_impls/primitives.rs` vs expected `json_schema_impls/primitives.rs`
- **Proposed provenance header:** `// port-lint: source json_schema_impls/primitives.rs` (current: `// port-lint: source src/json_schema_impls/primitives.rs`)
- **Lint issues:** 1

## Success Criteria

For each file to be considered "complete":
- **Similarity ≥ 0.85** (Excellent threshold)
- All public APIs ported
- All tests ported
- Documentation ported
- port-lint header present

## Next Commands

```bash
# Initialize task queue for systematic porting
cd tools/ast_distance
./ast_distance --init-tasks ../../tmp/schemars/src rust ../../src/commonMain/kotlin/io/github/kotlinmania/schemars kotlin tasks.json ../../AGENTS.md

# Get next high-priority task
./ast_distance --assign tasks.json <agent-id>
```
## Reexport / Wiring Modules

These files match `reexport_modules` patterns in `.ast_distance_config.json`. They are filtered out of
normal priority and missing-file ladders because they are wiring
modules, not direct logic ports. Consult them for call-site routing;
do not treat them as the next implementation target by default.

### Missing

| Source | Expected target | Deps | Source path | Expected path |
|--------|-----------------|------|-------------|---------------|
| `_private.mod` | `private.Mod` | 0 | `_private/mod.rs` | `private/Mod.kt` |
| `json_schema_impls.mod` | `jsonschemaimpls.Mod` | 0 | `json_schema_impls/mod.rs` | `jsonschemaimpls/Mod.kt` |


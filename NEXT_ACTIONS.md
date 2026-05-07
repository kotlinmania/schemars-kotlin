# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 34/34 (100.0%)
- **Function parity:** 154/181 matched (target 309) — 85.1%
- **Class/type parity:** 27/39 matched (target 65) — 69.2%
- **Combined symbol parity:** 181/220 matched (target 374) — 82.3%
- **Average inline-code cosine:** 0.47 (function body across 32 matched files)
- **Average documentation cosine:** 0.19 (doc text across 32 matched files)
- **Cheat-zeroed Files:** 9
- **Critical Issues:** 24 files with <0.60 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

No incomplete high-dependency files detected.

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. schema

- **Target:** `schemars.Schema`
- **Similarity:** 0.37
- **Dependents:** 2
- **Priority Score:** 2093006.4
- **Functions:** 20/27 matched (target 26)
- **Missing functions:** `deserialize`, `serialize`, `eq`, `schema_name`, `schema_id`, `json_schema`, `serialize_schema_property`
- **Types:** 1/3 matched (target 5)
- **Missing types:** `Error`, `OrderedKeywordWrapper`

### 2. generate

- **Target:** `generate.Generate`
- **Similarity:** 0.54
- **Dependents:** 0
- **Priority Score:** 155104.6
- **Functions:** 31/44 matched (target 38)
- **Missing functions:** `clone`, `from`, `new`, `is`, `downcast_ref`, `downcast_mut`, `downcast`, `_as_any`, `_as_any_mut`, `_into_any`, `fmt`, `_assert_send`, `assert`
- **Types:** 5/7 matched (target 6)
- **Missing types:** `CowStr`, `GenTransform`

### 3. ser

- **Target:** `schemars.Ser`
- **Similarity:** 0.44
- **Dependents:** 0
- **Priority Score:** 73105.6
- **Functions:** 20/21 matched (target 44)
- **Missing functions:** `end`
- **Types:** 4/10 matched (target 5)
- **Missing types:** `Ok`, `Error`, `SerializeTupleStruct`, `SerializeTupleVariant`, `SerializeStruct`, `SerializeStructVariant`

### 4. _private.mod

- **Target:** `private.Mod [STUB]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 32710.0
- **Functions:** 21/22 matched
- **Missing functions:** `nested_option_schemas`
- **Types:** 3/5 matched (target 3)
- **Missing types:** `NoSerialize`, `NoJsonSchema`
- **Tests:** 0/1 matched

### 5. transform

- **Target:** `transform.Transform`
- **Similarity:** 0.65
- **Dependents:** 0
- **Priority Score:** 21803.5
- **Functions:** 4/6 matched (target 19)
- **Missing functions:** `default`, `restrict_formats`
- **Types:** 12/12 matched
- **Missing types:** _none_
- **Tests:** 0/1 matched

### 6. _private.rustdoc

- **Target:** `private.Rustdoc`
- **Similarity:** 0.51
- **Dependents:** 0
- **Priority Score:** 20604.9
- **Functions:** 4/6 matched (target 5)
- **Missing functions:** `subslice`, `to_utf8`
- **Types:** 0/0 matched
- **Missing types:** _none_

### 7. json_schema_impls.atomic

- **Target:** `jsonschemaimpls.Atomic [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10110.0
- **Functions:** 0/1 matched (target 0)
- **Missing functions:** `schema_for_atomics`
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Tests:** 0/1 matched

### 8. encoding

- **Target:** `schemars.Encoding`
- **Similarity:** 0.59
- **Dependents:** 0
- **Priority Score:** 504.1
- **Functions:** 5/5 matched (target 6)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 2/2 matched

### 9. json_schema_impls.core

- **Target:** `jsonschemaimpls.Core`
- **Similarity:** 0.66
- **Dependents:** 0
- **Priority Score:** 503.4
- **Functions:** 5/5 matched (target 15)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 5)
- **Missing types:** _none_

### 10. lib

- **Target:** `schemars.JsonSchema`
- **Similarity:** 0.91
- **Dependents:** 0
- **Priority Score:** 500.9
- **Functions:** 4/4 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 11. json_schema_impls.maps

- **Target:** `jsonschemaimpls.Maps`
- **Similarity:** 0.76
- **Dependents:** 0
- **Priority Score:** 402.4
- **Functions:** 3/3 matched (target 5)
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_

### 12. json_schema_impls.bytes1

- **Target:** `jsonschemaimpls.Bytes1`
- **Similarity:** 0.37
- **Dependents:** 0
- **Priority Score:** 306.3
- **Functions:** 3/3 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_

### 13. json_schema_impls.ffi

- **Target:** `jsonschemaimpls.Ffi`
- **Similarity:** 0.39
- **Dependents:** 0
- **Priority Score:** 306.1
- **Functions:** 3/3 matched (target 6)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 2)
- **Missing types:** _none_

### 14. json_schema_impls.chrono04

- **Target:** `jsonschemaimpls.Chrono04`
- **Similarity:** 0.43
- **Dependents:** 0
- **Priority Score:** 305.7
- **Functions:** 3/3 matched (target 13)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 2)
- **Missing types:** _none_

### 15. json_schema_impls.primitives

- **Target:** `jsonschemaimpls.Primitives`
- **Similarity:** 0.44
- **Dependents:** 0
- **Priority Score:** 305.6
- **Functions:** 3/3 matched (target 24)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_

### 16. json_schema_impls.array

- **Target:** `jsonschemaimpls.Array`
- **Similarity:** 0.45
- **Dependents:** 0
- **Priority Score:** 305.5
- **Functions:** 3/3 matched (target 8)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 2)
- **Missing types:** _none_

### 17. json_schema_impls.url2

- **Target:** `jsonschemaimpls.Url2`
- **Similarity:** 0.45
- **Dependents:** 0
- **Priority Score:** 305.5
- **Functions:** 3/3 matched (target 4)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_

### 18. json_schema_impls.uuid1

- **Target:** `jsonschemaimpls.Uuid1`
- **Similarity:** 0.45
- **Dependents:** 0
- **Priority Score:** 305.5
- **Functions:** 3/3 matched (target 4)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_

### 19. json_schema_impls.semver1

- **Target:** `jsonschemaimpls.Semver1`
- **Similarity:** 0.45
- **Dependents:** 0
- **Priority Score:** 305.5
- **Functions:** 3/3 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_

### 20. json_schema_impls.std_time

- **Target:** `jsonschemaimpls.StdTime`
- **Similarity:** 0.46
- **Dependents:** 0
- **Priority Score:** 305.4
- **Functions:** 3/3 matched (target 6)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 2)
- **Missing types:** _none_

### 21. json_schema_impls.either1

- **Target:** `jsonschemaimpls.Either1`
- **Similarity:** 0.65
- **Dependents:** 0
- **Priority Score:** 303.5
- **Functions:** 3/3 matched (target 4)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_

### 22. _private.regex_syntax

- **Target:** `private.RegexSyntax`
- **Similarity:** 0.84
- **Dependents:** 0
- **Priority Score:** 301.6
- **Functions:** 3/3 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 23. json_schema_impls.serdejson

- **Target:** `jsonschemaimpls.SerdeJson`
- **Similarity:** 0.46
- **Dependents:** 0
- **Priority Score:** 205.4
- **Functions:** 2/2 matched (target 6)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 2)
- **Missing types:** _none_

### 24. json_schema_impls.arrayvec07

- **Target:** `jsonschemaimpls.Arrayvec07`
- **Similarity:** 0.78
- **Dependents:** 0
- **Priority Score:** 202.2
- **Functions:** 2/2 matched (target 3)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_

### 25. json_schema_impls.nonzero

- **Target:** `jsonschemaimpls.Nonzero [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched (target 10)
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 26. json_schema_impls.decimal

- **Target:** `jsonschemaimpls.Decimal [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched (target 4)
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 27. json_schema_impls.jiff02

- **Target:** `jsonschemaimpls.Jiff02 [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched (target 5)
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 28. json_schema_impls.mod

- **Target:** `jsonschemaimpls.Mod [STUB]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched (target 1)
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 29. json_schema_impls.sequences

- **Target:** `jsonschemaimpls.Sequences [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched (target 8)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 2)
- **Missing types:** _none_

### 30. macros

- **Target:** `schemars.Macros [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched (target 6)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_

### 31. json_schema_impls.tuple

- **Target:** `jsonschemaimpls.Tuple [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched (target 4)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_

### 32. json_schema_impls.wrapper

- **Target:** `jsonschemaimpls.Wrapper`
- **Similarity:** 1.00
- **Dependents:** 0
- **Priority Score:** 0.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_

### 33. json_schema_impls.indexmap2

- **Target:** `jsonschemaimpls.Indexmap2`
- **Similarity:** 1.00
- **Dependents:** 0
- **Priority Score:** 0.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 2)
- **Missing types:** _none_

### 34. consts

- **Target:** `schemars.Consts`
- **Similarity:** 1.00
- **Dependents:** 0
- **Priority Score:** 0.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_

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

// port-lint: source json_schema_impls/mod.rs
package io.github.kotlinmania.schemars.jsonschemaimpls

/*
 * The upstream `mod.rs` declares submodules and the helper macros `inline_schema!` and
 * `forward_impl!`. The Kotlin port:
 *
 *   - inline_schema!() macro: each implementing object overrides `inlineSchema()` to return
 *     `true` directly. There is no shared abstract carrier; AGENTS.md forbids re-export
 *     typealiases at root packages.
 *
 *   - forward_impl!(impl => target) macro: each Kotlin file that ports a forward impl
 *     either delegates with `class FooSchema(...) : JsonSchema by FooDelegate` or, for
 *     unparameterised aliases, exposes a top-level `val FooSchema: JsonSchema = TargetSchema`.
 *
 * Per the workspace re-export rule, this file does NOT introduce typealiases. It is kept as
 * the tracking ledger for the upstream module's `pub use` lines.
 *
 * Module declarations (mirrored as Kotlin files inside this package):
 *   - mod array;        -> Array.kt (EmptyArraySchema, FixedArraySchema)
 *   - mod core;         -> Core.kt (OptionSchema, ResultSchema, BoundSchema, RangeSchema)
 *   - mod maps;         -> Maps.kt (MapSchema)
 *   - mod nonzero;      -> Nonzero.kt (NonZeroI{8,16,32,64,128} / NonZeroU{8,16,32,64,128} / NonZeroIsize / NonZeroUsize)
 *   - mod primitives;   -> Primitives.kt (StrSchema, StringSchema, BoolSchema, ...)
 *   - mod sequences;    -> Sequences.kt (SeqSchema, SetSchema)
 *   - mod serdejson;    -> SerdeJson.kt (ValueSchema, JsonNumberSchema, JsonMapSchema, RawValueSchema)
 *   - mod std_time;     -> StdTime.kt (DurationSchema, SystemTimeSchema)
 *   - mod tuple;        -> Tuple.kt (TupleSchema)
 *   - mod wrapper;      -> Wrapper.kt (WrapperSchema)
 *   - mod atomic;       -> Atomic.kt (AtomicBool, AtomicI{8,16,32,64}, AtomicU{8,16,32,64}, AtomicIsize, AtomicUsize schemas)
 *
 * Feature-gated upstream modules:
 *   - mod ffi;          -> Ffi.kt (OsStringSchema, CStringSchema, OsStrSchema, CStrSchema)
 *   - mod arrayvec07;   -> Arrayvec07.kt (ArrayStringSchema, ArrayVecSchema)
 *   - mod bytes1;       -> Bytes1.kt (BytesSchema, BytesMutSchema)
 *   - mod chrono04;     -> Chrono04.kt (WeekdaySchema, TimeDeltaSchema, NaiveDate/Time/DateTime, ChronoDateTimeSchema)
 *   - mod decimal;      -> Decimal.kt (RustDecimalSchema, BigDecimalSchema)
 *   - mod either1;      -> Either1.kt (EitherSchema)
 *   - mod indexmap2;    -> Indexmap2.kt (IndexMapSchema, IndexSetSchema)
 *   - mod jiff02;       -> Jiff02.kt (SignedDuration, Span, Timestamp, Zoned, Date, Time, DateTime)
 *   - mod semver1;      -> Semver1.kt (SemverSchema)
 *   - mod url2;         -> Url2.kt (UrlSchema)
 *   - mod uuid1;        -> Uuid1.kt (UuidSchema)
 *
 * Inline `forward_impl!` calls in the upstream mod.rs (rather than separate files):
 *   - smallvec1::SmallVec → SeqSchema(elementSchema)
 *   - smol_str02::SmolStr → StringSchema
 *   - smol_str03::SmolStr → StringSchema
 *
 * Re-exports the module would otherwise carry are intentionally absent so callers reach the
 * concrete target `*Schema` value directly. (See workspace memory: "Re-export migration:
 * search Rust first" — Kotlin's no-typealias rule applies to root re-exports.)
 *
 * Callers migrated:
 *   - (none yet — Kotlin call sites land directly on the target `*Schema` from day one).
 *
 * Projected callers (Rust):
 *   - All sibling `-kotlin` repos' `tmp` Rust trees that `use schemars::JsonSchema;` — when those crates are
 *     ported, they import the target `*Schema` from this same package directly.
 */

/** `forward_impl!((<A: smallvec1::Array> JsonSchema for SmallVec<A>) => Vec<A::Item>);`. */
@Suppress("FunctionName")
fun SmallVecSchema(element: io.github.kotlinmania.schemars.JsonSchema): io.github.kotlinmania.schemars.JsonSchema =
    SeqSchema(element)

/** `forward_impl!(smol_str02::SmolStr => String);`. */
val SmolStr02Schema: io.github.kotlinmania.schemars.JsonSchema = StringSchema

/** `forward_impl!(smol_str03::SmolStr => String);`. */
val SmolStr03Schema: io.github.kotlinmania.schemars.JsonSchema = StringSchema

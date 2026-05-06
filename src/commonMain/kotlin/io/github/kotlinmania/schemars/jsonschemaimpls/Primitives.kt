// port-lint: source json_schema_impls/primitives.rs
package io.github.kotlinmania.schemars.jsonschemaimpls

import io.github.kotlinmania.schemars.JsonSchema
import io.github.kotlinmania.schemars.Schema
import io.github.kotlinmania.schemars.SchemaGenerator
import io.github.kotlinmania.schemars.jsonSchema

/*
 * Each upstream `simple_impl!`, `ranged_impl!`, and `unsigned_impl!` macro produces an
 * `impl JsonSchema for $type { ... }`. Kotlin doesn't allow extension implementations of an
 * interface for built-in types, so each Rust primitive maps to a singleton JsonSchema object.
 * Call sites that mirror Rust's `<String>::json_schema(gen)` write `StringSchema.jsonSchema(gen)`.
 */

private fun simple(name: String, instanceType: String): JsonSchema = object : JsonSchema {
    override fun inlineSchema(): Boolean = true
    override fun schemaName(): String = name
    override fun jsonSchema(generator: SchemaGenerator): Schema = jsonSchema {
        "type" to instanceType
    }
}

private fun simpleFormat(name: String, instanceType: String, format: String): JsonSchema = object : JsonSchema {
    override fun inlineSchema(): Boolean = true
    override fun schemaName(): String = name
    override fun jsonSchema(generator: SchemaGenerator): Schema = jsonSchema {
        "type" to instanceType
        "format" to format
    }
}

private fun ranged(
    name: String,
    instanceType: String,
    format: String,
    min: Long,
    max: Long,
): JsonSchema = object : JsonSchema {
    override fun inlineSchema(): Boolean = true
    override fun schemaName(): String = name
    override fun jsonSchema(generator: SchemaGenerator): Schema = jsonSchema {
        "type" to instanceType
        "format" to format
        "minimum" to min
        "maximum" to max
    }
}

private fun rangedUnsigned(
    name: String,
    instanceType: String,
    format: String,
    max: ULong,
): JsonSchema = object : JsonSchema {
    override fun inlineSchema(): Boolean = true
    override fun schemaName(): String = name
    override fun jsonSchema(generator: SchemaGenerator): Schema = jsonSchema {
        "type" to instanceType
        "format" to format
        "minimum" to 0
        "maximum" to max.toLong()
    }
}

private fun unsigned(name: String, instanceType: String, format: String): JsonSchema = object : JsonSchema {
    override fun inlineSchema(): Boolean = true
    override fun schemaName(): String = name
    override fun jsonSchema(generator: SchemaGenerator): Schema = jsonSchema {
        "type" to instanceType
        "format" to format
        "minimum" to 0
    }
}

// `simple_impl!(str => "string");` and `simple_impl!(String => "string");`
val StrSchema: JsonSchema = simple("string", "string")
val StringSchema: JsonSchema = simple("string", "string")
// `simple_impl!(bool => "boolean");`
val BoolSchema: JsonSchema = simple("boolean", "boolean")
// `simple_impl!(f32 => "number", "float");`
val F32Schema: JsonSchema = simpleFormat("float", "number", "float")
// `simple_impl!(f64 => "number", "double");`
val F64Schema: JsonSchema = simpleFormat("double", "number", "double")
// `ranged_impl!(i8 => "integer", "int8");`
val I8Schema: JsonSchema = ranged("int8", "integer", "int8", Byte.MIN_VALUE.toLong(), Byte.MAX_VALUE.toLong())
// `ranged_impl!(i16 => "integer", "int16");`
val I16Schema: JsonSchema = ranged("int16", "integer", "int16", Short.MIN_VALUE.toLong(), Short.MAX_VALUE.toLong())
// `simple_impl!(i32 => "integer", "int32");`
val I32Schema: JsonSchema = simpleFormat("int32", "integer", "int32")
// `simple_impl!(i64 => "integer", "int64");`
val I64Schema: JsonSchema = simpleFormat("int64", "integer", "int64")
// `simple_impl!(i128 => "integer", "int128");` — Kotlin has no native i128; use the format only.
val I128Schema: JsonSchema = simpleFormat("int128", "integer", "int128")
// `simple_impl!(isize => "integer", "int");` — isize maps to Long on 64-bit platforms.
val IsizeSchema: JsonSchema = simpleFormat("int", "integer", "int")
// `simple_impl!(() => "null");`
val UnitSchema: JsonSchema = simple("null", "null")

// `ranged_impl!(u8 => "integer", "uint8");`
val U8Schema: JsonSchema = rangedUnsigned("uint8", "integer", "uint8", UByte.MAX_VALUE.toULong())
// `ranged_impl!(u16 => "integer", "uint16");`
val U16Schema: JsonSchema = rangedUnsigned("uint16", "integer", "uint16", UShort.MAX_VALUE.toULong())
// `unsigned_impl!(u32 => "integer", "uint32");`
val U32Schema: JsonSchema = unsigned("uint32", "integer", "uint32")
// `unsigned_impl!(u64 => "integer", "uint64");`
val U64Schema: JsonSchema = unsigned("uint64", "integer", "uint64")
// `unsigned_impl!(u128 => "integer", "uint128");`
val U128Schema: JsonSchema = unsigned("uint128", "integer", "uint128")
// `unsigned_impl!(usize => "integer", "uint");`
val UsizeSchema: JsonSchema = unsigned("uint", "integer", "uint")

/**
 * `impl JsonSchema for char` — single-character string with explicit length bounds.
 *
 * The Rust `char` type holds a single Unicode scalar value. Kotlin's `Char` is a UTF-16 code
 * unit, but the schema constraint (string length 1-1) is identical.
 */
object CharSchema : JsonSchema {
    override fun inlineSchema(): Boolean = true
    override fun schemaName(): String = "Character"
    override fun schemaId(): String = "char"
    override fun jsonSchema(generator: SchemaGenerator): Schema = jsonSchema {
        "type" to "string"
        "minLength" to 1
        "maxLength" to 1
    }
}

/*
 * `#[cfg(feature = "std")] mod std_types`: These are gated on the `std` Cargo feature in upstream.
 * Their Kotlin equivalents live in this same file (Kotlin's `commonMain` always has the std lib).
 */

// `simple_impl!(Path => "string");` and `simple_impl!(PathBuf => "string");`
val PathSchema: JsonSchema = simple("string", "string")
val PathBufSchema: JsonSchema = simple("string", "string")

// `simple_impl!(Ipv4Addr => "string", "ipv4");`
val Ipv4AddrSchema: JsonSchema = simpleFormat("ipv4", "string", "ipv4")
// `simple_impl!(Ipv6Addr => "string", "ipv6");`
val Ipv6AddrSchema: JsonSchema = simpleFormat("ipv6", "string", "ipv6")
// `simple_impl!(IpAddr => "string", "ip");`
val IpAddrSchema: JsonSchema = simpleFormat("ip", "string", "ip")

// `simple_impl!(SocketAddr => "string");`
val SocketAddrSchema: JsonSchema = simple("string", "string")
// `simple_impl!(SocketAddrV4 => "string");`
val SocketAddrV4Schema: JsonSchema = simple("string", "string")
// `simple_impl!(SocketAddrV6 => "string");`
val SocketAddrV6Schema: JsonSchema = simple("string", "string")

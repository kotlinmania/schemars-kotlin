// port-lint: source json_schema_impls/primitives.rs
package io.github.kotlinmania.schemars.jsonschemaimpls

import io.github.kotlinmania.schemars.JsonSchema
import io.github.kotlinmania.schemars.Schema
import io.github.kotlinmania.schemars.generate.SchemaGenerator
import io.github.kotlinmania.schemars.jsonSchema

private fun simple(name: String, instanceType: String): JsonSchema = object : JsonSchema {
    override fun inlineSchema(): Boolean = true
    override fun schemaName(): String = name
    override fun jsonSchema(generator: SchemaGenerator): Schema = jsonSchema {
        this["type"] = instanceType
    }
}

private fun simpleFormat(name: String, instanceType: String, format: String): JsonSchema = object : JsonSchema {
    override fun inlineSchema(): Boolean = true
    override fun schemaName(): String = name
    override fun jsonSchema(generator: SchemaGenerator): Schema = jsonSchema {
        this["type"] = instanceType
        this["format"] = format
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
        this["type"] = instanceType
        this["format"] = format
        this["minimum"] = min
        this["maximum"] = max
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
        this["type"] = instanceType
        this["format"] = format
        this["minimum"] = 0
        this["maximum"] = max.toLong()
    }
}

private fun unsigned(name: String, instanceType: String, format: String): JsonSchema = object : JsonSchema {
    override fun inlineSchema(): Boolean = true
    override fun schemaName(): String = name
    override fun jsonSchema(generator: SchemaGenerator): Schema = jsonSchema {
        this["type"] = instanceType
        this["format"] = format
        this["minimum"] = 0
    }
}

val StrSchema: JsonSchema = simple("string", "string")
val StringSchema: JsonSchema = simple("string", "string")
val BoolSchema: JsonSchema = simple("boolean", "boolean")
val F32Schema: JsonSchema = simpleFormat("float", "number", "float")
val F64Schema: JsonSchema = simpleFormat("double", "number", "double")
val I8Schema: JsonSchema = ranged("int8", "integer", "int8", Byte.MIN_VALUE.toLong(), Byte.MAX_VALUE.toLong())
val I16Schema: JsonSchema = ranged("int16", "integer", "int16", Short.MIN_VALUE.toLong(), Short.MAX_VALUE.toLong())
val I32Schema: JsonSchema = simpleFormat("int32", "integer", "int32")
val I64Schema: JsonSchema = simpleFormat("int64", "integer", "int64")
val I128Schema: JsonSchema = simpleFormat("int128", "integer", "int128")
val IsizeSchema: JsonSchema = simpleFormat("int", "integer", "int")
val UnitSchema: JsonSchema = simple("null", "null")

val U8Schema: JsonSchema = rangedUnsigned("uint8", "integer", "uint8", UByte.MAX_VALUE.toULong())
val U16Schema: JsonSchema = rangedUnsigned("uint16", "integer", "uint16", UShort.MAX_VALUE.toULong())
val U32Schema: JsonSchema = unsigned("uint32", "integer", "uint32")
val U64Schema: JsonSchema = unsigned("uint64", "integer", "uint64")
val U128Schema: JsonSchema = unsigned("uint128", "integer", "uint128")
val UsizeSchema: JsonSchema = unsigned("uint", "integer", "uint")

object CharSchema : JsonSchema {
    override fun inlineSchema(): Boolean = true
    override fun schemaName(): String = "Character"
    override fun schemaId(): String = "char"
    override fun jsonSchema(generator: SchemaGenerator): Schema = jsonSchema {
        this["type"] = "string"
        this["minLength"] = 1
        this["maxLength"] = 1
    }
}

val PathSchema: JsonSchema = simple("string", "string")
val PathBufSchema: JsonSchema = simple("string", "string")

val Ipv4AddrSchema: JsonSchema = simpleFormat("ipv4", "string", "ipv4")
val Ipv6AddrSchema: JsonSchema = simpleFormat("ipv6", "string", "ipv6")
val IpAddrSchema: JsonSchema = simpleFormat("ip", "string", "ip")

val SocketAddrSchema: JsonSchema = simple("string", "string")
val SocketAddrV4Schema: JsonSchema = simple("string", "string")
val SocketAddrV6Schema: JsonSchema = simple("string", "string")

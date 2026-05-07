// port-lint: source json_schema_impls/semver1.rs
package io.github.kotlinmania.schemars.jsonschemaimpls

import io.github.kotlinmania.schemars.JsonSchema
import io.github.kotlinmania.schemars.Schema
import io.github.kotlinmania.schemars.SchemaGenerator
import io.github.kotlinmania.schemars.jsonSchema

object SemverSchema : JsonSchema {
    override fun schemaName(): String = "SemVer"
    override fun schemaId(): String = "semver::Version"
    override fun jsonSchema(generator: SchemaGenerator): Schema = jsonSchema {
        this["type"] = "string"
        // https://semver.org/#is-there-a-suggested-regular-expression-regex-to-check-a-semver-string
        this["pattern"] =
            "^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)" +
            "(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)" +
            "(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?" +
            "(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?\$"
    }
}

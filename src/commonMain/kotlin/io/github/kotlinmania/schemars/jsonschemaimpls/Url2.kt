// port-lint: source json_schema_impls/url2.rs
package io.github.kotlinmania.schemars.jsonschemaimpls

import io.github.kotlinmania.schemars.JsonSchema
import io.github.kotlinmania.schemars.Schema
import io.github.kotlinmania.schemars.SchemaGenerator
import io.github.kotlinmania.schemars.jsonSchema

object UrlSchema : JsonSchema {
    override fun inlineSchema(): Boolean = true
    override fun schemaName(): String = "Url"
    override fun schemaId(): String = "url::Url"
    override fun jsonSchema(generator: SchemaGenerator): Schema = jsonSchema {
        this["type"] = "string"
        this["format"] = "uri"
    }
}

// port-lint: source json_schema_impls/wrapper.rs
package io.github.kotlinmania.schemars.jsonschemaimpls

import io.github.kotlinmania.schemars.JsonSchema

/** Schema for a thin wrapper around an inner type — the schema is the inner type's schema. */
class WrapperSchema(
    inner: JsonSchema,
) : JsonSchema by inner

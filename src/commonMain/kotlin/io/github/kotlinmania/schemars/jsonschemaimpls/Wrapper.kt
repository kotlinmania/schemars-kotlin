// port-lint: source json_schema_impls/wrapper.rs
package io.github.kotlinmania.schemars.jsonschemaimpls

import io.github.kotlinmania.schemars.JsonSchema

/*
 * `wrapper_impl!` produces forward impls that delegate to the inner type's schema for:
 *   `&'a T`, `&'a mut T`, `Box<T>`, `Rc<T>`, `Weak<T>`, `Arc<T>`, `Mutex<T>`, `RwLock<T>`,
 *   `Cell<T>`, `RefCell<T>`, `Cow<'a, T>`, `Wrapping<T>`, `Reverse<T>`.
 *
 * Per the workspace porting rules, `Box<T>`/`Rc<T>`/`Arc<T>`/etc. collapse to bare `T`
 * references in Kotlin (the GC subsumes them). Their schema is identical to the inner
 * type's — so `WrapperSchema(inner)` is just an alias.
 */

/** Equivalent to `forward_impl!(... => T)` for any of the upstream wrapper types. */
class WrapperSchema(inner: JsonSchema) : JsonSchema by inner

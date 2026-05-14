// port-lint: source encoding.rs
package io.github.kotlinmania.schemars

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class EncodingTest {
    @Test
    fun testEncodeRefName() {
        assertEquals("Simple!", encodeRefName("Simple!"))
        assertEquals(
            "Needs%20%25-encoding%20%F0%9F%9A%80",
            encodeRefName("Needs %-encoding 🚀"),
        )
        assertEquals(
            "aA0-._!\$&'()*+,;=:@?",
            encodeRefName("aA0-._!\$&'()*+,;=:@?"),
        )
        assertEquals("%22%C2%A3%25%5E%5C~0~1", encodeRefName("\"£%^\\~/"))
    }

    @Test
    fun testPercentDecode() {
        assertEquals("Simple!", percentDecode("Simple!"))
        assertEquals("Needs %-encoding 🚀", percentDecode("Needs %-encoding 🚀"))
        assertEquals("Needs %-encoding 🚀", percentDecode("Needs%20%25-encoding%20%F0%9F%9A%80"))
        assertEquals("aA0-._!\$&'()*+,;=:@?", percentDecode("aA0-._!\$&'()*+,;=:@?"))
        assertEquals("\"£%^\\~/", percentDecode("\"£%^\\~/"))
        assertEquals("\"£%^\\~0~1", percentDecode("%22%C2%A3%25%5E%5C~0~1"))
        assertEquals("%% 20%%%", percentDecode("%%%2020%%%"))
        assertEquals("🚀", percentDecode("%f0%9F%9a%80"))
        assertNull(percentDecode("%F0%9F%9A"))
    }
}

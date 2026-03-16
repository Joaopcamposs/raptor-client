package com.raptorclient.models

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class HttpMethodTest {
    @Nested
    inner class FromString {
        @Test
        fun `should parse GET method`() {
            assertEquals(HttpMethod.GET, HttpMethod.fromString("GET"))
        }

        @Test
        fun `should parse POST method`() {
            assertEquals(HttpMethod.POST, HttpMethod.fromString("POST"))
        }

        @Test
        fun `should parse PUT method`() {
            assertEquals(HttpMethod.PUT, HttpMethod.fromString("PUT"))
        }

        @Test
        fun `should parse DELETE method`() {
            assertEquals(HttpMethod.DELETE, HttpMethod.fromString("DELETE"))
        }

        @Test
        fun `should parse PATCH method`() {
            assertEquals(HttpMethod.PATCH, HttpMethod.fromString("PATCH"))
        }

        @Test
        fun `should parse HEAD method`() {
            assertEquals(HttpMethod.HEAD, HttpMethod.fromString("HEAD"))
        }

        @Test
        fun `should parse OPTIONS method`() {
            assertEquals(HttpMethod.OPTIONS, HttpMethod.fromString("OPTIONS"))
        }

        @Test
        fun `should be case insensitive`() {
            assertEquals(HttpMethod.GET, HttpMethod.fromString("get"))
            assertEquals(HttpMethod.POST, HttpMethod.fromString("post"))
            assertEquals(HttpMethod.PUT, HttpMethod.fromString("Put"))
        }

        @Test
        fun `should default to GET for unknown method`() {
            assertEquals(HttpMethod.GET, HttpMethod.fromString("UNKNOWN"))
            assertEquals(HttpMethod.GET, HttpMethod.fromString(""))
        }
    }

    @Nested
    inner class Properties {
        @Test
        fun `should have correct display names`() {
            assertEquals("GET", HttpMethod.GET.displayName)
            assertEquals("POST", HttpMethod.POST.displayName)
            assertEquals("DELETE", HttpMethod.DELETE.displayName)
        }

        @Test
        fun `should have valid hex color codes`() {
            for (method in HttpMethod.entries) {
                assertTrue(method.color.matches(Regex("^#[0-9A-Fa-f]{6}$"))) {
                    "${method.name} has invalid color: ${method.color}"
                }
            }
        }

        @Test
        fun `should have all seven HTTP methods`() {
            assertEquals(7, HttpMethod.entries.size)
        }
    }
}

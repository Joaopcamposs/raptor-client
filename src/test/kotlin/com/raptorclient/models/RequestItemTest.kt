package com.raptorclient.models

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class RequestItemTest {
    @Nested
    inner class Defaults {
        @Test
        fun `should create request with default values`() {
            val request = RequestItem()

            assertEquals("New Request", request.name)
            assertEquals(HttpMethod.GET, request.method)
            assertEquals("", request.url)
            assertTrue(request.headers.isEmpty())
            assertTrue(request.params.isEmpty())
            assertEquals(BodyType.NONE, request.body.type)
            assertEquals(AuthType.NONE, request.auth.type)
            assertNull(request.parentId)
            assertNotNull(request.id)
            assertTrue(request.id.isNotBlank())
        }

        @Test
        fun `should generate unique IDs for different requests`() {
            val request1 = RequestItem()
            val request2 = RequestItem()

            assertNotEquals(request1.id, request2.id)
        }
    }

    @Nested
    inner class Duplicate {
        @Test
        fun `should create a copy with new ID`() {
            val original = RequestItem(name = "Original", url = "https://example.com")

            val copy = original.duplicate()

            assertNotEquals(original.id, copy.id)
            assertEquals("Original (Copy)", copy.name)
            assertEquals("https://example.com", copy.url)
        }

        @Test
        fun `should deep copy headers`() {
            val original =
                RequestItem(
                    headers = mutableListOf(KeyValuePair("Key", "Value")),
                )

            val copy = original.duplicate()
            copy.headers[0].key = "Modified"

            assertEquals("Key", original.headers[0].key)
            assertEquals("Modified", copy.headers[0].key)
        }

        @Test
        fun `should deep copy params`() {
            val original =
                RequestItem(
                    params = mutableListOf(KeyValuePair("page", "1")),
                )

            val copy = original.duplicate()
            copy.params[0].value = "2"

            assertEquals("1", original.params[0].value)
            assertEquals("2", copy.params[0].value)
        }

        @Test
        fun `should preserve method and URL`() {
            val original =
                RequestItem(
                    method = HttpMethod.POST,
                    url = "https://api.example.com/users",
                )

            val copy = original.duplicate()

            assertEquals(HttpMethod.POST, copy.method)
            assertEquals("https://api.example.com/users", copy.url)
        }

        @Test
        fun `should set new timestamps on duplicate`() {
            val original = RequestItem()
            // Pequeno delay para garantir timestamps diferentes
            Thread.sleep(10)

            val copy = original.duplicate()

            assertTrue(copy.createdAt >= original.createdAt)
        }
    }

    @Nested
    inner class KeyValuePairTest {
        @Test
        fun `should create with default values`() {
            val pair = KeyValuePair()

            assertEquals("", pair.key)
            assertEquals("", pair.value)
            assertTrue(pair.enabled)
            assertEquals("", pair.description)
        }

        @Test
        fun `should create with custom values`() {
            val pair =
                KeyValuePair(
                    key = "Authorization",
                    value = "Bearer token",
                    enabled = true,
                    description = "Auth header",
                )

            assertEquals("Authorization", pair.key)
            assertEquals("Bearer token", pair.value)
            assertTrue(pair.enabled)
            assertEquals("Auth header", pair.description)
        }
    }

    @Nested
    inner class RequestBodyTest {
        @Test
        fun `should create with NONE type by default`() {
            val body = RequestBody()

            assertEquals(BodyType.NONE, body.type)
            assertEquals("", body.raw)
            assertEquals(RawBodyType.JSON, body.rawType)
            assertTrue(body.formData.isEmpty())
            assertTrue(body.urlEncoded.isEmpty())
        }
    }

    @Nested
    inner class AuthConfigTest {
        @Test
        fun `should create with NONE type by default`() {
            val auth = AuthConfig()

            assertEquals(AuthType.NONE, auth.type)
            assertEquals("", auth.bearerToken)
            assertEquals("", auth.basicUsername)
            assertEquals("", auth.basicPassword)
            assertEquals("", auth.apiKeyName)
            assertEquals("", auth.apiKeyValue)
            assertEquals(ApiKeyLocation.HEADER, auth.apiKeyLocation)
        }
    }
}

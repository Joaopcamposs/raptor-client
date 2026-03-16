package com.raptorclient.models

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SerializationTest {
    private lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun setUp() {
        objectMapper = ObjectMapper().registerModule(KotlinModule.Builder().build())
    }

    @Nested
    inner class RequestItemSerialization {
        @Test
        fun `should round-trip serialize RequestItem`() {
            val original =
                RequestItem(
                    name = "Test Request",
                    method = HttpMethod.POST,
                    url = "https://api.example.com/users",
                    headers = mutableListOf(KeyValuePair("Content-Type", "application/json")),
                    params = mutableListOf(KeyValuePair("page", "1")),
                    body =
                        RequestBody(
                            type = BodyType.RAW,
                            raw = """{"name": "John"}""",
                            rawType = RawBodyType.JSON,
                        ),
                    auth =
                        AuthConfig(
                            type = AuthType.BEARER,
                            bearerToken = "mytoken",
                        ),
                )

            val json = objectMapper.writeValueAsString(original)
            val deserialized: RequestItem = objectMapper.readValue(json)

            assertEquals(original.id, deserialized.id)
            assertEquals(original.name, deserialized.name)
            assertEquals(original.method, deserialized.method)
            assertEquals(original.url, deserialized.url)
            assertEquals(original.headers.size, deserialized.headers.size)
            assertEquals(original.headers[0].key, deserialized.headers[0].key)
            assertEquals(original.params.size, deserialized.params.size)
            assertEquals(original.body.type, deserialized.body.type)
            assertEquals(original.body.raw, deserialized.body.raw)
            assertEquals(original.auth.type, deserialized.auth.type)
            assertEquals(original.auth.bearerToken, deserialized.auth.bearerToken)
        }

        @Test
        fun `should handle unknown JSON properties gracefully`() {
            val json =
                """
                {
                    "id": "test-id",
                    "name": "Test",
                    "method": "GET",
                    "url": "https://example.com",
                    "headers": [],
                    "params": [],
                    "body": {"type": "NONE", "raw": "", "rawType": "JSON", "formData": [], "urlEncoded": []},
                    "auth": {"type": "NONE", "bearerToken": "", "basicUsername": "", "basicPassword": "", "apiKeyName": "", "apiKeyValue": "", "apiKeyLocation": "HEADER"},
                    "unknownField": "should be ignored",
                    "preRequestScript": "",
                    "testScript": "",
                    "createdAt": 0,
                    "updatedAt": 0
                }
                """.trimIndent()

            val request: RequestItem = objectMapper.readValue(json)

            assertEquals("test-id", request.id)
            assertEquals("Test", request.name)
            assertEquals(HttpMethod.GET, request.method)
        }
    }

    @Nested
    inner class CollectionSerialization {
        @Test
        fun `should round-trip serialize Collection`() {
            val original =
                Collection(
                    folders = mutableListOf(FolderItem(name = "Folder 1")),
                    requests = mutableListOf(RequestItem(name = "Request 1")),
                    drafts = mutableListOf(RequestItem(name = "Draft 1")),
                )

            val json = objectMapper.writeValueAsString(original)
            val deserialized: Collection = objectMapper.readValue(json)

            assertEquals(1, deserialized.folders.size)
            assertEquals("Folder 1", deserialized.folders[0].name)
            assertEquals(1, deserialized.requests.size)
            assertEquals("Request 1", deserialized.requests[0].name)
            assertEquals(1, deserialized.drafts.size)
            assertEquals("Draft 1", deserialized.drafts[0].name)
        }

        @Test
        fun `should deserialize empty collection`() {
            val json = """{"folders":[],"requests":[],"drafts":[]}"""

            val collection: Collection = objectMapper.readValue(json)

            assertTrue(collection.folders.isEmpty())
            assertTrue(collection.requests.isEmpty())
            assertTrue(collection.drafts.isEmpty())
        }
    }

    @Nested
    inner class FolderItemSerialization {
        @Test
        fun `should round-trip serialize FolderItem`() {
            val original = FolderItem(name = "My Folder", parentId = "parent-123")

            val json = objectMapper.writeValueAsString(original)
            val deserialized: FolderItem = objectMapper.readValue(json)

            assertEquals(original.id, deserialized.id)
            assertEquals(original.name, deserialized.name)
            assertEquals(original.parentId, deserialized.parentId)
            assertEquals(original.expanded, deserialized.expanded)
        }

        @Test
        fun `should handle null parentId`() {
            val original = FolderItem(name = "Root Folder", parentId = null)

            val json = objectMapper.writeValueAsString(original)
            val deserialized: FolderItem = objectMapper.readValue(json)

            assertNull(deserialized.parentId)
        }
    }

    @Nested
    inner class AuthConfigSerialization {
        @Test
        fun `should serialize all auth types`() {
            for (authType in AuthType.entries) {
                val auth = AuthConfig(type = authType)
                val json = objectMapper.writeValueAsString(auth)
                val deserialized: AuthConfig = objectMapper.readValue(json)
                assertEquals(authType, deserialized.type)
            }
        }

        @Test
        fun `should serialize API key with location`() {
            val auth =
                AuthConfig(
                    type = AuthType.API_KEY,
                    apiKeyName = "X-API-Key",
                    apiKeyValue = "secret",
                    apiKeyLocation = ApiKeyLocation.QUERY,
                )

            val json = objectMapper.writeValueAsString(auth)
            val deserialized: AuthConfig = objectMapper.readValue(json)

            assertEquals(ApiKeyLocation.QUERY, deserialized.apiKeyLocation)
            assertEquals("X-API-Key", deserialized.apiKeyName)
            assertEquals("secret", deserialized.apiKeyValue)
        }
    }
}

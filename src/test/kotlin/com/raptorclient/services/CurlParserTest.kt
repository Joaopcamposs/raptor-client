package com.raptorclient.services

import com.raptorclient.models.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CurlParserTest {
    private lateinit var parser: CurlParser

    @BeforeEach
    fun setUp() {
        parser = CurlParser()
    }

    @Nested
    inner class BasicParsing {
        @Test
        fun `should parse simple GET request`() {
            val result = parser.parse("curl https://api.example.com/users")

            assertEquals(HttpMethod.GET, result.method)
            assertEquals("https://api.example.com/users", result.url)
        }

        @Test
        fun `should parse URL with single quotes`() {
            val result = parser.parse("curl 'https://api.example.com/users'")

            assertEquals("https://api.example.com/users", result.url)
        }

        @Test
        fun `should parse URL with double quotes`() {
            val result = parser.parse("curl \"https://api.example.com/users\"")

            assertEquals("https://api.example.com/users", result.url)
        }

        @Test
        fun `should handle multiline cURL with backslash continuations`() {
            val curl =
                """
                curl -X POST https://api.example.com/users \
                  -H "Content-Type: application/json" \
                  -d '{"name": "John"}'
                """.trimIndent()

            val result = parser.parse(curl)

            assertEquals(HttpMethod.POST, result.method)
            assertEquals("https://api.example.com/users", result.url)
        }
    }

    @Nested
    inner class HttpMethods {
        @Test
        fun `should parse explicit GET method`() {
            val result = parser.parse("curl -X GET https://api.example.com/users")
            assertEquals(HttpMethod.GET, result.method)
        }

        @Test
        fun `should parse POST method`() {
            val result = parser.parse("curl -X POST https://api.example.com/users")
            assertEquals(HttpMethod.POST, result.method)
        }

        @Test
        fun `should parse PUT method`() {
            val result = parser.parse("curl -X PUT https://api.example.com/users/1")
            assertEquals(HttpMethod.PUT, result.method)
        }

        @Test
        fun `should parse DELETE method`() {
            val result = parser.parse("curl -X DELETE https://api.example.com/users/1")
            assertEquals(HttpMethod.DELETE, result.method)
        }

        @Test
        fun `should parse PATCH method`() {
            val result = parser.parse("curl -X PATCH https://api.example.com/users/1")
            assertEquals(HttpMethod.PATCH, result.method)
        }

        @Test
        fun `should parse HEAD method`() {
            val result = parser.parse("curl -X HEAD https://api.example.com/users")
            assertEquals(HttpMethod.HEAD, result.method)
        }

        @Test
        fun `should parse OPTIONS method`() {
            val result = parser.parse("curl -X OPTIONS https://api.example.com/users")
            assertEquals(HttpMethod.OPTIONS, result.method)
        }

        @Test
        fun `should parse method with --request flag`() {
            val result = parser.parse("curl --request POST https://api.example.com/users")
            assertEquals(HttpMethod.POST, result.method)
        }

        @Test
        fun `should parse case-insensitive method`() {
            val result = parser.parse("curl -X post https://api.example.com/users")
            assertEquals(HttpMethod.POST, result.method)
        }
    }

    @Nested
    inner class Headers {
        @Test
        fun `should parse single header with -H`() {
            val result = parser.parse("curl -H \"Content-Type: application/json\" https://api.example.com")

            assertEquals(1, result.headers.size)
            assertEquals("Content-Type", result.headers[0].key)
            assertEquals("application/json", result.headers[0].value)
            assertTrue(result.headers[0].enabled)
        }

        @Test
        fun `should parse multiple headers`() {
            val curl = """curl -H "Content-Type: application/json" -H "Accept: text/html" https://api.example.com"""

            val result = parser.parse(curl)

            assertEquals(2, result.headers.size)
            assertEquals("Content-Type", result.headers[0].key)
            assertEquals("Accept", result.headers[1].key)
        }

        @Test
        fun `should parse header with --header flag`() {
            val result = parser.parse("curl --header \"X-Custom: value\" https://api.example.com")

            assertEquals(1, result.headers.size)
            assertEquals("X-Custom", result.headers[0].key)
            assertEquals("value", result.headers[0].value)
        }

        @Test
        fun `should parse header with value containing colons`() {
            val result = parser.parse("curl -H \"Authorization: Bearer abc:def:ghi\" https://api.example.com")

            // O Authorization será extraído como auth, mas vamos verificar o parse
            // Nesse caso, Bearer token é detectado e movido para auth
            assertEquals(AuthType.BEARER, result.auth.type)
            assertEquals("abc:def:ghi", result.auth.bearerToken)
        }
    }

    @Nested
    inner class RequestBody {
        @Test
        fun `should parse raw body with -d flag`() {
            val curl = """curl -X POST -d '{"name": "John"}' https://api.example.com/users"""

            val result = parser.parse(curl)

            assertEquals(BodyType.RAW, result.body.type)
            assertEquals("{\"name\": \"John\"}", result.body.raw)
            assertEquals(RawBodyType.JSON, result.body.rawType)
        }

        @Test
        fun `should parse body with --data flag`() {
            val curl = """curl -X POST --data '{"key": "value"}' https://api.example.com"""

            val result = parser.parse(curl)

            assertEquals(BodyType.RAW, result.body.type)
            assertEquals("{\"key\": \"value\"}", result.body.raw)
        }

        @Test
        fun `should parse body with --data-raw flag`() {
            val curl = """curl -X POST --data-raw '{"key": "value"}' https://api.example.com"""

            val result = parser.parse(curl)

            assertEquals(BodyType.RAW, result.body.type)
        }

        @Test
        fun `should detect JSON body type from content starting with brace`() {
            val curl = """curl -d '{"a": 1}' https://api.example.com"""

            val result = parser.parse(curl)

            assertEquals(RawBodyType.JSON, result.body.rawType)
        }

        @Test
        fun `should detect JSON body type from content starting with bracket`() {
            val curl = """curl -d '[1, 2, 3]' https://api.example.com"""

            val result = parser.parse(curl)

            assertEquals(RawBodyType.JSON, result.body.rawType)
        }

        @Test
        fun `should auto-set POST method when body is present and method is GET`() {
            val curl = """curl -d '{"name": "test"}' https://api.example.com"""

            val result = parser.parse(curl)

            assertEquals(HttpMethod.POST, result.method)
        }

        @Test
        fun `should not override explicit method when body is present`() {
            val curl = """curl -X PUT -d '{"name": "test"}' https://api.example.com"""

            val result = parser.parse(curl)

            assertEquals(HttpMethod.PUT, result.method)
        }
    }

    @Nested
    inner class UrlEncodedData {
        @Test
        fun `should parse --data-urlencode flag`() {
            val curl = """curl --data-urlencode 'name=John' https://api.example.com"""

            val result = parser.parse(curl)

            assertEquals(BodyType.URL_ENCODED, result.body.type)
            assertEquals(1, result.body.urlEncoded.size)
            assertEquals("name", result.body.urlEncoded[0].key)
            assertEquals("John", result.body.urlEncoded[0].value)
        }
    }

    @Nested
    inner class FormData {
        @Test
        fun `should parse -F flag as form data`() {
            val curl = """curl -F 'file=@/path/to/file' https://api.example.com/upload"""

            val result = parser.parse(curl)

            assertEquals(BodyType.FORM_DATA, result.body.type)
            assertEquals(1, result.body.formData.size)
            assertEquals("file", result.body.formData[0].key)
            assertEquals("@/path/to/file", result.body.formData[0].value)
        }

        @Test
        fun `should parse --form flag as form data`() {
            val curl = """curl --form 'name=John' https://api.example.com"""

            val result = parser.parse(curl)

            assertEquals(BodyType.FORM_DATA, result.body.type)
        }

        @Test
        fun `should parse multiple form fields`() {
            val curl = """curl -F 'name=John' -F 'age=30' https://api.example.com"""

            val result = parser.parse(curl)

            assertEquals(2, result.body.formData.size)
        }
    }

    @Nested
    inner class Authentication {
        @Test
        fun `should parse Bearer token from Authorization header`() {
            val curl = """curl -H "Authorization: Bearer mytoken123" https://api.example.com"""

            val result = parser.parse(curl)

            assertEquals(AuthType.BEARER, result.auth.type)
            assertEquals("mytoken123", result.auth.bearerToken)
            assertTrue(result.headers.none { it.key.equals("Authorization", ignoreCase = true) })
        }

        @Test
        fun `should parse Basic auth from Authorization header`() {
            val encoded =
                java.util.Base64
                    .getEncoder()
                    .encodeToString("user:pass".toByteArray())
            val curl = """curl -H "Authorization: Basic $encoded" https://api.example.com"""

            val result = parser.parse(curl)

            assertEquals(AuthType.BASIC, result.auth.type)
            assertEquals("user", result.auth.basicUsername)
            assertEquals("pass", result.auth.basicPassword)
        }

        @Test
        fun `should parse -u flag as Basic auth`() {
            val curl = """curl -u user:password https://api.example.com"""

            val result = parser.parse(curl)

            assertEquals(AuthType.BASIC, result.auth.type)
            assertEquals("user", result.auth.basicUsername)
            assertEquals("password", result.auth.basicPassword)
        }

        @Test
        fun `should parse --user flag as Basic auth`() {
            val curl = """curl --user admin:secret https://api.example.com"""

            val result = parser.parse(curl)

            assertEquals(AuthType.BASIC, result.auth.type)
            assertEquals("admin", result.auth.basicUsername)
            assertEquals("secret", result.auth.basicPassword)
        }
    }

    @Nested
    inner class SpecialHeaders {
        @Test
        fun `should parse -A flag as User-Agent header`() {
            val curl = """curl -A "Mozilla/5.0" https://api.example.com"""

            val result = parser.parse(curl)

            assertTrue(result.headers.any { it.key == "User-Agent" && it.value == "Mozilla/5.0" })
        }

        @Test
        fun `should parse -e flag as Referer header`() {
            val curl = """curl -e "https://google.com" https://api.example.com"""

            val result = parser.parse(curl)

            assertTrue(result.headers.any { it.key == "Referer" && it.value == "https://google.com" })
        }

        @Test
        fun `should parse -b flag as Cookie header`() {
            val curl = """curl -b "session=abc123" https://api.example.com"""

            val result = parser.parse(curl)

            assertTrue(result.headers.any { it.key == "Cookie" && it.value == "session=abc123" })
        }
    }

    @Nested
    inner class RequestNaming {
        @Test
        fun `should generate name from method and path`() {
            val result = parser.parse("curl -X POST https://api.example.com/api/users")

            assertEquals("POST /api/users", result.name)
        }

        @Test
        fun `should use root path when URL has no path`() {
            val result = parser.parse("curl https://api.example.com")

            assertEquals("GET /", result.name)
        }

        @Test
        fun `should handle invalid URL gracefully`() {
            val result = parser.parse("curl not-a-valid-url")

            assertNotNull(result.name)
        }
    }

    @Nested
    inner class ComplexCommands {
        @Test
        fun `should parse complete POST request with headers, body, and auth`() {
            val curl =
                """
                curl -X POST https://api.example.com/users \
                  -H "Content-Type: application/json" \
                  -H "Accept: application/json" \
                  -H "Authorization: Bearer token123" \
                  -d '{"name": "John", "email": "john@example.com"}'
                """.trimIndent()

            val result = parser.parse(curl)

            assertEquals(HttpMethod.POST, result.method)
            assertEquals("https://api.example.com/users", result.url)
            assertEquals(AuthType.BEARER, result.auth.type)
            assertEquals("token123", result.auth.bearerToken)
            assertEquals(BodyType.RAW, result.body.type)
            assertTrue(result.body.raw.contains("John"))
            // Content-Type and Accept should remain, Authorization moved to auth
            assertEquals(2, result.headers.size)
        }

        @Test
        fun `should ignore unknown flags gracefully`() {
            val curl = """curl -k --compressed -v https://api.example.com"""

            val result = parser.parse(curl)

            assertEquals("https://api.example.com", result.url)
            assertEquals(HttpMethod.GET, result.method)
        }
    }
}

package com.raptorclient.services

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Testa a lógica de resolução de variáveis de ambiente isoladamente,
 * sem depender do PersistentStateComponent do IntelliJ.
 */
class EnvironmentVariableResolverTest {
    private fun resolveVariables(
        text: String,
        vars: Map<String, String>,
    ): String {
        var result = text
        val pattern = Regex("\\{\\{([^}]+)\\}\\}")

        pattern.findAll(text).forEach { match ->
            val varName = match.groupValues[1].trim()
            val value = vars[varName]
            if (value != null) {
                result = result.replace(match.value, value)
            }
        }

        return result
    }

    @Nested
    inner class BasicResolution {
        @Test
        fun `should resolve single variable`() {
            val vars = mapOf("base_url" to "https://api.example.com")

            val result = resolveVariables("{{base_url}}/users", vars)

            assertEquals("https://api.example.com/users", result)
        }

        @Test
        fun `should resolve multiple variables`() {
            val vars =
                mapOf(
                    "base_url" to "https://api.example.com",
                    "version" to "v2",
                )

            val result = resolveVariables("{{base_url}}/api/{{version}}/users", vars)

            assertEquals("https://api.example.com/api/v2/users", result)
        }

        @Test
        fun `should return text unchanged when no variables present`() {
            val vars = mapOf("base_url" to "https://api.example.com")

            val result = resolveVariables("https://static-url.com/users", vars)

            assertEquals("https://static-url.com/users", result)
        }

        @Test
        fun `should leave undefined variables unresolved`() {
            val vars = mapOf("base_url" to "https://api.example.com")

            val result = resolveVariables("{{base_url}}/{{undefined_var}}", vars)

            assertEquals("https://api.example.com/{{undefined_var}}", result)
        }

        @Test
        fun `should return empty string when text is empty`() {
            val result = resolveVariables("", mapOf("key" to "value"))
            assertEquals("", result)
        }
    }

    @Nested
    inner class EdgeCases {
        @Test
        fun `should resolve variable with spaces around name`() {
            val vars = mapOf("token" to "abc123")

            val result = resolveVariables("{{ token }}", vars)

            assertEquals("abc123", result)
        }

        @Test
        fun `should resolve same variable used multiple times`() {
            val vars = mapOf("host" to "localhost")

            val result = resolveVariables("{{host}}:8080 and {{host}}:9090", vars)

            assertEquals("localhost:8080 and localhost:9090", result)
        }

        @Test
        fun `should handle variables in headers format`() {
            val vars = mapOf("access_token" to "eyJhbGciOiJIUzI1NiJ9")

            val result = resolveVariables("Bearer {{access_token}}", vars)

            assertEquals("Bearer eyJhbGciOiJIUzI1NiJ9", result)
        }

        @Test
        fun `should handle variables in JSON body`() {
            val vars = mapOf("user_id" to "42")

            val result = resolveVariables("""{"userId": "{{user_id}}"}""", vars)

            assertEquals("""{"userId": "42"}""", result)
        }

        @Test
        fun `should not resolve incomplete patterns`() {
            val vars = mapOf("key" to "value")

            assertEquals("{{key", resolveVariables("{{key", vars))
            assertEquals("{key}", resolveVariables("{key}", vars))
            assertEquals("key}}", resolveVariables("key}}", vars))
        }

        @Test
        fun `should handle empty variables map`() {
            val result = resolveVariables("{{base_url}}/api", emptyMap())

            assertEquals("{{base_url}}/api", result)
        }
    }
}

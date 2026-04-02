package com.raptorclient.models

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class HttpResponseTest {
    @Nested
    inner class IsSuccess {
        @Test
        fun `should return true for 2xx status codes`() {
            assertTrue(createResponse(200).isSuccess)
            assertTrue(createResponse(201).isSuccess)
            assertTrue(createResponse(204).isSuccess)
            assertTrue(createResponse(299).isSuccess)
        }

        @Test
        fun `should return false for non-2xx status codes`() {
            assertFalse(createResponse(0).isSuccess)
            assertFalse(createResponse(100).isSuccess)
            assertFalse(createResponse(301).isSuccess)
            assertFalse(createResponse(400).isSuccess)
            assertFalse(createResponse(404).isSuccess)
            assertFalse(createResponse(500).isSuccess)
        }
    }

    @Nested
    inner class FormattedSize {
        @Test
        fun `should format bytes`() {
            assertEquals("100 bytes", createResponse(responseSize = 100).formattedSize)
            assertEquals("0 bytes", createResponse(responseSize = 0).formattedSize)
            assertEquals("1023 bytes", createResponse(responseSize = 1023).formattedSize)
        }

        @Test
        fun `should format kilobytes`() {
            val response = createResponse(responseSize = 1024)
            assertEquals("1.00 KB", response.formattedSize)
        }

        @Test
        fun `should format kilobytes with decimals`() {
            val response = createResponse(responseSize = 2560)
            assertEquals("2.50 KB", response.formattedSize)
        }

        @Test
        fun `should format megabytes`() {
            val response = createResponse(responseSize = 1024 * 1024)
            assertEquals("1.00 MB", response.formattedSize)
        }

        @Test
        fun `should format megabytes with decimals`() {
            val response = createResponse(responseSize = (1.5 * 1024 * 1024).toLong())
            assertEquals("1.50 MB", response.formattedSize)
        }
    }

    @Nested
    inner class FormattedTime {
        @Test
        fun `should format milliseconds`() {
            assertEquals("100 ms", createResponse(responseTime = 100).formattedTime)
            assertEquals("0 ms", createResponse(responseTime = 0).formattedTime)
            assertEquals("999 ms", createResponse(responseTime = 999).formattedTime)
        }

        @Test
        fun `should format seconds`() {
            assertEquals("1.00 s", createResponse(responseTime = 1000).formattedTime)
            assertEquals("2.50 s", createResponse(responseTime = 2500).formattedTime)
        }
    }

    private fun createResponse(
        statusCode: Int = 200,
        responseSize: Long = 0,
        responseTime: Long = 0,
    ): HttpResponse =
        HttpResponse(
            statusCode = statusCode,
            statusText = "OK",
            headers = emptyMap(),
            body = "",
            contentType = "text/plain",
            responseTime = responseTime,
            responseSize = responseSize,
        )
}

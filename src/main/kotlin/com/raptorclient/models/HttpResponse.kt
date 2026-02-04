package com.raptorclient.models

data class HttpResponse(
    val statusCode: Int,
    val statusText: String,
    val headers: Map<String, List<String>>,
    val body: String,
    val contentType: String,
    val responseTime: Long,
    val responseSize: Long,
    val timestamp: Long = System.currentTimeMillis(),
) {
    val isSuccess: Boolean get() = statusCode in 200..299

    val formattedSize: String get() {
        return when {
            responseSize < 1024 -> "$responseSize bytes"
            responseSize < 1024 * 1024 -> String.format("%.2f KB", responseSize / 1024.0)
            else -> String.format("%.2f MB", responseSize / (1024.0 * 1024.0))
        }
    }

    val formattedTime: String get() {
        return when {
            responseTime < 1000 -> "$responseTime ms"
            else -> String.format("%.2f s", responseTime / 1000.0)
        }
    }
}

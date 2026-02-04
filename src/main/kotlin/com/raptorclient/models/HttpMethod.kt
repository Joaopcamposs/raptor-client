package com.raptorclient.models

enum class HttpMethod(
    val displayName: String,
    val color: String,
) {
    GET("GET", "#61AFFE"),
    POST("POST", "#49CC90"),
    PUT("PUT", "#FCA130"),
    DELETE("DELETE", "#F93E3E"),
    PATCH("PATCH", "#50E3C2"),
    HEAD("HEAD", "#9012FE"),
    OPTIONS("OPTIONS", "#0D5AA7"),
    ;

    companion object {
        fun fromString(method: String): HttpMethod = values().find { it.name.equals(method, ignoreCase = true) } ?: GET
    }
}

package com.raptorclient.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.UUID

@JsonIgnoreProperties(ignoreUnknown = true)
data class RequestItem(
    @JsonProperty("id")
    val id: String = UUID.randomUUID().toString(),
    @JsonProperty("name")
    var name: String = "New Request",
    @JsonProperty("method")
    var method: HttpMethod = HttpMethod.GET,
    @JsonProperty("url")
    var url: String = "",
    @JsonProperty("headers")
    var headers: MutableList<KeyValuePair> = mutableListOf(),
    @JsonProperty("params")
    var params: MutableList<KeyValuePair> = mutableListOf(),
    @JsonProperty("body")
    var body: RequestBody = RequestBody(),
    @JsonProperty("auth")
    var auth: AuthConfig = AuthConfig(),
    @JsonProperty("preRequestScript")
    var preRequestScript: String = "",
    @JsonProperty("testScript")
    var testScript: String = "",
    @JsonProperty("parentId")
    var parentId: String? = null,
    @JsonProperty("createdAt")
    val createdAt: Long = System.currentTimeMillis(),
    @JsonProperty("updatedAt")
    var updatedAt: Long = System.currentTimeMillis(),
) {
    fun duplicate(): RequestItem =
        this.copy(
            id = UUID.randomUUID().toString(),
            name = "${this.name} (Copy)",
            headers = this.headers.map { it.copy() }.toMutableList(),
            params = this.params.map { it.copy() }.toMutableList(),
            body = this.body.copy(),
            auth = this.auth.copy(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
        )
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class KeyValuePair(
    @JsonProperty("key")
    var key: String = "",
    @JsonProperty("value")
    var value: String = "",
    @JsonProperty("enabled")
    var enabled: Boolean = true,
    @JsonProperty("description")
    var description: String = "",
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class RequestBody(
    @JsonProperty("type")
    var type: BodyType = BodyType.NONE,
    @JsonProperty("raw")
    var raw: String = "",
    @JsonProperty("rawType")
    var rawType: RawBodyType = RawBodyType.JSON,
    @JsonProperty("formData")
    var formData: MutableList<KeyValuePair> = mutableListOf(),
    @JsonProperty("urlEncoded")
    var urlEncoded: MutableList<KeyValuePair> = mutableListOf(),
)

enum class BodyType {
    NONE,
    RAW,
    FORM_DATA,
    URL_ENCODED,
    BINARY,
}

enum class RawBodyType(
    val contentType: String,
) {
    JSON("application/json"),
    XML("application/xml"),
    TEXT("text/plain"),
    HTML("text/html"),
    JAVASCRIPT("application/javascript"),
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class AuthConfig(
    @JsonProperty("type")
    var type: AuthType = AuthType.NONE,
    @JsonProperty("bearerToken")
    var bearerToken: String = "",
    @JsonProperty("basicUsername")
    var basicUsername: String = "",
    @JsonProperty("basicPassword")
    var basicPassword: String = "",
    @JsonProperty("apiKeyName")
    var apiKeyName: String = "",
    @JsonProperty("apiKeyValue")
    var apiKeyValue: String = "",
    @JsonProperty("apiKeyLocation")
    var apiKeyLocation: ApiKeyLocation = ApiKeyLocation.HEADER,
)

enum class AuthType {
    NONE,
    BEARER,
    BASIC,
    API_KEY,
}

enum class ApiKeyLocation {
    HEADER,
    QUERY,
}

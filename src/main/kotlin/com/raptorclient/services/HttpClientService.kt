package com.raptorclient.services

import com.raptorclient.models.ApiKeyLocation
import com.raptorclient.models.AuthType
import com.raptorclient.models.BodyType
import com.raptorclient.models.HttpMethod
import com.raptorclient.models.HttpResponse
import com.raptorclient.models.RequestItem
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.Base64
import java.util.concurrent.TimeUnit

class HttpClientService {
    private val client =
        OkHttpClient
            .Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .build()

    fun executeRequest(
        request: RequestItem,
        environmentService: EnvironmentService? = null,
    ): HttpResponse {
        val startTime = System.currentTimeMillis()

        try {
            val resolvedUrl = buildUrl(request, environmentService)
            val httpRequest = buildOkHttpRequest(request, resolvedUrl, environmentService)

            client.newCall(httpRequest).execute().use { response ->
                val endTime = System.currentTimeMillis()
                val body = response.body?.string() ?: ""

                return HttpResponse(
                    statusCode = response.code,
                    statusText = response.message.ifEmpty { getStatusText(response.code) },
                    headers = response.headers.toMultimap(),
                    body = body,
                    contentType = response.header("Content-Type") ?: "text/plain",
                    responseTime = endTime - startTime,
                    responseSize = body.length.toLong(),
                )
            }
        } catch (e: IOException) {
            val endTime = System.currentTimeMillis()
            return HttpResponse(
                statusCode = 0,
                statusText = "Error",
                headers = emptyMap(),
                body = "Error: ${e.message}",
                contentType = "text/plain",
                responseTime = endTime - startTime,
                responseSize = 0,
            )
        }
    }

    private fun buildUrl(
        request: RequestItem,
        environmentService: EnvironmentService?,
    ): String {
        var url = environmentService?.resolveVariables(request.url) ?: request.url

        val enabledParams = request.params.filter { it.enabled && it.key.isNotBlank() }
        if (enabledParams.isNotEmpty()) {
            val queryString =
                enabledParams.joinToString("&") { param ->
                    val key = environmentService?.resolveVariables(param.key) ?: param.key
                    val value = environmentService?.resolveVariables(param.value) ?: param.value
                    "${urlEncode(key)}=${urlEncode(value)}"
                }
            url = if (url.contains("?")) "$url&$queryString" else "$url?$queryString"
        }

        if (request.auth.type == AuthType.API_KEY && request.auth.apiKeyLocation == ApiKeyLocation.QUERY) {
            val key = environmentService?.resolveVariables(request.auth.apiKeyName) ?: request.auth.apiKeyName
            val value = environmentService?.resolveVariables(request.auth.apiKeyValue) ?: request.auth.apiKeyValue
            url = if (url.contains("?")) "$url&$key=$value" else "$url?$key=$value"
        }

        return url
    }

    private fun buildOkHttpRequest(
        request: RequestItem,
        url: String,
        environmentService: EnvironmentService?,
    ): Request {
        val builder = Request.Builder().url(url)

        request.headers.filter { it.enabled && it.key.isNotBlank() }.forEach { header ->
            val key = environmentService?.resolveVariables(header.key) ?: header.key
            val value = environmentService?.resolveVariables(header.value) ?: header.value
            builder.addHeader(key, value)
        }

        when (request.auth.type) {
            AuthType.BEARER -> {
                val token = environmentService?.resolveVariables(request.auth.bearerToken) ?: request.auth.bearerToken
                builder.addHeader("Authorization", "Bearer $token")
            }
            AuthType.BASIC -> {
                val username = environmentService?.resolveVariables(request.auth.basicUsername) ?: request.auth.basicUsername
                val password = environmentService?.resolveVariables(request.auth.basicPassword) ?: request.auth.basicPassword
                val credentials = Base64.getEncoder().encodeToString("$username:$password".toByteArray())
                builder.addHeader("Authorization", "Basic $credentials")
            }
            AuthType.API_KEY -> {
                if (request.auth.apiKeyLocation == ApiKeyLocation.HEADER) {
                    val key = environmentService?.resolveVariables(request.auth.apiKeyName) ?: request.auth.apiKeyName
                    val value = environmentService?.resolveVariables(request.auth.apiKeyValue) ?: request.auth.apiKeyValue
                    builder.addHeader(key, value)
                }
            }
            AuthType.NONE -> {}
        }

        val body = buildRequestBody(request, environmentService)

        when (request.method) {
            HttpMethod.GET -> builder.get()
            HttpMethod.POST -> builder.post(body ?: "".toRequestBody(null))
            HttpMethod.PUT -> builder.put(body ?: "".toRequestBody(null))
            HttpMethod.DELETE -> if (body != null) builder.delete(body) else builder.delete()
            HttpMethod.PATCH -> builder.patch(body ?: "".toRequestBody(null))
            HttpMethod.HEAD -> builder.head()
            HttpMethod.OPTIONS -> builder.method("OPTIONS", null)
        }

        return builder.build()
    }

    private fun buildRequestBody(
        request: RequestItem,
        environmentService: EnvironmentService?,
    ): RequestBody? =
        when (request.body.type) {
            BodyType.NONE -> null
            BodyType.RAW -> {
                val content = environmentService?.resolveVariables(request.body.raw) ?: request.body.raw
                content.toRequestBody(
                    request.body.rawType.contentType
                        .toMediaType(),
                )
            }
            BodyType.FORM_DATA -> {
                val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
                request.body.formData.filter { it.enabled && it.key.isNotBlank() }.forEach { item ->
                    val key = environmentService?.resolveVariables(item.key) ?: item.key
                    val value = environmentService?.resolveVariables(item.value) ?: item.value
                    builder.addFormDataPart(key, value)
                }
                builder.build()
            }
            BodyType.URL_ENCODED -> {
                val builder = FormBody.Builder()
                request.body.urlEncoded.filter { it.enabled && it.key.isNotBlank() }.forEach { item ->
                    val key = environmentService?.resolveVariables(item.key) ?: item.key
                    val value = environmentService?.resolveVariables(item.value) ?: item.value
                    builder.add(key, value)
                }
                builder.build()
            }
            BodyType.BINARY -> null
        }

    private fun urlEncode(value: String): String = java.net.URLEncoder.encode(value, "UTF-8")

    private fun getStatusText(code: Int): String =
        when (code) {
            200 -> "OK"
            201 -> "Created"
            204 -> "No Content"
            400 -> "Bad Request"
            401 -> "Unauthorized"
            403 -> "Forbidden"
            404 -> "Not Found"
            405 -> "Method Not Allowed"
            500 -> "Internal Server Error"
            502 -> "Bad Gateway"
            503 -> "Service Unavailable"
            else -> ""
        }
}

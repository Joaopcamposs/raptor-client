package com.raptorclient.services

import com.raptorclient.models.*

class CurlParser {
    fun parse(curlCommand: String): RequestItem {
        val request = RequestItem(name = "Imported Request")

        val normalizedCommand =
            curlCommand
                .replace("\\\n", " ")
                .replace("\\\r\n", " ")
                .trim()

        val tokens = tokenize(normalizedCommand)

        var i = 0
        while (i < tokens.size) {
            val token = tokens[i]

            when {
                token == "curl" -> {}

                token == "-X" || token == "--request" -> {
                    if (i + 1 < tokens.size) {
                        request.method = HttpMethod.fromString(tokens[++i])
                    }
                }

                token == "-H" || token == "--header" -> {
                    if (i + 1 < tokens.size) {
                        val headerValue = tokens[++i]
                        val colonIndex = headerValue.indexOf(':')
                        if (colonIndex > 0) {
                            val key = headerValue.substring(0, colonIndex).trim()
                            val value = headerValue.substring(colonIndex + 1).trim()
                            request.headers.add(KeyValuePair(key, value, true))
                        }
                    }
                }

                token == "-d" || token == "--data" || token == "--data-raw" || token == "--data-binary" -> {
                    if (i + 1 < tokens.size) {
                        val bodyData = tokens[++i]
                        request.body.type = BodyType.RAW
                        request.body.raw = bodyData

                        if (bodyData.trim().startsWith("{") || bodyData.trim().startsWith("[")) {
                            request.body.rawType = RawBodyType.JSON
                        }

                        if (request.method == HttpMethod.GET) {
                            request.method = HttpMethod.POST
                        }
                    }
                }

                token == "--data-urlencode" -> {
                    if (i + 1 < tokens.size) {
                        val param = tokens[++i]
                        val equalsIndex = param.indexOf('=')
                        if (equalsIndex > 0) {
                            val key = param.substring(0, equalsIndex)
                            val value = param.substring(equalsIndex + 1)
                            request.body.urlEncoded.add(KeyValuePair(key, value, true))
                            request.body.type = BodyType.URL_ENCODED
                        }
                        if (request.method == HttpMethod.GET) {
                            request.method = HttpMethod.POST
                        }
                    }
                }

                token == "-F" || token == "--form" -> {
                    if (i + 1 < tokens.size) {
                        val formData = tokens[++i]
                        val equalsIndex = formData.indexOf('=')
                        if (equalsIndex > 0) {
                            val key = formData.substring(0, equalsIndex)
                            val value = formData.substring(equalsIndex + 1)
                            request.body.formData.add(KeyValuePair(key, value, true))
                            request.body.type = BodyType.FORM_DATA
                        }
                        if (request.method == HttpMethod.GET) {
                            request.method = HttpMethod.POST
                        }
                    }
                }

                token == "-u" || token == "--user" -> {
                    if (i + 1 < tokens.size) {
                        val userPass = tokens[++i]
                        val colonIndex = userPass.indexOf(':')
                        if (colonIndex > 0) {
                            request.auth.type = AuthType.BASIC
                            request.auth.basicUsername = userPass.substring(0, colonIndex)
                            request.auth.basicPassword = userPass.substring(colonIndex + 1)
                        }
                    }
                }

                token == "-A" || token == "--user-agent" -> {
                    if (i + 1 < tokens.size) {
                        request.headers.add(KeyValuePair("User-Agent", tokens[++i], true))
                    }
                }

                token == "-e" || token == "--referer" -> {
                    if (i + 1 < tokens.size) {
                        request.headers.add(KeyValuePair("Referer", tokens[++i], true))
                    }
                }

                token == "-b" || token == "--cookie" -> {
                    if (i + 1 < tokens.size) {
                        request.headers.add(KeyValuePair("Cookie", tokens[++i], true))
                    }
                }

                token.startsWith("-") -> {
                    if (i + 1 < tokens.size && !tokens[i + 1].startsWith("-") && !isUrl(tokens[i + 1])) {
                        i++
                    }
                }

                isUrl(token) || (!token.startsWith("-") && request.url.isEmpty()) -> {
                    request.url = token.removeSurrounding("'").removeSurrounding("\"")
                }
            }

            i++
        }

        val authHeader =
            request.headers.find {
                it.key.equals("Authorization", ignoreCase = true)
            }
        if (authHeader != null) {
            val value = authHeader.value.trim()
            when {
                value.startsWith("Bearer ", ignoreCase = true) -> {
                    request.auth.type = AuthType.BEARER
                    request.auth.bearerToken = value.substring(7).trim()
                    request.headers.remove(authHeader)
                }
                value.startsWith("Basic ", ignoreCase = true) -> {
                    try {
                        val decoded =
                            String(
                                java.util.Base64
                                    .getDecoder()
                                    .decode(value.substring(6).trim()),
                            )
                        val colonIndex = decoded.indexOf(':')
                        if (colonIndex > 0) {
                            request.auth.type = AuthType.BASIC
                            request.auth.basicUsername = decoded.substring(0, colonIndex)
                            request.auth.basicPassword = decoded.substring(colonIndex + 1)
                            request.headers.remove(authHeader)
                        }
                    } catch (_: Exception) {
                        // Invalid Base64 encoding, ignore
                    }
                }
            }
        }

        try {
            val uri = java.net.URI(request.url)
            request.name = "${request.method.name} ${uri.path.ifEmpty { "/" }}"
        } catch (_: Exception) {
            request.name = "${request.method.name} Request"
        }

        return request
    }

    private fun tokenize(command: String): List<String> {
        val tokens = mutableListOf<String>()
        val current = StringBuilder()
        var inSingleQuote = false
        var inDoubleQuote = false
        var escape = false

        for (char in command) {
            when {
                escape -> {
                    current.append(char)
                    escape = false
                }
                char == '\\' && !inSingleQuote -> {
                    escape = true
                }
                char == '\'' && !inDoubleQuote -> {
                    inSingleQuote = !inSingleQuote
                }
                char == '"' && !inSingleQuote -> {
                    inDoubleQuote = !inDoubleQuote
                }
                char.isWhitespace() && !inSingleQuote && !inDoubleQuote -> {
                    if (current.isNotEmpty()) {
                        tokens.add(current.toString())
                        current.clear()
                    }
                }
                else -> {
                    current.append(char)
                }
            }
        }

        if (current.isNotEmpty()) {
            tokens.add(current.toString())
        }

        return tokens
    }

    private fun isUrl(token: String): Boolean {
        val cleaned = token.removeSurrounding("'").removeSurrounding("\"")
        return cleaned.startsWith("http://") || cleaned.startsWith("https://")
    }
}

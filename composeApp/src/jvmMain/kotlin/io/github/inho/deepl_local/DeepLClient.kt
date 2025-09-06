package io.github.inho.deepl_local

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class DeepLClient(private val apiKey: String) {
    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }

        install(Logging) {
            logger = Logger.SIMPLE
            level = LogLevel.INFO
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 10000
            connectTimeoutMillis = 10000
        }
    }

    suspend fun translate(
        text: String,
        sourceLang: Language = Language.AUTO,
        targetLang: Language = Language.ENGLISH
    ): Result<String> {
        return try {
            val response = httpClient.post("https://api-free.deepl.com/v2/translate") {
                headers {
                    append(HttpHeaders.Authorization, "DeepL-Auth-Key $apiKey")
                    append(HttpHeaders.ContentType, "application/json")
                }
                setBody(DeepLTranslateRequest(
                    text = listOf(text),
                    sourceLang = sourceLang.code.takeIf { it.isNotEmpty() },
                    targetLang = targetLang.code
                ))
            }

            val result: DeepLTranslateResponse = response.body()
            Result.success(result.translations.first().text)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun close() {
        httpClient.close()
    }
}
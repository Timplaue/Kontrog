package com.example.kontrog.network

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*

/**
 * Клиент для Gigachat API с поддержкой Access Token.
 * Сначала запрашивает токен, затем может отправлять сообщения.
 */
class GigachatClient(
    private val authorizationKey: String, // твой ключ вида MDE5YThhY2Ut...
    private val clientId: String = "019a8ace-6124-7f42-8b3c-cda182c21dc3"
) {

    private val client = OkHttpClient()
    private val moshi = Moshi.Builder().build()

    private var accessToken: String? = null

    /** Получаем Access Token (действует 30 минут) */
    private suspend fun getAccessToken(): String = withContext(Dispatchers.IO) {
        // Если токен уже есть, возвращаем его
        accessToken?.let { return@withContext it }

        val json = """
            {
                "client_id": "$clientId",
                "scope": "GIGACHAT_API_PERS"
            }
        """.trimIndent().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("https://api.gigachat.com/api/v2/oauth")
            .post(json)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", authorizationKey)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("Ошибка получения токена: ${response.code}")
            val body = response.body?.string() ?: throw Exception("Пустой ответ при получении токена")

            // Парсим JSON
            val adapter = moshi.adapter(AccessTokenResponse::class.java)
            val tokenResponse = adapter.fromJson(body)
                ?: throw Exception("Не удалось распарсить Access Token")
            accessToken = tokenResponse.accessToken
            return@withContext accessToken!!
        }
    }

    /** Отправляем сообщение в чат */
    suspend fun sendMessage(prompt: String): String = withContext(Dispatchers.IO) {
        val token = getAccessToken()

        val json = """
            {
                "model": "gigachat-large",
                "messages": [
                    {"role": "user", "content": "$prompt"}
                ]
            }
        """.trimIndent().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("https://api.gigachat.com/v1/chat/completions")
            .post(json)
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("Ошибка API: ${response.code}")
            val body = response.body?.string() ?: ""
            // Можно парсить JSON с Moshi, если нужно только текст
            return@withContext body
        }
    }

    private data class AccessTokenResponse(
        @Json(name = "access_token") val accessToken: String
    )
}

package com.maurofmorato.cafecomnota.data.coffee

import com.maurofmorato.cafecomnota.data.supabase.SupabaseConfig
import com.maurofmorato.cafecomnota.data.auth.AuthenticationExpiredException
import com.maurofmorato.cafecomnota.data.auth.isAuthenticationExpired
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class SupabaseCoffeeWriteRepository {
    suspend fun createCoffee(
        request: CoffeeCreateRequest
    ): String {
        return withContext(Dispatchers.IO) {
            val endpoint = "${SupabaseConfig.BASE_URL}/rest/v1/cafes"

            val body = JSONObject()
                .put("nome", request.name.trim())
                .put("marca", request.brand.trim())
                .put("tipo_cafe", request.type)
                .put("torra", request.roast)
                .put("peso_padrao_g", request.standardWeightGrams)
                .put("origem_dado", "usuario")
                .put("fonte_dado", "Usuário")
                .put("produto_rotulo", request.name.trim())
                .put("status", request.status)
                .put("cadastrado_por", request.userId)

            executeInsert(
                endpoint = endpoint,
                accessToken = request.accessToken,
                body = body
            )
        }
    }

    private fun executeInsert(
        endpoint: String,
        accessToken: String,
        body: JSONObject
    ): String {
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 12_000
            readTimeout = 12_000
            doOutput = true

            setRequestProperty("apikey", SupabaseConfig.PUBLISHABLE_KEY)
            setRequestProperty("Authorization", "Bearer $accessToken")
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Prefer", "return=representation")
        }

        try {
            connection.outputStream.use { output ->
                output.write(body.toString().toByteArray(Charsets.UTF_8))
            }

            val statusCode = connection.responseCode
            val responseBody = if (statusCode in 200..299) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
            }

            if (statusCode !in 200..299) {
                if (isAuthenticationExpired(statusCode, responseBody)) {
                    throw AuthenticationExpiredException()
                }
                throw IllegalStateException(parseErrorMessage(responseBody))
            }

            return parseInsertedCoffeeId(responseBody)
        } finally {
            connection.disconnect()
        }
    }

    private fun parseInsertedCoffeeId(
        responseBody: String
    ): String {
        if (responseBody.isBlank()) {
            return ""
        }

        return try {
            val array = JSONArray(responseBody)

            if (array.length() > 0) {
                array.getJSONObject(0).optString("id", "")
            } else {
                ""
            }
        } catch (_: Exception) {
            ""
        }
    }

    private fun parseErrorMessage(
        responseBody: String
    ): String {
        if (responseBody.isBlank()) {
            return "Não foi possível salvar o café."
        }

        return try {
            val json = JSONObject(responseBody)

            val message = json.optString("message")
                .ifBlank { json.optString("msg") }
                .ifBlank { json.optString("hint") }
                .ifBlank { json.optString("details") }

            message.ifBlank {
                "Não foi possível salvar o café."
            }
        } catch (_: Exception) {
            "Não foi possível salvar o café."
        }
    }
}

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
    suspend fun createOrReuseCoffee(
        request: CoffeeCreateRequest
    ): String {
        return withContext(Dispatchers.IO) {
            findCoffeeIdForSubmission(request)?.let { return@withContext it }

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
                .put("chave_envio", request.submissionKey)
                .put("fotos_esperadas", request.photos.size)
                .put("fotos_enviadas", 0)
                .put(
                    "fotos_status",
                    if (request.photos.isEmpty()) "nao_solicitada" else "pendente"
                )

            request.producer?.let { body.put("produtor", it) }
            request.originRegion?.let { body.put("origem_regiao", it) }
            request.altitudeMeters?.let { body.put("altitude_m", it) }
            request.variety?.let { body.put("variedade", it) }
            request.process?.let { body.put("processo", it) }
            request.aromaFlavor?.let { body.put("aroma_sabor", it) }
            request.certification?.let { body.put("certificacao", it) }

            try {
                executeInsert(endpoint, request.accessToken, body)
            } catch (error: Throwable) {
                // A resposta pode ter se perdido depois de o banco criar o café.
                findCoffeeIdForSubmission(request) ?: throw error
            }
        }
    }

    private fun findCoffeeIdForSubmission(request: CoffeeCreateRequest): String? {
        val endpoint = "${SupabaseConfig.BASE_URL}/rest/v1/cafes?select=id" +
            "&cadastrado_por=eq.${encode(request.userId)}" +
            "&chave_envio=eq.${encode(request.submissionKey)}&limit=1"
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 12_000
            readTimeout = 12_000
            setRequestProperty("apikey", SupabaseConfig.PUBLISHABLE_KEY)
            setRequestProperty("Authorization", "Bearer ${request.accessToken}")
            setRequestProperty("Accept", "application/json")
        }

        return try {
            val code = connection.responseCode
            if (code !in 200..299) return null
            val response = connection.inputStream.bufferedReader().use { it.readText() }
            parseInsertedCoffeeId(response)
                .ifBlank { null }
        } finally {
            connection.disconnect()
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
        // A resposta do banco pode conter SQL, JWT ou detalhes internos.
        // A tela exibe apenas uma orientação segura ao usuário.
        return "Não foi possível concluir esta ação agora. Tente novamente."
    }

    private fun encode(value: String): String = java.net.URLEncoder.encode(
        value,
        java.nio.charset.StandardCharsets.UTF_8.toString()
    )
}

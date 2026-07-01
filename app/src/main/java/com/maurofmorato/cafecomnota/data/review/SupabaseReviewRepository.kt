package com.maurofmorato.cafecomnota.data.review

import com.maurofmorato.cafecomnota.data.supabase.SupabaseConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class SupabaseReviewRepository {
    suspend fun saveReviewAndOptionalPrice(request: ReviewSaveRequest) {
        withContext(Dispatchers.IO) {
            saveReview(request)
            if (request.pricePaid != null && request.weightGrams != null && request.pricePaid > 0.0 && request.weightGrams > 0.0) {
                savePrice(request)
            }
        }
    }

    private fun saveReview(request: ReviewSaveRequest) {
        val endpoint = "${SupabaseConfig.BASE_URL}/rest/v1/avaliacoes?on_conflict=${encode("cafe_id,usuario_id")}" 
        val body = JSONObject()
            .put("cafe_id", request.cafeId)
            .put("usuario_id", request.userId)
            .put("nota_geral", request.rating)
            .put("compraria_novamente", request.wouldBuyAgain)
            .put("metodo_preparo", request.brewMethod.ifBlank { "nao_informado" })

        if (request.comment.isBlank()) {
            body.put("comentario", JSONObject.NULL)
        } else {
            body.put("comentario", request.comment)
        }

        if (request.pricePaid != null && request.weightGrams != null && request.pricePaid > 0.0 && request.weightGrams > 0.0) {
            body.put("preco_pago", request.pricePaid)
            body.put("peso_g", request.weightGrams)
        }

        executePost(
            endpoint = endpoint,
            accessToken = request.accessToken,
            body = body,
            prefer = "resolution=merge-duplicates,return=representation"
        )
    }

    private fun savePrice(request: ReviewSaveRequest) {
        val body = JSONObject()
            .put("cafe_id", request.cafeId)
            .put("usuario_id", request.userId)
            .put("preco_pago", request.pricePaid)
            .put("peso_g", request.weightGrams)
            .put("origem_preco", "usuario")

        executePost(
            endpoint = "${SupabaseConfig.BASE_URL}/rest/v1/precos_cafe",
            accessToken = request.accessToken,
            body = body,
            prefer = "return=representation"
        )
    }

    private fun executePost(endpoint: String, accessToken: String, body: JSONObject, prefer: String) {
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 12_000
            readTimeout = 12_000
            doOutput = true
            setRequestProperty("apikey", SupabaseConfig.PUBLISHABLE_KEY)
            setRequestProperty("Authorization", "Bearer $accessToken")
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Prefer", prefer)
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
                throw IllegalStateException(parseErrorMessage(responseBody))
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun parseErrorMessage(responseBody: String): String {
        if (responseBody.isBlank()) return "Não foi possível salvar no Supabase."
        return try {
            val json = JSONObject(responseBody)
            val message = json.optString("message")
                .ifBlank { json.optString("msg") }
                .ifBlank { json.optString("hint") }
                .ifBlank { json.optString("details") }
            message.ifBlank { "Não foi possível salvar no Supabase." }
        } catch (_: Exception) {
            "Não foi possível salvar no Supabase."
        }
    }

    private fun encode(value: String): String {
        return URLEncoder.encode(value, StandardCharsets.UTF_8.toString())
    }
}

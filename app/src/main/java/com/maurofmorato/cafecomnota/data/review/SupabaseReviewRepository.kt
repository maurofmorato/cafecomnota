package com.maurofmorato.cafecomnota.data.review

import com.maurofmorato.cafecomnota.data.supabase.SupabaseConfig
import com.maurofmorato.cafecomnota.data.auth.AuthenticationExpiredException
import com.maurofmorato.cafecomnota.data.auth.isAuthenticationExpired
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SupabaseReviewRepository {
    suspend fun loadExistingReviewForUser(
        cafeId: String,
        userId: String,
        accessToken: String
    ): ExistingReviewData? {
        return withContext(Dispatchers.IO) {
            val review = loadReviewRow(
                cafeId = cafeId,
                userId = userId,
                accessToken = accessToken
            )

            val currentPrice = loadCurrentPriceRow(
                cafeId = cafeId,
                userId = userId,
                accessToken = accessToken
            )

            val result = ExistingReviewData(
                rating = review?.optNullableDouble("nota_geral"),
                wouldBuyAgain = review?.optNullableBoolean("compraria_novamente"),
                aroma = review?.optNullableInt("aroma"),
                flavor = review?.optNullableInt("sabor"),
                body = review?.optNullableInt("corpo"),
                bitterness = review?.optNullableInt("amargor"),
                acidity = review?.optNullableInt("acidez"),
                sweetness = review?.optNullableInt("docura"),
                valueRating = review?.optNullableInt("custo_beneficio"),
                pricePaid = currentPrice?.optNullableDouble("preco_pago")
                    ?: review?.optNullableDouble("preco_pago"),
                weightGrams = currentPrice?.optNullableDouble("peso_g")
                    ?: review?.optNullableDouble("peso_g"),
                brewMethod = review?.optNullableString("metodo_preparo"),
                comment = review?.optNullableString("comentario")
            )

            result.takeIf { it.hasAnyData }
        }
    }

    suspend fun saveReviewAndOptionalPrice(
        request: ReviewSaveRequest
    ) {
        withContext(Dispatchers.IO) {
            saveReview(request)

            if (
                request.pricePaid != null &&
                request.weightGrams != null &&
                request.pricePaid > 0.0 &&
                request.weightGrams > 0.0
            ) {
                saveCurrentPrice(request)
                savePriceHistory(request)
            }
        }
    }

    private fun loadReviewRow(
        cafeId: String,
        userId: String,
        accessToken: String
    ): JSONObject? {
        val query = listOf(
            "select=nota_geral,compraria_novamente,aroma,sabor,corpo,amargor,acidez,docura,custo_beneficio,metodo_preparo,comentario,preco_pago,peso_g,created_at,updated_at",
            "cafe_id=eq.${encode(cafeId)}",
            "usuario_id=eq.${encode(userId)}",
            "limit=1"
        ).joinToString("&")

        val endpoint = "${SupabaseConfig.BASE_URL}/rest/v1/avaliacoes?$query"
        return executeGetArray(
            endpoint = endpoint,
            accessToken = accessToken
        ).firstObjectOrNull()
    }

    private fun loadCurrentPriceRow(
        cafeId: String,
        userId: String,
        accessToken: String
    ): JSONObject? {
        val query = listOf(
            "select=preco_pago,peso_g,preco_kg,preco_250g,data_preco,created_at,updated_at,moeda",
            "cafe_id=eq.${encode(cafeId)}",
            "usuario_id=eq.${encode(userId)}",
            "limit=1"
        ).joinToString("&")

        val endpoint = "${SupabaseConfig.BASE_URL}/rest/v1/precos_cafe?$query"
        return executeGetArray(
            endpoint = endpoint,
            accessToken = accessToken
        ).firstObjectOrNull()
    }

    private fun saveReview(
        request: ReviewSaveRequest
    ) {
        val endpoint =
            "${SupabaseConfig.BASE_URL}/rest/v1/avaliacoes?on_conflict=${encode("cafe_id,usuario_id")}"

        val body = JSONObject()
            .put("cafe_id", request.cafeId)
            .put("usuario_id", request.userId)
            .put("nota_geral", request.rating)
            .put("compraria_novamente", request.wouldBuyAgain)
            .put("metodo_preparo", request.brewMethod.ifBlank { "nao_informado" })

        body.putNullableInt("aroma", request.aroma)
        body.putNullableInt("sabor", request.flavor)
        body.putNullableInt("corpo", request.body)
        body.putNullableInt("amargor", request.bitterness)
        body.putNullableInt("acidez", request.acidity)
        body.putNullableInt("docura", request.sweetness)
        body.putNullableInt("custo_beneficio", request.valueRating)

        if (request.comment.isBlank()) {
            body.put("comentario", JSONObject.NULL)
        } else {
            body.put("comentario", request.comment)
        }

        if (
            request.pricePaid != null &&
            request.weightGrams != null &&
            request.pricePaid > 0.0 &&
            request.weightGrams > 0.0
        ) {
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

    private fun saveCurrentPrice(
        request: ReviewSaveRequest
    ) {
        val endpoint =
            "${SupabaseConfig.BASE_URL}/rest/v1/precos_cafe?on_conflict=${encode("cafe_id,usuario_id")}"

        val body = JSONObject()
            .put("cafe_id", request.cafeId)
            .put("usuario_id", request.userId)
            .put("preco_pago", request.pricePaid)
            .put("peso_g", request.weightGrams)
            .put("data_preco", todayIsoDate())
            .put("origem_preco", "usuario")
            .put("moeda", "BRL")

        executePost(
            endpoint = endpoint,
            accessToken = request.accessToken,
            body = body,
            prefer = "resolution=merge-duplicates,return=representation"
        )
    }

    private fun savePriceHistory(
        request: ReviewSaveRequest
    ) {
        val endpoint = "${SupabaseConfig.BASE_URL}/rest/v1/precos_cafe_historico"

        val body = JSONObject()
            .put("cafe_id", request.cafeId)
            .put("usuario_id", request.userId)
            .put("preco_pago", request.pricePaid)
            .put("peso_g", request.weightGrams)
            .put("data_preco", todayIsoDate())
            .put("origem_preco", "usuario")
            .put("moeda", "BRL")

        executePost(
            endpoint = endpoint,
            accessToken = request.accessToken,
            body = body,
            prefer = "return=representation"
        )
    }

    private fun executeGetArray(
        endpoint: String,
        accessToken: String
    ): JSONArray {
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 12_000
            readTimeout = 12_000

            setRequestProperty("apikey", SupabaseConfig.PUBLISHABLE_KEY)
            setRequestProperty("Authorization", "Bearer $accessToken")
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Content-Type", "application/json")
        }

        try {
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

            return JSONArray(responseBody.ifBlank { "[]" })
        } finally {
            connection.disconnect()
        }
    }

    private fun executePost(
        endpoint: String,
        accessToken: String,
        body: JSONObject,
        prefer: String
    ) {
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
                if (isAuthenticationExpired(statusCode, responseBody)) {
                    throw AuthenticationExpiredException()
                }
                throw IllegalStateException(parseErrorMessage(responseBody))
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun parseErrorMessage(
        responseBody: String
    ): String {
        // A resposta do banco pode conter SQL, JWT ou detalhes internos.
        // A tela exibe apenas uma orientação segura ao usuário.
        return "Não foi possível concluir esta ação agora. Tente novamente."
    }

    private fun encode(value: String): String {
        return URLEncoder.encode(
            value,
            StandardCharsets.UTF_8.toString()
        )
    }

    private fun todayIsoDate(): String {
        return SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.US
        ).format(Date())
    }

    private fun JSONArray.firstObjectOrNull(): JSONObject? {
        return if (length() > 0) {
            optJSONObject(0)
        } else {
            null
        }
    }

    private fun JSONObject.optNullableString(name: String): String? {
        if (isNull(name)) return null
        return optString(name).takeIf { it.isNotBlank() }
    }

    private fun JSONObject.optNullableDouble(name: String): Double? {
        if (isNull(name)) return null
        return try {
            getDouble(name)
        } catch (_: Exception) {
            null
        }
    }

    private fun JSONObject.optNullableBoolean(name: String): Boolean? {
        if (isNull(name)) return null
        return try {
            getBoolean(name)
        } catch (_: Exception) {
            null
        }
    }

    private fun JSONObject.optNullableInt(name: String): Int? {
        if (isNull(name)) return null
        return try {
            getInt(name)
        } catch (_: Exception) {
            null
        }
    }

    private fun JSONObject.putNullableInt(
        name: String,
        value: Int?
    ) {
        put(name, value ?: JSONObject.NULL)
    }
}

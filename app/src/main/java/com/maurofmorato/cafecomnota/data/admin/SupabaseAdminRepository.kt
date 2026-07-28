package com.maurofmorato.cafecomnota.data.admin

import com.maurofmorato.cafecomnota.data.supabase.SupabaseConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class SupabaseAdminRepository {
    suspend fun loadCoffeesForAdministration(
        accessToken: String,
        mineOnlyUserId: String? = null
    ): List<ModerationCoffee> = withContext(Dispatchers.IO) {
        val filter = mineOnlyUserId?.let { "&cadastrado_por=eq.${encode(it)}" }.orEmpty()
        val endpoint = "${SupabaseConfig.BASE_URL}/rest/v1/cafes?select=" +
            "id,nome,marca,status,cadastrado_em,cadastrado_por,motivo_moderacao," +
            "fotos_esperadas,fotos_enviadas,fotos_status&order=cadastrado_em.desc$filter"
        val response = executeRequest(endpoint, accessToken, "GET", JSONObject(), null)
        val array = JSONArray(response)
        buildList {
            for (index in 0 until array.length()) {
                val item = array.getJSONObject(index)
                add(
                    ModerationCoffee(
                        id = item.optString("id"),
                        name = item.optString("nome"),
                        brand = item.optString("marca"),
                        status = item.optString("status", "pendente"),
                        createdAt = item.optString("cadastrado_em"),
                        authorId = item.optString("cadastrado_por"),
                        moderationReason = item.optNullableString("motivo_moderacao"),
                        expectedPhotos = item.optInt("fotos_esperadas"),
                        uploadedPhotos = item.optInt("fotos_enviadas"),
                        photosStatus = item.optString("fotos_status", "nao_solicitada")
                    )
                )
            }
        }
    }

    suspend fun isCurrentUserAdmin(
        accessToken: String
    ): Boolean {
        return withContext(Dispatchers.IO) {
            val endpoint = "${SupabaseConfig.BASE_URL}/rest/v1/rpc/usuario_atual_is_admin"
            val response = executeRequest(
                endpoint = endpoint,
                accessToken = accessToken,
                method = "POST",
                body = JSONObject(),
                prefer = null
            )

            response.trim().equals(
                other = "true",
                ignoreCase = true
            )
        }
    }

    suspend fun updateCoffeeStatus(
        coffeeId: String,
        accessToken: String,
        newStatus: String,
        reason: String
    ) {
        withContext(Dispatchers.IO) {
            val endpoint =
                "${SupabaseConfig.BASE_URL}/rest/v1/cafes?id=eq.${encode(coffeeId)}"

            val body = JSONObject()
                .put("status", newStatus)

            if (reason.isBlank()) {
                body.put("motivo_moderacao", JSONObject.NULL)
            } else {
                body.put("motivo_moderacao", reason)
            }

            executeRequest(
                endpoint = endpoint,
                accessToken = accessToken,
                method = "PATCH",
                body = body,
                prefer = "return=minimal"
            )
        }
    }

    suspend fun updateCoffeeBasics(
        coffeeId: String,
        accessToken: String,
        name: String,
        brand: String
    ) = withContext(Dispatchers.IO) {
        val endpoint = "${SupabaseConfig.BASE_URL}/rest/v1/cafes?id=eq.${encode(coffeeId)}"
        executeRequest(
            endpoint = endpoint,
            accessToken = accessToken,
            method = "PATCH",
            body = JSONObject().put("nome", name.trim()).put("marca", brand.trim()),
            prefer = "return=minimal"
        )
    }

    private fun executeRequest(
        endpoint: String,
        accessToken: String,
        method: String,
        body: JSONObject,
        prefer: String?
    ): String {
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 12_000
            readTimeout = 12_000
            doOutput = method != "GET"

            setRequestProperty("apikey", SupabaseConfig.PUBLISHABLE_KEY)
            setRequestProperty("Authorization", "Bearer $accessToken")
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")

            if (!prefer.isNullOrBlank()) {
                setRequestProperty("Prefer", prefer)
            }
        }

        try {
            if (method != "GET") {
                connection.outputStream.use { output ->
                    output.write(body.toString().toByteArray(Charsets.UTF_8))
                }
            }

            val statusCode = connection.responseCode
            val responseBody = if (statusCode == HttpURLConnection.HTTP_NO_CONTENT) {
                ""
            } else if (statusCode in 200..299) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
            }

            if (statusCode !in 200..299) {
                throw IllegalStateException(parseErrorMessage(responseBody))
            }

            return responseBody
        } finally {
            connection.disconnect()
        }
    }

    private fun parseErrorMessage(
        responseBody: String
    ): String {
        if (responseBody.isBlank()) {
            return "Operação administrativa não autorizada."
        }

        return try {
            val json = JSONObject(responseBody)

            val message = json.optString("message")
                .ifBlank { json.optString("msg") }
                .ifBlank { json.optString("hint") }
                .ifBlank { json.optString("details") }

            message.ifBlank {
                "Operação administrativa não autorizada."
            }
        } catch (_: Exception) {
            "Operação administrativa não autorizada."
        }
    }

    private fun encode(
        value: String
    ): String {
        return URLEncoder.encode(
            value,
            StandardCharsets.UTF_8.toString()
        )
    }

    private fun JSONObject.optNullableString(
        name: String
    ): String {
        if (isNull(name)) return ""

        return optString(name)
            .takeUnless { it.equals("null", ignoreCase = true) }
            .orEmpty()
    }
}

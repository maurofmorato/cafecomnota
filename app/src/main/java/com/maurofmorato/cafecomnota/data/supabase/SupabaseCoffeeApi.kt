package com.maurofmorato.cafecomnota.data.supabase

import android.util.Log
import com.maurofmorato.cafecomnota.ui.model.CoffeeUiModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class SupabaseCoffeeApi {
    suspend fun loadCoffeeSummaries(): List<CoffeeUiModel> {
        return withContext(Dispatchers.IO) {
            val query = buildQuery()
            val url = URL("${SupabaseConfig.COFFEES_SUMMARY_ENDPOINT}?$query")

            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 12_000
                readTimeout = 12_000

                setRequestProperty("apikey", SupabaseConfig.PUBLISHABLE_KEY)
                setRequestProperty("Authorization", "Bearer ${SupabaseConfig.PUBLISHABLE_KEY}")
                setRequestProperty("Accept", "application/json")
                setRequestProperty("Content-Type", "application/json")
            }

            try {
                val statusCode = connection.responseCode
                val body = if (statusCode in 200..299) {
                    connection.inputStream.bufferedReader().use { it.readText() }
                } else {
                    connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
                }

                if (statusCode !in 200..299) {
                    throw IllegalStateException(
                        "Supabase retornou HTTP $statusCode: ${body.take(300)}"
                    )
                }

                parseCoffeeSummaries(body)
            } finally {
                connection.disconnect()
            }
        }
    }

    private fun buildQuery(): String {
        val select = encode("*")
        val order = encode("nota_media.desc.nullslast,total_avaliacoes.desc.nullslast")

        return "select=$select&order=$order"
    }

    private fun encode(value: String): String {
        return URLEncoder.encode(
            value,
            StandardCharsets.UTF_8.toString()
        )
    }

    private fun parseCoffeeSummaries(json: String): List<CoffeeUiModel> {
        val array = JSONArray(json)
        val coffees = mutableListOf<CoffeeUiModel>()

        for (index in 0 until array.length()) {
            val item = array.getJSONObject(index)
            val coffee = item.toCoffeeUiModel()

            if (coffee.name.isNotBlank()) {
                coffees.add(coffee)
            }
        }

        Log.d(
            "SupabaseCoffeeApi",
            "cafes carregados do Supabase=${coffees.size}"
        )

        return coffees
    }

    private fun JSONObject.toCoffeeUiModel(): CoffeeUiModel {
        val id = optStringOrNull("id")
            ?: optStringOrNull("cafe_id")
            ?: "supabase_${hashCode()}"

        val name = optStringOrNull("nome")
            ?: optStringOrNull("name")
            ?: optStringOrNull("cafe")
            ?: ""

        val brand = optStringOrNull("marca")
            ?: optStringOrNull("torrefacao")
            ?: optStringOrNull("brand")
            ?: "Marca não informada"

        val type = optStringOrNull("tipo_cafe")
            ?: optStringOrNull("tipo")
            ?: optStringOrNull("type")
            ?: "Café"

        val roast = optStringOrNull("torra")
            ?: optStringOrNull("roast")
            ?: "Torra não informada"

        val rating = optDoubleOrNull("nota_media")
            ?: optDoubleOrNull("rating")
            ?: optDoubleOrNull("media_nota")
            ?: 0.0

        val totalReviews = optIntOrNull("total_avaliacoes")
            ?: optIntOrNull("reviews")
            ?: optIntOrNull("qtd_avaliacoes")
            ?: 0

        val priceKg = optDoubleOrNull("preco_medio_por_kg")
            ?: optDoubleOrNull("preco_por_kg_medio")
            ?: optDoubleOrNull("price_kg")
            ?: 0.0

        val wouldBuyAgainPercent = optIntOrNull("percentual_compraria_novamente")
            ?: optIntOrNull("compraria_novamente_percentual")
            ?: optIntOrNull("would_buy_again_percent")
            ?: 0

        val normalizedRating = if (rating > 0.0) {
            rating.coerceIn(1.0, 5.0)
        } else {
            0.0
        }

        val valueRating = when {
            priceKg <= 0.0 && normalizedRating <= 0.0 -> 0.0
            priceKg <= 0.0 -> normalizedRating
            priceKg <= 60.0 -> (normalizedRating + 0.5).coerceAtMost(5.0)
            priceKg <= 90.0 -> normalizedRating
            else -> (normalizedRating - 0.3).coerceAtLeast(0.0)
        }

        return CoffeeUiModel(
            id = id,
            name = name,
            brand = brand,
            type = type,
            roast = roast,
            rating = normalizedRating,
            totalReviews = totalReviews,
            priceKg = priceKg,
            wouldBuyAgainPercent = wouldBuyAgainPercent,
            description = buildDescription(
                name = name,
                brand = brand,
                totalReviews = totalReviews
            ),
            tags = buildTags(
                type = type,
                roast = roast,
                rating = normalizedRating,
                priceKg = priceKg
            ),
            aroma = normalizedRating,
            flavor = normalizedRating,
            body = normalizedRating,
            acidity = if (normalizedRating > 0.0) {
                (normalizedRating - 0.2).coerceAtLeast(1.0)
            } else {
                0.0
            },
            bitterness = if (normalizedRating > 0.0) {
                (5.2 - normalizedRating).coerceIn(1.0, 5.0)
            } else {
                0.0
            },
            sweetness = if (normalizedRating > 0.0) {
                (normalizedRating - 0.1).coerceAtLeast(1.0)
            } else {
                0.0
            },
            valueRating = valueRating
        )
    }

    private fun JSONObject.optStringOrNull(name: String): String? {
        if (!has(name) || isNull(name)) {
            return null
        }

        val value = optString(name).trim()
        return value.ifBlank {
            null
        }
    }

    private fun JSONObject.optDoubleOrNull(name: String): Double? {
        if (!has(name) || isNull(name)) {
            return null
        }

        return try {
            when (val value = get(name)) {
                is Number -> value.toDouble()
                is String -> value
                    .replace(",", ".")
                    .toDoubleOrNull()

                else -> null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun JSONObject.optIntOrNull(name: String): Int? {
        if (!has(name) || isNull(name)) {
            return null
        }

        return try {
            when (val value = get(name)) {
                is Number -> value.toInt()
                is String -> value.toIntOrNull()
                else -> null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun buildTags(
        type: String,
        roast: String,
        rating: Double,
        priceKg: Double
    ): List<String> {
        val tags = mutableListOf<String>()

        if (type.isNotBlank()) {
            tags.add(type)
        }

        if (roast.isNotBlank()) {
            tags.add(roast)
        }

        if (rating >= 4.5) {
            tags.add("Bem avaliado")
        }

        if (priceKg > 0.0 && priceKg <= 60.0) {
            tags.add("Bom custo-benefício")
        }

        return tags.distinct().take(4)
    }

    private fun buildDescription(
        name: String,
        brand: String,
        totalReviews: Int
    ): String {
        return if (totalReviews > 0) {
            "$name, da marca $brand, já possui avaliações reais na base do Café com nota."
        } else {
            "$name, da marca $brand, está cadastrado na base do Café com nota e aguardando as primeiras avaliações."
        }
    }
}

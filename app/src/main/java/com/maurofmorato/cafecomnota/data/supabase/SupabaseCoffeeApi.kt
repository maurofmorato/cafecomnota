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
            val coffees = parseCoffeeSummaries(
                executeGet("${SupabaseConfig.COFFEES_SUMMARY_ENDPOINT}?${buildQuery()}")
            )
            val imagePaths = runCatching { loadCoffeeImagePaths() }
                .onFailure { error ->
                    Log.w("SupabaseCoffeeApi", "Fotos do catálogo indisponíveis", error)
                }
                .getOrDefault(emptyMap())

            coffees.map { coffee ->
                coffee.copy(imagePath = imagePaths[coffee.id])
            }
        }
    }

    /**
     * A ordem respeita a regra visual do app: frente, outra imagem cadastrada
     * e, quando não houver foto acessível, o pacote neutro na interface.
     *
     * A consulta continua sob RLS; não torna o bucket do Storage público.
     */
    private fun loadCoffeeImagePaths(): Map<String, String> {
        val query = "select=${encode("cafe_id,storage_path,rotulo,ordem")}&order=${encode("cafe_id.asc,ordem.asc")}"
        val array = JSONArray(
            executeGet("${SupabaseConfig.BASE_URL}/rest/v1/cafe_fotos?$query")
        )
        val photosByCoffee = mutableMapOf<String, MutableList<CoffeePhotoReference>>()

        for (index in 0 until array.length()) {
            val item = array.optJSONObject(index) ?: continue
            val cafeId = item.optString("cafe_id").trim()
            val path = item.optString("storage_path").trim()
            if (cafeId.isBlank() || path.isBlank()) continue

            photosByCoffee.getOrPut(cafeId) { mutableListOf() }.add(
                CoffeePhotoReference(
                    storagePath = path,
                    label = item.optString("rotulo").trim(),
                    order = item.optInt("ordem", Int.MAX_VALUE)
                )
            )
        }

        return photosByCoffee.mapValues { (_, photos) ->
            photos.sortedWith(
                compareBy<CoffeePhotoReference> {
                    if (it.label.equals("frente", ignoreCase = true)) 0 else 1
                }.thenBy { it.order }
            ).first().storagePath
        }
    }

    private fun executeGet(endpoint: String): String {
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
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
                Log.w("SupabaseCoffeeApi", "Falha HTTP $statusCode ao consultar catálogo: ${body.take(240)}")
                throw IllegalStateException("Não foi possível atualizar o catálogo agora.")
            }

            return body
        } finally {
            connection.disconnect()
        }
    }

    private data class CoffeePhotoReference(
        val storagePath: String,
        val label: String,
        val order: Int
    )

    private fun buildQuery(): String {
        val select = encode("*")
        val order = encode("total_avaliacoes.desc.nullslast,nota_media.desc.nullslast,nome.asc")

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
        val id = optStringOrNull("cafe_id")
            ?: optStringOrNull("id")
            ?: optStringOrNull("codigo")
            ?: "supabase_${hashCode()}"

        val name = optStringOrNull("nome")
            ?: optStringOrNull("name")
            ?: optStringOrNull("cafe")
            ?: ""

        val brand = optStringOrNull("marca")
            ?: optStringOrNull("torrefacao")
            ?: optStringOrNull("brand")
            ?: "Marca não informada"

        val rawType = optStringOrNull("tipo_cafe")
            ?: optStringOrNull("tipo")
            ?: optStringOrNull("type")
            ?: "cafe"

        val rawRoast = optStringOrNull("torra")
            ?: optStringOrNull("roast")
            ?: "nao_informada"

        val type = displayCoffeeType(rawType)
        val roast = displayRoast(rawRoast)
        val category = optStringOrNull("categoria")
        val certification = optStringOrNull("certificacao")
        val sourceLabel = optStringOrNull("fonte_dado")

        val rating = optDoubleOrNull("nota_media")
            ?: optDoubleOrNull("rating")
            ?: optDoubleOrNull("media_nota")
            ?: 0.0

        val totalReviews = optIntOrNull("total_avaliacoes")
            ?: optIntOrNull("reviews")
            ?: optIntOrNull("qtd_avaliacoes")
            ?: 0

        val priceKg = optDoubleOrNull("preco_kg_medio")
            ?: optDoubleOrNull("preco_medio_por_kg")
            ?: optDoubleOrNull("preco_por_kg_medio")
            ?: optDoubleOrNull("price_kg")
            ?: 0.0

        val price250g = optDoubleOrNull("preco_250g_medio")
            ?: if (priceKg > 0.0) priceKg / 4.0 else 0.0

        val lastPriceDate = optStringOrNull("ultimo_preco_em")
        val totalPriceRecords = optIntOrNull("total_precos") ?: 0

        val wouldBuyAgainPercent = optIntOrNull("percentual_compraria_novamente")
            ?: optIntOrNull("compraria_novamente_percentual")
            ?: optIntOrNull("would_buy_again_percent")
            ?: 0

        val normalizedRating = if (rating > 0.0 && totalReviews > 0) {
            rating.coerceIn(1.0, 5.0)
        } else {
            0.0
        }

        val detailedAroma = optDetailedRating("aroma_media")
        val detailedFlavor = optDetailedRating("sabor_media")
        val detailedBody = optDetailedRating("corpo_media")
        val detailedBitterness = optDetailedRating("amargor_media")
        val detailedAcidity = optDetailedRating("acidez_media")
        val detailedSweetness = optDetailedRating("docura_media")
        val detailedValueRating = optDetailedRating("custo_beneficio_media")
        val hasDetailedRatings = listOf(
            detailedAroma,
            detailedFlavor,
            detailedBody,
            detailedBitterness,
            detailedAcidity,
            detailedSweetness,
            detailedValueRating
        ).any { value -> value != null }

        val tags = buildTags(type, roast, category, certification, normalizedRating, priceKg)

        val calculatedValueRating = when {
            normalizedRating <= 0.0 -> 0.0
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
            description = buildDescription(name, brand, totalReviews, totalPriceRecords),
            tags = tags,
            aroma = detailedAroma ?: 0.0,
            flavor = detailedFlavor ?: 0.0,
            body = detailedBody ?: 0.0,
            acidity = detailedAcidity ?: 0.0,
            bitterness = detailedBitterness ?: 0.0,
            sweetness = detailedSweetness ?: 0.0,
            valueRating = detailedValueRating ?: calculatedValueRating,
            hasDetailedRatings = hasDetailedRatings,
            price250g = price250g,
            lastPriceDate = lastPriceDate,
            totalPriceRecords = totalPriceRecords,
            productLabel = optStringOrNull("produto_rotulo"),
            producer = optStringOrNull("produtor"),
            originRegion = optStringOrNull("origem_regiao"),
            altitudeMeters = optIntOrNull("altitude_m"),
            variety = optStringOrNull("variedade"),
            process = optStringOrNull("processo"),
            scaScoreText = optStringOrNull("pontuacao_sca_texto"),
            bodyDescription = optStringOrNull("corpo_descricao"),
            aromaFlavor = optStringOrNull("aroma_sabor"),
            acidityDescription = optStringOrNull("acidez_descricao"),
            certification = certification,
            dataSourceLabel = displaySourceLabel(sourceLabel)
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

    private fun JSONObject.optDetailedRating(name: String): Double? {
        return optDoubleOrNull(name)?.takeIf { value ->
            value in 1.0..5.0
        }
    }

    private fun buildTags(
        type: String,
        roast: String,
        category: String?,
        certification: String?,
        rating: Double,
        priceKg: Double
    ): List<String> {
        val tags = mutableListOf<String>()

        if (type.isNotBlank()) tags.add(type)
        if (roast.isNotBlank()) tags.add(roast)
        if (!category.isNullOrBlank()) tags.add(category)
        if (!certification.isNullOrBlank()) tags.add(certification)
        if (rating >= 4.5) tags.add("Bem avaliado")
        if (priceKg > 0.0 && priceKg <= 60.0) tags.add("Bom custo-benefício")

        return tags.distinct().take(4)
    }

    private fun buildDescription(
        name: String,
        brand: String,
        totalReviews: Int,
        totalPriceRecords: Int
    ): String {
        return when {
            totalReviews > 0 && totalPriceRecords > 0 -> "$name, da marca $brand, já possui avaliações e registros de preço na base do Café com nota."
            totalReviews > 0 -> "$name, da marca $brand, já possui avaliações reais na base do Café com nota."
            else -> "$name, da marca $brand, está cadastrado como informação de catálogo e aguardando as primeiras avaliações."
        }
    }

    private fun displayCoffeeType(value: String): String {
        return when (value.lowercase().trim()) {
            "grao", "graos", "grão", "grãos" -> "Grãos"
            "moido", "moído" -> "Moído"
            "capsula", "capsulas", "cápsula", "cápsulas" -> "Cápsula"
            else -> value.replaceFirstChar { char ->
                char.titlecase()
            }
        }
    }

    private fun displayRoast(value: String): String {
        return when (value.lowercase().trim()) {
            "clara" -> "Clara"
            "media", "média" -> "Média"
            "media_clara", "média clara" -> "Média clara"
            "media_escura", "média escura" -> "Média escura"
            "escura" -> "Escura"
            "nao_informada", "não informada" -> "Torra não informada"
            else -> value.replace("_", " ").replaceFirstChar { char ->
                char.titlecase()
            }
        }
    }

    private fun displaySourceLabel(value: String?): String? {
        return when (value?.lowercase()?.trim()) {
            null, "" -> null
            "embalagem" -> "Embalagem"
            "catalogo_manual_inicial" -> "Catálogo inicial"
            "abic" -> "ABIC"
            "bsca" -> "BSCA"
            "sca" -> "SCA"
            else -> value.replace("_", " ").replaceFirstChar { char ->
                char.titlecase()
            }
        }
    }
}

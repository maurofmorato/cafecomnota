package com.maurofmorato.cafecomnota.ui.screens

import com.maurofmorato.cafecomnota.ui.model.CoffeeUiModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CoffeeLabelSuggestionTest {
    @Test
    fun `prefere produto exato do catalogo e ignora texto do navegador`() {
        val result = coffeeLabelSuggestion(
            text = """
                Café com nota
                Compre pelo App Baggio
                Baixar App
                Café torrado e moído
                Sabor chocolate trufado
                BAGGIO
                CAFÉ
                AROMAS
                Chocolate
                Trufado
                250g
            """.trimIndent(),
            existingCoffees = listOf(
                coffee(name = "Baggio Bourbon", brand = "Baggio"),
                coffee(name = "Baggio Chocolate Trufado", brand = "Baggio")
            )
        )

        assertEquals("Baggio Chocolate Trufado", result.name)
        assertEquals("Baggio", result.brand)
        assertEquals(250, result.weightGrams)
        assertEquals("moido", result.type)
        assertEquals("media", result.roast)
        assertTrue(result.matchedCatalog)
    }

    @Test
    fun `nao transforma palavra forte isolada em torra escura`() {
        val result = coffeeLabelSuggestion(
            text = """
                Café com nota
                MARCA NOVA
                Café de sabor forte e marcante
                Torrado e moído
                250g
            """.trimIndent(),
            existingCoffees = emptyList()
        )

        assertFalse(result.brand.contains("Café com", ignoreCase = true))
        assertEquals("moido", result.type)
        assertNull(result.roast)
    }

    @Test
    fun `combina frente e verso e extrai ficha tecnica declarada`() {
        val result = coffeeLabelSuggestion(
            text = """
                CAFÉ SALOMÃO
                BOURBON
                100% ARÁBICA
                EQUILIBRADO COM NOTAS DE CARAMELO E CHOCOLATE
                TORRA MÉDIA
                CAFÉ TORRADO EM GRÃO
                250g
                Origem: Cerrado Mineiro MG
                Fazenda Santa Clara
                Altitude 1100m
                Processo natural
                Certificação Orgânico
            """.trimIndent(),
            existingCoffees = emptyList()
        )

        assertEquals(250, result.weightGrams)
        assertEquals("grao", result.type)
        assertEquals("media", result.roast)
        assertEquals(1100, result.altitudeMeters)
        assertTrue(result.variety.orEmpty().contains("arabica", ignoreCase = true))
        assertTrue(result.process.orEmpty().contains("natural", ignoreCase = true))
        assertTrue(result.aromaFlavor.orEmpty().contains("caramelo", ignoreCase = true))
        assertTrue(result.certification.orEmpty().contains("organico", ignoreCase = true))
    }

    private fun coffee(name: String, brand: String) = CoffeeUiModel(
        id = name,
        name = name,
        brand = brand,
        type = "Moído",
        roast = "Média",
        rating = 0.0,
        totalReviews = 0,
        priceKg = 0.0,
        wouldBuyAgainPercent = 0,
        description = "",
        tags = emptyList(),
        aroma = 0.0,
        flavor = 0.0,
        body = 0.0,
        acidity = 0.0,
        bitterness = 0.0,
        sweetness = 0.0,
        valueRating = 0.0
    )
}

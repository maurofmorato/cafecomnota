package com.maurofmorato.cafecomnota.ui.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CoffeeRankingTest {
    @Test
    fun bayesianRankingValuesMoreReviewsWhenAveragesAreClose() {
        val oneReview = coffee(name = "Uma avaliação", rating = 4.5, reviews = 1)
        val fourReviews = coffee(name = "Quatro avaliações", rating = 4.4, reviews = 4)

        val oneReviewScore = bayesianRankingScore(oneReview, catalogAverage = 4.3)
        val fourReviewsScore = bayesianRankingScore(fourReviews, catalogAverage = 4.3)

        assertTrue(fourReviewsScore > oneReviewScore)
    }

    @Test
    fun topRatedUsesBayesianScoreButPreservesOriginalRating() {
        val coffees = listOf(
            coffee(name = "Uma avaliação", rating = 4.5, reviews = 1),
            coffee(name = "Quatro avaliações", rating = 4.4, reviews = 4),
            coffee(name = "Referência", rating = 4.3, reviews = 20)
        )

        val ranked = topRatedCoffees(coffees)

        assertEquals("Quatro avaliações", ranked.first().name)
        assertEquals(4.4, ranked.first().rating, 0.0)
    }

    @Test
    fun bestValueUsesBayesianScoreWhenAveragesAreClose() {
        val coffees = listOf(
            coffee(
                name = "Uma avaliação",
                rating = 4.5,
                reviews = 1,
                valueRating = 4.5
            ),
            coffee(
                name = "Quatro avaliações",
                rating = 4.4,
                reviews = 4,
                valueRating = 4.4
            ),
            coffee(
                name = "Referência",
                rating = 4.3,
                reviews = 20,
                valueRating = 4.3
            )
        )

        val ranked = bestValueCoffees(coffees)

        assertEquals("Quatro avaliações", ranked.first().name)
        assertEquals(4.4, ranked.first().valueRating, 0.0)
    }

    @Test
    fun bestValueIgnoresCoffeeWithoutValueRating() {
        val ranked = bestValueCoffees(
            listOf(
                coffee(
                    name = "Sem custo-benefício",
                    rating = 5.0,
                    reviews = 10,
                    valueRating = 0.0
                ),
                coffee(
                    name = "Com custo-benefício",
                    rating = 4.0,
                    reviews = 2,
                    valueRating = 4.0
                )
            )
        )

        assertEquals(listOf("Com custo-benefício"), ranked.map { it.name })
    }

    @Test
    fun valueBayesianScoreUsesOnlyCompletedValueRatings() {
        val coffee = coffee(
            name = "Avaliações antigas",
            rating = 4.5,
            reviews = 100,
            valueRating = 5.0,
            valueReviews = 1
        )

        val score = bayesianValueRankingScore(
            coffee = coffee,
            catalogAverage = 4.0,
            minimumReliableReviews = 5.0
        )

        assertEquals(4.166666, score, 0.000001)
    }

    @Test
    fun bestValueUsesNeutralPriorWhileCatalogIsStillSmall() {
        val coffees = listOf(
            coffee(
                name = "Uma opinião",
                rating = 4.0,
                reviews = 1,
                valueRating = 5.0,
                valueReviews = 1
            ),
            coffee(
                name = "Dez opiniões",
                rating = 5.0,
                reviews = 10,
                valueRating = 4.0,
                valueReviews = 10
            )
        )

        val ranked = bestValueCoffees(coffees)

        assertEquals("Dez opiniões", ranked.first().name)
        assertEquals(3.0, NEUTRAL_VALUE_PRIOR, 0.0)
    }

    @Test
    fun bestValueUsesLowerPriceOnlyAsTieBreaker() {
        val coffees = listOf(
            coffee(
                name = "Mais caro",
                rating = 4.0,
                reviews = 1,
                valueRating = 4.0,
                valueReviews = 1,
                priceKg = 120.0
            ),
            coffee(
                name = "Mais barato",
                rating = 4.0,
                reviews = 1,
                valueRating = 4.0,
                valueReviews = 1,
                priceKg = 65.0
            )
        )

        assertEquals("Mais barato", bestValueCoffees(coffees).first().name)
    }

    private fun coffee(
        name: String,
        rating: Double,
        reviews: Int,
        valueRating: Double = 0.0,
        valueReviews: Int = if (valueRating > 0.0) reviews else 0,
        priceKg: Double = 50.0
    ) = CoffeeUiModel(
        id = name,
        name = name,
        brand = "Marca",
        type = "Moído",
        roast = "Média",
        rating = rating,
        totalReviews = reviews,
        priceKg = priceKg,
        wouldBuyAgainPercent = 0,
        description = "",
        tags = emptyList(),
        aroma = 0.0,
        flavor = 0.0,
        body = 0.0,
        acidity = 0.0,
        bitterness = 0.0,
        sweetness = 0.0,
        valueRating = valueRating,
        totalValueReviews = valueReviews
    )
}

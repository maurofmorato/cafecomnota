package com.maurofmorato.cafecomnota.ui.model

data class CoffeeUiModel(
    val id: String,
    val name: String,
    val brand: String,
    val type: String,
    val roast: String,
    val rating: Double,
    val totalReviews: Int,
    val priceKg: Double,
    val wouldBuyAgainPercent: Int,
    val description: String,
    val tags: List<String>,
    val aroma: Double,
    val flavor: Double,
    val body: Double,
    val acidity: Double,
    val bitterness: Double,
    val sweetness: Double,
    val valueRating: Double,
    val price250g: Double = if (priceKg > 0.0) priceKg / 4.0 else 0.0,
    val lastPriceDate: String? = null,
    val totalPriceRecords: Int = 0
) {
    val hasRating: Boolean
        get() = rating > 0.0 && totalReviews > 0

    val hasPrice: Boolean
        get() = priceKg > 0.0
}

fun sampleCoffees(): List<CoffeeUiModel> {
    return listOf(
        CoffeeUiModel(
            id = "1",
            name = "Cerrado Mineiro Especial",
            brand = "Torra da Serra",
            type = "Moído",
            roast = "Média",
            rating = 4.8,
            totalReviews = 128,
            priceKg = 68.90,
            wouldBuyAgainPercent = 94,
            description = "Café equilibrado, com aroma marcante, baixa acidez e bom desempenho no coado.",
            tags = listOf(
                "Chocolate",
                "Baixa acidez",
                "Coado",
                "Custo-benefício"
            ),
            aroma = 4.7,
            flavor = 4.8,
            body = 4.5,
            acidity = 3.2,
            bitterness = 2.7,
            sweetness = 4.2,
            valueRating = 4.6,
            lastPriceDate = "2026-06-01",
            totalPriceRecords = 4
        ),

        CoffeeUiModel(
            id = "2",
            name = "Sul de Minas Gourmet",
            brand = "Café Boa Prosa",
            type = "Grãos",
            roast = "Média clara",
            rating = 4.6,
            totalReviews = 87,
            priceKg = 82.50,
            wouldBuyAgainPercent = 89,
            description = "Boa opção para quem gosta de café aromático, levemente adocicado e com finalização limpa.",
            tags = listOf(
                "Aromático",
                "Adocicado",
                "Grãos",
                "Especial"
            ),
            aroma = 4.8,
            flavor = 4.5,
            body = 4.1,
            acidity = 3.8,
            bitterness = 2.3,
            sweetness = 4.4,
            valueRating = 4.0,
            lastPriceDate = "2026-05-20",
            totalPriceRecords = 2
        ),

        CoffeeUiModel(
            id = "3",
            name = "Tradicional Forte",
            brand = "Mercado Bom",
            type = "Moído",
            roast = "Escura",
            rating = 3.7,
            totalReviews = 211,
            priceKg = 39.80,
            wouldBuyAgainPercent = 68,
            description = "Café popular, forte, de preço baixo, mas com amargor mais presente.",
            tags = listOf(
                "Forte",
                "Mercado",
                "Barato",
                "Amargor alto"
            ),
            aroma = 3.4,
            flavor = 3.5,
            body = 4.0,
            acidity = 2.6,
            bitterness = 4.2,
            sweetness = 2.4,
            valueRating = 4.2,
            lastPriceDate = "2026-06-10",
            totalPriceRecords = 6
        ),

        CoffeeUiModel(
            id = "4",
            name = "Orgânico Mantiqueira",
            brand = "Raiz Café",
            type = "Grãos",
            roast = "Média",
            rating = 4.9,
            totalReviews = 46,
            priceKg = 118.00,
            wouldBuyAgainPercent = 96,
            description = "Café de perfil especial, com notas doces, corpo agradável e preço mais alto.",
            tags = listOf(
                "Orgânico",
                "Especial",
                "Doce",
                "Premium"
            ),
            aroma = 4.9,
            flavor = 4.9,
            body = 4.7,
            acidity = 3.9,
            bitterness = 2.0,
            sweetness = 4.8,
            valueRating = 3.8,
            lastPriceDate = "2026-04-15",
            totalPriceRecords = 3
        )
    )
}

fun findCoffeeById(
    id: String,
    coffees: List<CoffeeUiModel> = sampleCoffees()
): CoffeeUiModel? {
    return coffees.firstOrNull { coffee ->
        coffee.id == id
    }
}

fun topRatedCoffees(
    coffees: List<CoffeeUiModel> = sampleCoffees()
): List<CoffeeUiModel> {
    return coffees
        .filter { coffee ->
            coffee.hasRating
        }
        .sortedWith(
            compareByDescending<CoffeeUiModel> { coffee ->
                coffee.rating
            }.thenByDescending { coffee ->
                coffee.totalReviews
            }
        )
}

fun bestValueCoffees(
    coffees: List<CoffeeUiModel> = sampleCoffees()
): List<CoffeeUiModel> {
    return coffees
        .filter { coffee ->
            coffee.hasRating
        }
        .sortedWith(
            compareByDescending<CoffeeUiModel> { coffee ->
                coffee.valueRating
            }.thenBy { coffee ->
                if (coffee.priceKg > 0.0) coffee.priceKg else Double.MAX_VALUE
            }
        )
}

fun mostReviewedCoffees(
    coffees: List<CoffeeUiModel> = sampleCoffees()
): List<CoffeeUiModel> {
    return coffees
        .filter { coffee ->
            coffee.hasRating
        }
        .sortedByDescending { coffee ->
            coffee.totalReviews
        }
}

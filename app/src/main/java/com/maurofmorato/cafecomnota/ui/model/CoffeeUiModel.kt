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
    val totalPriceRecords: Int = 0,
    val productLabel: String? = null,
    val producer: String? = null,
    val originRegion: String? = null,
    val altitudeMeters: Int? = null,
    val variety: String? = null,
    val process: String? = null,
    val scaScoreText: String? = null,
    val bodyDescription: String? = null,
    val aromaFlavor: String? = null,
    val acidityDescription: String? = null,
    val certification: String? = null,
    val dataSourceLabel: String? = null
) {
    val hasRating: Boolean
        get() = rating > 0.0 && totalReviews > 0

    val hasPrice: Boolean
        get() = priceKg > 0.0

    val hasTechnicalSheet: Boolean
        get() = listOf(
            productLabel,
            producer,
            originRegion,
            altitudeMeters?.toString(),
            variety,
            process,
            scaScoreText,
            bodyDescription,
            aromaFlavor,
            acidityDescription,
            certification,
            dataSourceLabel
        ).any { value ->
            !value.isNullOrBlank()
        }
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
            tags = listOf("Chocolate", "Baixa acidez", "Coado", "Custo-benefício"),
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
            name = "Black Tucano Honey Coffee",
            brand = "Black Tucano Coffee Roasters",
            type = "Grãos",
            roast = "Média",
            rating = 0.0,
            totalReviews = 0,
            priceKg = 0.0,
            wouldBuyAgainPercent = 0,
            description = "Café de catálogo especial, aguardando avaliações dos usuários.",
            tags = listOf("Grãos", "Média", "Especial", "SCA"),
            aroma = 0.0,
            flavor = 0.0,
            body = 0.0,
            acidity = 0.0,
            bitterness = 0.0,
            sweetness = 0.0,
            valueRating = 0.0,
            productLabel = "Café Black Tucano Honey Coffee Torrado e em Grãos 250g",
            producer = "Waldir Manske",
            originRegion = "Sítio Alto Santa Joana – Afonso Cláudio, Espírito Santo",
            altitudeMeters = 1100,
            variety = "Caturra Amarelo",
            process = "Honey Coffee",
            scaScoreText = "Acima de 86 pontos",
            bodyDescription = "Aveludado",
            aromaFlavor = "Melaço de cana, mel e framboesa",
            acidityDescription = "Média e arredondada",
            certification = "SCA",
            dataSourceLabel = "Embalagem"
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

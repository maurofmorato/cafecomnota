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
    val wouldBuyAgainPercent: Int
)

fun sampleCoffees(): List<CoffeeUiModel> {
    return listOf(
        CoffeeUiModel(
            id = "1",
            name = "Orfeu Intenso",
            brand = "Orfeu",
            type = "Grão",
            roast = "Média",
            rating = 4.6,
            totalReviews = 238,
            priceKg = 82.70,
            wouldBuyAgainPercent = 87
        ),
        CoffeeUiModel(
            id = "2",
            name = "3 Corações Gourmet",
            brand = "3 Corações",
            type = "Moído",
            roast = "Média",
            rating = 4.3,
            totalReviews = 184,
            priceKg = 58.90,
            wouldBuyAgainPercent = 81
        ),
        CoffeeUiModel(
            id = "3",
            name = "Melitta Especial",
            brand = "Melitta",
            type = "Moído",
            roast = "Média escura",
            rating = 4.1,
            totalReviews = 151,
            priceKg = 49.80,
            wouldBuyAgainPercent = 76
        ),
        CoffeeUiModel(
            id = "4",
            name = "Santa Monica Premium",
            brand = "Santa Monica",
            type = "Grão",
            roast = "Média",
            rating = 4.5,
            totalReviews = 97,
            priceKg = 96.40,
            wouldBuyAgainPercent = 84
        ),
        CoffeeUiModel(
            id = "5",
            name = "Pilão Tradicional",
            brand = "Pilão",
            type = "Moído",
            roast = "Escura",
            rating = 3.8,
            totalReviews = 312,
            priceKg = 37.90,
            wouldBuyAgainPercent = 68
        )
    )
}

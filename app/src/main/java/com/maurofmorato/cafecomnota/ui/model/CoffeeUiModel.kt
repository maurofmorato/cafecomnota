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
    val valueRating: Double,
    val wouldBuyAgainPercent: Int,
    val aroma: Double,
    val flavor: Double,
    val body: Double,
    val acidity: Double,
    val bitterness: Double,
    val sweetness: Double,
    val description: String,
    val tags: List<String>
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
            valueRating = 4.2,
            wouldBuyAgainPercent = 87,
            aroma = 4.5,
            flavor = 4.7,
            body = 4.4,
            acidity = 3.1,
            bitterness = 2.8,
            sweetness = 3.9,
            description = "Café encorpado, com boa presença no coado e no espresso. Perfil percebido mais intenso, com baixa acidez.",
            tags = listOf("Encorpado", "Chocolate", "Baixa acidez")
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
            valueRating = 4.4,
            wouldBuyAgainPercent = 81,
            aroma = 4.1,
            flavor = 4.2,
            body = 4.0,
            acidity = 3.0,
            bitterness = 3.2,
            sweetness = 3.5,
            description = "Boa opção de mercado, com preço mais acessível e avaliação consistente para o dia a dia.",
            tags = listOf("Custo-benefício", "Mercado", "Coado")
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
            valueRating = 4.3,
            wouldBuyAgainPercent = 76,
            aroma = 3.9,
            flavor = 4.0,
            body = 3.8,
            acidity = 2.7,
            bitterness = 3.4,
            sweetness = 3.1,
            description = "Café simples, fácil de encontrar e com boa relação de preço por kg.",
            tags = listOf("Popular", "Preço bom", "Dia a dia")
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
            valueRating = 3.9,
            wouldBuyAgainPercent = 84,
            aroma = 4.6,
            flavor = 4.5,
            body = 4.3,
            acidity = 3.4,
            bitterness = 2.6,
            sweetness = 4.0,
            description = "Café de perfil mais refinado, com aroma marcante e bom equilíbrio sensorial.",
            tags = listOf("Especial", "Aromático", "Equilibrado")
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
            valueRating = 4.1,
            wouldBuyAgainPercent = 68,
            aroma = 3.5,
            flavor = 3.6,
            body = 3.7,
            acidity = 2.3,
            bitterness = 4.0,
            sweetness = 2.8,
            description = "Café tradicional, forte e de preço competitivo. Pode agradar quem prefere torra mais escura.",
            tags = listOf("Tradicional", "Forte", "Barato")
        )
    )
}

fun findCoffeeById(id: String): CoffeeUiModel? {
    return sampleCoffees().firstOrNull {
        it.id == id
    }
}

fun topRatedCoffees(): List<CoffeeUiModel> {
    return sampleCoffees().sortedByDescending {
        it.rating
    }
}

fun bestValueCoffees(): List<CoffeeUiModel> {
    return sampleCoffees().sortedWith(
        compareByDescending<CoffeeUiModel> {
            it.valueRating
        }.thenBy {
            it.priceKg
        }
    )
}

fun mostReviewedCoffees(): List<CoffeeUiModel> {
    return sampleCoffees().sortedByDescending {
        it.totalReviews
    }
}

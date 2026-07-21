package com.maurofmorato.cafecomnota.data.coffee

data class CoffeeCreateRequest(
    val name: String,
    val brand: String,
    val type: String,
    val roast: String,
    val standardWeightGrams: Int,
    val userId: String,
    val accessToken: String,
    val status: String,
    val producer: String? = null,
    val originRegion: String? = null,
    val altitudeMeters: Int? = null,
    val variety: String? = null,
    val process: String? = null,
    val aromaFlavor: String? = null,
    val certification: String? = null
)

package com.maurofmorato.cafecomnota.data.coffee

data class CoffeeCreateRequest(
    val name: String,
    val brand: String,
    val type: String,
    val roast: String,
    val standardWeightGrams: Int,
    val userId: String,
    val accessToken: String,
    val status: String
)

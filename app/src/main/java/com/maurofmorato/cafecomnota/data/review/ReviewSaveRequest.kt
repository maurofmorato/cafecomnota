package com.maurofmorato.cafecomnota.data.review

data class ReviewSaveRequest(
    val cafeId: String,
    val userId: String,
    val accessToken: String,
    val rating: Double,
    val wouldBuyAgain: Boolean,
    val pricePaid: Double?,
    val weightGrams: Double?,
    val brewMethod: String,
    val comment: String
)

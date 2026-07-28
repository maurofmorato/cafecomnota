package com.maurofmorato.cafecomnota.data.review

data class ReviewSaveRequest(
    val cafeId: String,
    val userId: String,
    val accessToken: String,
    val rating: Double,
    val wouldBuyAgain: Boolean,
    val aroma: Int?,
    val flavor: Int?,
    val body: Int?,
    val bitterness: Int?,
    val acidity: Int?,
    val sweetness: Int?,
    val valueRating: Int?,
    val pricePaid: Double?,
    val weightGrams: Double?,
    val brewMethod: String,
    val comment: String
)

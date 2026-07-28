package com.maurofmorato.cafecomnota.data.review

data class ExistingReviewData(
    val rating: Double? = null,
    val wouldBuyAgain: Boolean? = null,
    val aroma: Int? = null,
    val flavor: Int? = null,
    val body: Int? = null,
    val bitterness: Int? = null,
    val acidity: Int? = null,
    val sweetness: Int? = null,
    val valueRating: Int? = null,
    val pricePaid: Double? = null,
    val weightGrams: Double? = null,
    val brewMethod: String? = null,
    val comment: String? = null
) {
    val hasAnyData: Boolean
        get() =
            rating != null ||
                wouldBuyAgain != null ||
                aroma != null ||
                flavor != null ||
                body != null ||
                bitterness != null ||
                acidity != null ||
                sweetness != null ||
                valueRating != null ||
                pricePaid != null ||
                weightGrams != null ||
                !brewMethod.isNullOrBlank() ||
                !comment.isNullOrBlank()

    val hasDetailedRatings: Boolean
        get() =
            aroma != null ||
                flavor != null ||
                body != null ||
                bitterness != null ||
                acidity != null ||
                sweetness != null ||
                valueRating != null
}

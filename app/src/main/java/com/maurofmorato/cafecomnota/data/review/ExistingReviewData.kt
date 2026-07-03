package com.maurofmorato.cafecomnota.data.review

data class ExistingReviewData(
    val rating: Double? = null,
    val wouldBuyAgain: Boolean? = null,
    val pricePaid: Double? = null,
    val weightGrams: Double? = null,
    val brewMethod: String? = null,
    val comment: String? = null
) {
    val hasAnyData: Boolean
        get() =
            rating != null ||
                wouldBuyAgain != null ||
                pricePaid != null ||
                weightGrams != null ||
                !brewMethod.isNullOrBlank() ||
                !comment.isNullOrBlank()
}

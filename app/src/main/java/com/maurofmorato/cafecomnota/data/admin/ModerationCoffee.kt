package com.maurofmorato.cafecomnota.data.admin

data class ModerationCoffee(
    val id: String,
    val name: String,
    val brand: String,
    val status: String,
    val createdAt: String,
    val authorId: String,
    val moderationReason: String,
    val expectedPhotos: Int,
    val uploadedPhotos: Int,
    val photosStatus: String,
    val imagePath: String? = null
)
